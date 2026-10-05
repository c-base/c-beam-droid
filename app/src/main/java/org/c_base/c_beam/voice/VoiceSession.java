package org.c_base.c_beam.voice;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * One spoken request from transcript to outcome: asks the model, runs QUERY tools and
 * feeds their results back, runs SELF tools, and hands CONFIRM tools to the UI untouched.
 * A question that needs no tool (general knowledge, a calculation, a translation) the
 * model answers by itself; only actions are bound to the tool list.
 * Knows nothing about HTTP (that is {@link ChatModel}) or views (the controller).
 */
public class VoiceSession {
    /** A query may need a couple of rounds (ask, tool result, answer) but never many. */
    private static final int MAX_ROUNDS = 3;

    public interface Listener {
        /** The model answered in words. */
        void onAnswer(String text);
        /** A SELF or QUERY action ran; description says which. */
        void onExecuted(VoiceCommands.Action action);
        /** A CONFIRM action is proposed; the UI decides whether to run it. */
        void onConfirmRequired(VoiceCommands.Action action);
        void onError(String message);
    }

    private final ChatModel model;
    private final VoiceCommands commands;
    private final Listener listener;
    private final JSONArray messages = new JSONArray();
    private int rounds;

    public VoiceSession(ChatModel model, VoiceCommands commands, String username, Listener listener) {
        this.model = model;
        this.commands = commands;
        this.listener = listener;
        put(message("system", systemPrompt(username)));
    }

    public void start(String transcript) {
        put(message("user", transcript));
        step();
    }

    public void cancel() {
        model.cancel();
    }

    private void step() {
        rounds++;
        model.chat(messages, commands.toolDefinitions(), new ChatModel.Callback() {
            @Override
            public void onReply(ChatModel.Reply reply) {
                if (reply.toolCall == null) {
                    if (reply.text.isEmpty()) {
                        listener.onError("keine antwort");
                    } else {
                        listener.onAnswer(reply.text);
                    }
                    return;
                }
                VoiceCommands.Action action = commands.validate(reply.toolCall);
                if (action == null) {
                    listener.onError("nicht verstanden");
                    return;
                }
                switch (action.level) {
                    case QUERY: {
                        String result = commands.execute(action);
                        if (rounds >= MAX_ROUNDS) {
                            listener.onAnswer(result);
                            return;
                        }
                        put(assistantToolCall(reply.toolCall));
                        put(toolResult(reply.toolCall, result));
                        step();
                        break;
                    }
                    case SELF:
                        commands.execute(action);
                        listener.onExecuted(action);
                        break;
                    case CONFIRM:
                        listener.onConfirmRequired(action);
                        break;
                }
            }

            @Override
            public void onError(String message) {
                listener.onError(message);
            }
        });
    }

    private static String systemPrompt(String username) {
        String now = new SimpleDateFormat("EEEE, d. MMMM yyyy, HH:mm", Locale.GERMAN).format(new Date());
        return "Du bist c-beam, der Bordcomputer der Raumstation c-base in Berlin. "
                + "Du bekommst gesprochene Sätze als Text, erkannt per Spracherkennung; "
                + "kleine Fehler darin sind möglich. "
                + "Führe Befehle ausschließlich über die Werkzeuge aus und erfinde keine. "
                + "Fragen zur Station (wer an Bord ist, Bar, Events, Missionen, Artefakte) "
                + "beantwortest du mit Hilfe der Werkzeuge. Alle anderen Fragen, etwa Wissens- "
                + "und Verständnisfragen, Rechnen oder Übersetzen, beantwortest du selbst aus "
                + "deinem Wissen; weißt du etwas nicht sicher, sage das. "
                + "Soll etwas getan werden, wofür es kein Werkzeug gibt, sage in einem Satz, "
                + "dass du das nicht kannst. "
                + "Antworte immer auf Deutsch, kurz und in höchstens drei Sätzen. Deine Antwort "
                + "wird vorgelesen: Fließtext ohne Markdown, Listen, Tabellen oder Formeln. "
                + "Keine Floskeln, keine Angebote, keine Rückfragen am Ende und keine Erklärung, "
                + "woher du etwas weißt. "
                + "Der sprechende Nutzer heißt " + username + ". "
                + "Jetzt ist " + now + ".";
    }

    private void put(JSONObject message) {
        messages.put(message);
    }

    private static JSONObject message(String role, String content) {
        try {
            return new JSONObject().put("role", role).put("content", content);
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Echoes the model's own tool call back, as the chat format requires before a tool result. */
    private static JSONObject assistantToolCall(ChatModel.ToolCall call) {
        try {
            JSONObject fn = new JSONObject().put("name", call.name).put("arguments", call.arguments);
            JSONObject tc = new JSONObject().put("id", call.id).put("type", "function").put("function", fn);
            return new JSONObject().put("role", "assistant").put("content", JSONObject.NULL)
                    .put("tool_calls", new JSONArray().put(tc));
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }

    private static JSONObject toolResult(ChatModel.ToolCall call, String result) {
        try {
            return new JSONObject().put("role", "tool").put("tool_call_id", call.id).put("content", result);
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }
}
