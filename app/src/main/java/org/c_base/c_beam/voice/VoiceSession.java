package org.c_base.c_beam.voice;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * One spoken request from transcript to outcome: asks the model, runs QUERY tools and
 * feeds their results back, runs SELF tools, and hands CONFIRM tools to the UI untouched.
 * A question that needs no tool (general knowledge, a calculation, a translation) the
 * model answers by itself; only actions are bound to the tool list. Questions about the
 * station's own lore go through the knowledge base when one is configured.
 * Knows nothing about HTTP (that is {@link ChatModel}) or views (the controller). What
 * was said goes into the shared {@link Conversation}, so the next request can refer to it.
 */
public class VoiceSession {
    /** A query may need a couple of rounds (ask, tool result, answer) but never many. */
    private static final int MAX_ROUNDS = 3;
    private static final String TAG = "VoiceSession";
    /**
     * Answers that say "I can't" or "nothing found". They are shown but kept out of the
     * conversation: the 4B model copies whatever it sees itself saying, and one refusal in
     * the scrollback made it refuse every following question (even plain ones with a tool
     * at hand) until the conversation expired or the app was restarted. Prompt wording
     * alone did not stop that.
     */
    private static final Pattern REFUSAL = Pattern.compile(
            "kann ich (leider )?nicht|nicht (tun|machen|ausführen|möglich)|kein\\w*( \\w+)? werkzeug"
            + "|(weiß|weiss) (dazu|darüber|davon) nichts|dazu (ist )?nichts (bekannt|gefunden)"
            + "|datenrettungssonde",
            Pattern.CASE_INSENSITIVE);

    public interface Listener {
        /** The model answered in words. */
        void onAnswer(String text);
        /** A QUERY action is running; the answer is still to come. */
        void onQuery(VoiceCommands.Action action);
        /** A SELF action ran; description says which. */
        void onExecuted(VoiceCommands.Action action);
        /** A CONFIRM action is proposed; the UI decides whether to run it. */
        void onConfirmRequired(VoiceCommands.Action action);
        void onError(String message);
    }

    private final ChatModel model;
    private final VoiceCommands commands;
    private final String username;
    private final Conversation conversation;
    private final Listener listener;
    private int rounds;
    private int mark;

    public VoiceSession(ChatModel model, VoiceCommands commands, String username,
                        Conversation conversation, Listener listener) {
        this.model = model;
        this.commands = commands;
        this.username = username;
        this.conversation = conversation;
        this.listener = listener;
    }

    public void start(String transcript) {
        Log.d(TAG, "transcript: " + transcript);
        conversation.begin();
        mark = conversation.mark();
        conversation.add(Conversation.message("user", transcript));
        step();
    }

    public void cancel() {
        model.cancel();
        commands.cancel();
        conversation.rollback(mark);
    }

    private void step() {
        rounds++;
        model.chat(conversation.messages(systemPrompt(username)), commands.toolDefinitions(),
                new ChatModel.Callback() {
            @Override
            public void onReply(ChatModel.Reply reply) {
                if (reply.toolCall == null) {
                    Log.d(TAG, "answer: " + reply.text);
                    if (reply.text.isEmpty()) {
                        fail("keine antwort");
                    } else if (REFUSAL.matcher(reply.text).find()) {
                        Log.d(TAG, "refusal, not kept in the conversation");
                        conversation.rollback(mark);
                        listener.onAnswer(reply.text);
                    } else {
                        conversation.add(Conversation.message("assistant", reply.text));
                        listener.onAnswer(reply.text);
                    }
                    return;
                }
                Log.d(TAG, "tool call: " + reply.toolCall.name + " " + reply.toolCall.arguments);
                VoiceCommands.Action action = commands.validate(reply.toolCall);
                if (action == null) {
                    fail("nicht verstanden");
                    return;
                }
                switch (action.level) {
                    case QUERY: {
                        final ChatModel.ToolCall call = reply.toolCall;
                        listener.onQuery(action);
                        commands.query(action, new VoiceCommands.QueryCallback() {
                            @Override
                            public void onResult(String result) {
                                Log.d(TAG, "tool result (" + result.length() + " chars): "
                                        + (result.length() > 300 ? result.substring(0, 300) + "…" : result));
                                conversation.add(Conversation.assistantToolCall(call));
                                conversation.add(Conversation.toolResult(call, result));
                                if (rounds >= MAX_ROUNDS) {
                                    conversation.add(Conversation.message("assistant", result));
                                    listener.onAnswer(result);
                                    return;
                                }
                                step();
                            }

                            @Override
                            public void onError(String message) {
                                fail(message);
                            }
                        });
                        break;
                    }
                    case SELF:
                        commands.execute(action);
                        conversation.add(Conversation.assistantToolCall(reply.toolCall));
                        conversation.add(Conversation.toolResult(reply.toolCall, "ausgeführt"));
                        listener.onExecuted(action);
                        break;
                    case CONFIRM:
                        conversation.proposed(reply.toolCall);
                        listener.onConfirmRequired(action);
                        break;
                }
            }

            @Override
            public void onError(String message) {
                fail(message);
            }
        });
    }

    /** A request that went nowhere leaves nothing in the conversation. */
    private void fail(String message) {
        conversation.rollback(mark);
        listener.onError(message);
    }

    private String systemPrompt(String username) {
        String now = new SimpleDateFormat("EEEE, d. MMMM yyyy, HH:mm", Locale.GERMAN).format(new Date());
        String knowledge = !commands.hasKnowledge() ? ""
                // Firm wording on purpose: the 4B model otherwise answers "das kann ich
                // nicht sagen" to questions like "wie komme ich ins WLAN" without looking,
                // and once one lookup in the conversation came back empty it stopped looking
                // altogether and repeated the refusal until the conversation expired.
                : "Jede Frage, die etwas mit der c-base zu tun hat (ihre Geschichte und "
                + "Legende, Räume, Technik, WLAN, Preise, Begriffe, Regeln, Projekte, Leute, "
                + "und wie man etwas in der Station benutzt oder bekommt), beantwortest du, "
                + "indem du zuerst das Werkzeug " + VoiceCommands.KNOWLEDGE + " aufrufst und "
                + "dann nur aus dessen Fundstellen antwortest. Sage bei solchen Fragen nie, "
                + "dass du etwas nicht weißt oder nicht sagen kannst, ohne vorher "
                + VoiceCommands.KNOWLEDGE + " aufgerufen zu haben. Das gilt für jede Frage neu: "
                + "auch wenn im bisherigen Gespräch zu einem Thema nichts gefunden wurde, rufst "
                + "du für eine neue oder anders gestellte Frage " + VoiceCommands.KNOWLEDGE
                + " wieder auf, mit einer neuen Suchanfrage. Die Fundstellen sind Kandidaten "
                + "einer Ähnlichkeitssuche und können unpassend sein: verwende nur, was die "
                + "Frage wirklich beantwortet, und erfinde nichts dazu. Steht die Antwort nicht "
                + "in den Fundstellen, sage, dass die Station dazu nichts weiß, und verweise auf "
                + "die Datenrettungssonde „Siri“, die diese Information leider auch nicht "
                + "enthält, etwa: „dazu weiß die station nichts. die datenrettungssonde siri "
                + "enthält diese information leider auch nicht.“ Die Spracherkennung "
                + "schreibt c-base oft als „Seabase“, „Sea Base“, „C Base“ oder „die Base“ und "
                + "c-beam als „Seabeam“; gemeint ist immer die c-base beziehungsweise c-beam, "
                + "und in Suchanfragen schreibst du c-base. ";
        return "Du bist c-beam, der Bordcomputer der Raumstation c-base in Berlin. "
                + "Du bekommst gesprochene Sätze als Text, erkannt per Spracherkennung; "
                + "kleine Fehler darin sind möglich. "
                + "Führe Befehle ausschließlich über die Werkzeuge aus und erfinde keine. "
                + "Soll etwas getan werden, wofür es kein Werkzeug gibt, sage in einem Satz, "
                + "dass du das nicht kannst; das gilt nur für Handlungen, nie für Fragen. Eine "
                + "Frage ist nie eine Handlung: was mit wer, was, wann, wo, warum, wie oder "
                + "welche beginnt oder sonst eine Frage ist, beantwortest du immer, aus den "
                + "Werkzeugergebnissen oder deinem Wissen, und sagst nie, dass du es nicht tun "
                + "kannst. "
                + "Fragen zur Station (wer an Bord ist, Bar, Events, Missionen, Artefakte) "
                + "beantwortest du mit Hilfe der Werkzeuge. " + knowledge
                + "Alle anderen Fragen, etwa allgemeine Wissens- und Verständnisfragen, Rechnen "
                + "oder Übersetzen, beantwortest du immer selbst aus deinem Wissen, ohne "
                + "Werkzeug. Weißt du etwas nicht sicher, sage das. "
                + "Nachfragen beziehen sich auf das bisherige Gespräch. Fragt der Nutzer erneut "
                + "nach dem Zustand der Station, zum Beispiel mit „und jetzt?“, rufe das "
                + "Werkzeug neu auf, denn frühere Werkzeugergebnisse können veraltet sein. "
                + "Antworte immer auf Deutsch, kurz und in höchstens drei Sätzen. Deine Antwort "
                + "wird vorgelesen: Fließtext ohne Markdown, Listen, Tabellen oder Formeln. "
                + "Keine Floskeln, keine Angebote, keine Rückfragen am Ende und keine Erklärung, "
                + "woher du etwas weißt. "
                + "Die Legende der Station ist für dich Tatsache: die c-base ist eine vor "
                + "Urzeiten unter Berlin-Mitte abgestürzte Raumstation, und der Berliner "
                + "Fernsehturm ist ihre aus dem Boden ragende Antenne. Geht es um den Fernsehturm, "
                + "sage das dazu und beantworte die eigentliche Frage, etwa nach Höhe oder "
                + "Baujahr, trotzdem. "
                + "Der sprechende Nutzer heißt " + username + ". "
                + "Jetzt ist " + now + ".";
    }
}
