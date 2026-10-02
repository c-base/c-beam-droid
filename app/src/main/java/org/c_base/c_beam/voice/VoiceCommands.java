package org.c_base.c_beam.voice;

import org.c_base.c_beam.domain.Artefact;
import org.c_base.c_beam.domain.C_beam;
import org.c_base.c_beam.domain.Event;
import org.c_base.c_beam.domain.Mission;
import org.c_base.c_beam.domain.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The list of things the voice interface may do, each a thin wrapper around an existing
 * {@link C_beam} method. The model only ever sees this list and only ever proposes; what it
 * proposes is validated here, parameter by parameter, before anything runs.
 *
 * Every tool has a fixed level that the model cannot change:
 * QUERY runs at once and its result goes back to the model so it can answer;
 * SELF runs at once because it only touches the user's own state;
 * CONFIRM is anything audible or visible in the station and needs a tap first.
 */
public class VoiceCommands {
    public enum Level { QUERY, SELF, CONFIRM }

    private static final int MAX_TEXT_LENGTH = 200;
    private static final int MAX_LIST_ITEMS = 60;

    /** A validated, ready-to-run tool call. */
    public static class Action {
        public final String name;
        public final Level level;
        /** Shown to the user, e.g. in the confirmation card. */
        public final String description;
        final String text;
        final String sound;
        final String voice;

        Action(String name, Level level, String description, String text, String sound, String voice) {
            this.name = name;
            this.level = level;
            this.description = description;
            this.text = text;
            this.sound = sound;
            this.voice = voice;
        }
    }

    private final C_beam c_beam;
    private final String username;

    public VoiceCommands(C_beam c_beam, String username) {
        this.c_beam = c_beam;
        this.username = username;
    }

    /** Tool definitions for the model, built fresh so the sound list is current. */
    public JSONArray toolDefinitions() {
        JSONArray tools = new JSONArray();
        try {
            tools.put(tool("who_is_on_board",
                    "Liste der Crewmitglieder, die gerade in der c-base eingeloggt sind",
                    new JSONObject()));
            tools.put(tool("bar_status", "Ob die Bar der c-base gerade geöffnet ist",
                    new JSONObject()));
            tools.put(tool("events_today", "Die heutigen Events und Veranstaltungen in der c-base",
                    new JSONObject()));
            tools.put(tool("missions", "Die Missionen (Aufgaben) der c-base mit Status und AP",
                    new JSONObject()));
            tools.put(tool("artefacts",
                    "Die Artefakte der c-base, also Räume, Geräte und Einrichtungen der Station",
                    new JSONObject().put("search", new JSONObject()
                            .put("type", "string")
                            .put("description", "Optionaler Suchbegriff; leer für alle"))));
            tools.put(tool("login", "Den Nutzer in der c-base einloggen (an Bord melden)",
                    new JSONObject()));
            tools.put(tool("logout", "Den Nutzer aus der c-base ausloggen (von Bord melden)",
                    new JSONObject()));
            tools.put(tool("announce",
                    "Eine Durchsage über die Lautsprecher der Station machen",
                    new JSONObject().put("text", new JSONObject()
                            .put("type", "string")
                            .put("description", "Der Text der Durchsage")), "text"));
            tools.put(tool("say",
                    "Einen Text über die Lautsprecher der Station sprechen lassen",
                    new JSONObject()
                            .put("text", new JSONObject().put("type", "string"))
                            .put("voice", new JSONObject()
                                    .put("type", "string")
                                    .put("enum", new JSONArray().put("julia").put("r2d2"))
                                    .put("description", "julia ist die normale Stimme, r2d2 piept")),
                    "text"));
            JSONObject sound = new JSONObject().put("type", "string")
                    .put("description", "Dateiname des Sounds");
            List<String> sounds = c_beam.getSounds();
            if (!sounds.isEmpty()) {
                sound.put("enum", new JSONArray(sounds));
            }
            tools.put(tool("play_sound", "Einen Sound über die Lautsprecher der Station abspielen",
                    new JSONObject().put("sound", sound), "sound"));
        } catch (JSONException ignored) {
        }
        return tools;
    }

    /** Turns the model's proposal into an Action, or null if it is not on the list or malformed. */
    public Action validate(ChatModel.ToolCall call) {
        JSONObject args;
        try {
            args = new JSONObject(call.arguments == null || call.arguments.isEmpty() ? "{}" : call.arguments);
        } catch (JSONException e) {
            return null;
        }
        switch (call.name) {
            case "who_is_on_board":
                return new Action(call.name, Level.QUERY, "wer ist an bord", null, null, null);
            case "bar_status":
                return new Action(call.name, Level.QUERY, "bar-status", null, null, null);
            case "events_today":
                return new Action(call.name, Level.QUERY, "events", null, null, null);
            case "missions":
                return new Action(call.name, Level.QUERY, "missionen", null, null, null);
            case "artefacts":
                return new Action(call.name, Level.QUERY, "artefakte",
                        args.optString("search", "").trim(), null, null);
            case "login":
                return new Action(call.name, Level.SELF, "einloggen als " + username, null, null, null);
            case "logout":
                return new Action(call.name, Level.SELF, "ausloggen", null, null, null);
            case "announce": {
                String text = cleanText(args.optString("text", ""));
                return text == null ? null
                        : new Action(call.name, Level.CONFIRM, "durchsage: „" + text + "“", text, null, null);
            }
            case "say": {
                String text = cleanText(args.optString("text", ""));
                String voice = "r2d2".equals(args.optString("voice", "julia")) ? "r2d2" : "julia";
                return text == null ? null
                        : new Action(call.name, Level.CONFIRM, voice + " sagt: „" + text + "“", text, null, voice);
            }
            case "play_sound": {
                String sound = args.optString("sound", "");
                if (!c_beam.getSounds().contains(sound)) {
                    return null;
                }
                return new Action(call.name, Level.CONFIRM, "sound: " + sound, null, sound, null);
            }
            default:
                return null;
        }
    }

    /** Runs the action. For QUERY actions the return value is what the model gets to see. */
    public String execute(Action action) {
        switch (action.name) {
            case "who_is_on_board": {
                List<String> names = new ArrayList<String>();
                for (User u : c_beam.getOnlineList()) {
                    names.add(u.getUsername());
                }
                return names.isEmpty() ? "niemand" : join(names);
            }
            case "bar_status":
                return c_beam.getBarStatus() ? "geöffnet" : "geschlossen";
            case "events_today": {
                List<String> lines = new ArrayList<String>();
                for (Event e : c_beam.getEvents()) {
                    String line = e.toString();
                    String d = e.getDescription();
                    if (d != null && !d.trim().isEmpty()) {
                        line += ": " + shorten(d.trim(), 160);
                    }
                    lines.add(line);
                }
                return lines.isEmpty() ? "keine events heute" : joinLines(lines, MAX_LIST_ITEMS);
            }
            case "missions": {
                List<String> lines = new ArrayList<String>();
                for (Mission m : c_beam.getMissions()) {
                    String line = m.getShort_description() + " (" + m.getStatus() + ", "
                            + m.getAp() + " AP";
                    if (m.getAssigned_to() != null && !m.getAssigned_to().isEmpty()) {
                        line += ", übernommen von " + join(m.getAssigned_to());
                    }
                    lines.add(line + ")");
                }
                return lines.isEmpty() ? "keine missionen" : joinLines(lines, MAX_LIST_ITEMS);
            }
            case "artefacts": {
                String search = action.text == null ? "" : action.text.toLowerCase(Locale.GERMAN);
                List<String> names = new ArrayList<String>();
                for (Artefact a : c_beam.getArtefacts()) {
                    String name = a.getName();
                    if (name != null && (search.isEmpty()
                            || name.toLowerCase(Locale.GERMAN).contains(search))) {
                        names.add(name);
                    }
                }
                return names.isEmpty() ? "kein artefakt gefunden" : joinLines(names, MAX_LIST_ITEMS);
            }
            case "login":
                c_beam.force_login(username);
                return "ok";
            case "logout":
                c_beam.force_logout(username);
                return "ok";
            case "announce":
                c_beam.announce(action.text);
                return "ok";
            case "say":
                if ("r2d2".equals(action.voice)) {
                    c_beam.r2d2(action.text);
                } else {
                    c_beam.tts(action.text);
                }
                return "ok";
            case "play_sound":
                c_beam.play(action.sound);
                return "ok";
            default:
                return "unbekannt";
        }
    }

    private static JSONObject tool(String name, String description, JSONObject properties)
            throws JSONException {
        return tool(name, description, properties, null);
    }

    private static JSONObject tool(String name, String description, JSONObject properties,
                                   String required) throws JSONException {
        JSONObject parameters = new JSONObject().put("type", "object").put("properties", properties);
        if (required != null) {
            parameters.put("required", new JSONArray().put(required));
        }
        return new JSONObject().put("type", "function").put("function", new JSONObject()
                .put("name", name)
                .put("description", description)
                .put("parameters", parameters));
    }

    private static String cleanText(String text) {
        String t = text.trim().replaceAll("\\s+", " ");
        if (t.isEmpty() || t.length() > MAX_TEXT_LENGTH) {
            return null;
        }
        return t;
    }

    /** One item per line, capped so a long list does not balloon the prompt. */
    private static String joinLines(List<String> items, int max) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < items.size() && i < max; i++) {
            if (b.length() > 0) {
                b.append('\n');
            }
            b.append(items.get(i));
        }
        if (items.size() > max) {
            b.append("\n… und ").append(items.size() - max).append(" weitere");
        }
        return b.toString();
    }

    private static String shorten(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    private static String join(List<String> items) {
        StringBuilder b = new StringBuilder();
        for (String s : items) {
            if (b.length() > 0) {
                b.append(", ");
            }
            b.append(s);
        }
        return b.toString();
    }
}
