package org.c_base.c_beam.voice;

import android.os.SystemClock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * What the model has seen so far, kept across recordings so that a follow-up ("und in
 * Fuß?") makes sense. Lives in the controller, i.e. as long as the screen; forgets
 * everything once the user has been quiet for {@link #TTL_MS} and never grows past
 * {@link #MAX_MESSAGES} entries. The system prompt is not stored here: it carries the
 * current time and is rebuilt for every request.
 */
public class Conversation {
    /** Quiet for this long and the next recording starts a fresh conversation. */
    static final long TTL_MS = 3 * 60 * 1000;
    /** Scrollback for the model; older exchanges fall off, always at a user message. */
    static final int MAX_MESSAGES = 24;

    private final List<JSONObject> history = new ArrayList<JSONObject>();
    private long lastUsed;
    /** A CONFIRM proposal the UI is still showing; closed by {@link #resolved}. */
    private ChatModel.ToolCall pending;

    /** Called at the start of each request. */
    void begin() {
        long now = SystemClock.elapsedRealtime();
        if (lastUsed != 0 && now - lastUsed > TTL_MS) {
            history.clear();
        }
        lastUsed = now;
    }

    /** Where the history stands right now; {@link #rollback} returns to this point. */
    int mark() {
        return history.size();
    }

    /** Drops everything added since the mark, so a failed request leaves no trace. */
    void rollback(int mark) {
        while (history.size() > mark) {
            history.remove(history.size() - 1);
        }
    }

    void add(JSONObject message) {
        history.add(message);
        trim();
    }

    /** The messages for a request: the fresh system prompt, then the history. */
    JSONArray messages(String systemPrompt) {
        JSONArray messages = new JSONArray();
        messages.put(message("system", systemPrompt));
        for (JSONObject m : history) {
            messages.put(m);
        }
        return messages;
    }

    /** Remembers the proposal the UI took over; what became of it goes into the history. */
    void proposed(ChatModel.ToolCall call) {
        pending = call;
    }

    /** The user tapped "ausführen" or "verwerfen" (or the proposal died with the screen). */
    public void resolved(boolean executed) {
        if (pending == null) {
            return;
        }
        add(assistantToolCall(pending));
        add(toolResult(pending, executed ? "ausgeführt"
                : "abgelehnt: der Nutzer hat die Ausführung nicht bestätigt, es ist nichts passiert"));
        pending = null;
    }

    public void clear() {
        history.clear();
        pending = null;
        lastUsed = 0;
    }

    /** Never cuts between a tool call and its result: the oldest entry is always a user turn. */
    private void trim() {
        while (history.size() > MAX_MESSAGES) {
            history.remove(0);
        }
        while (!history.isEmpty() && !"user".equals(history.get(0).optString("role"))) {
            history.remove(0);
        }
    }

    static JSONObject message(String role, String content) {
        try {
            return new JSONObject().put("role", role).put("content", content);
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Echoes the model's own tool call back, as the chat format requires before a tool result. */
    static JSONObject assistantToolCall(ChatModel.ToolCall call) {
        try {
            JSONObject fn = new JSONObject().put("name", call.name).put("arguments", call.arguments);
            JSONObject tc = new JSONObject().put("id", call.id).put("type", "function").put("function", fn);
            return new JSONObject().put("role", "assistant").put("content", JSONObject.NULL)
                    .put("tool_calls", new JSONArray().put(tc));
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }

    static JSONObject toolResult(ChatModel.ToolCall call, String result) {
        try {
            return new JSONObject().put("role", "tool").put("tool_call_id", call.id).put("content", result);
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }
}
