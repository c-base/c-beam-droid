package org.c_base.c_beam.voice;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * One chat-completion request to the language model on the station's AI server,
 * OpenAI-compatible (`/chat/completions` with `tools`). Pure transport: it neither knows
 * what the tools do nor decides anything; see {@link VoiceSession} for that.
 */
public class ChatModel {
    private static final String TAG = "ChatModel";

    public static final String DEFAULT_MODEL = "gemma4:e4b";
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 60000;

    /** A tool invocation the model proposes; the arguments are whatever it produced. */
    public static class ToolCall {
        public final String id;
        public final String name;
        public final String arguments;

        ToolCall(String id, String name, String arguments) {
            this.id = id;
            this.name = name;
            this.arguments = arguments;
        }
    }

    /** Either a text answer or a tool call, never both. */
    public static class Reply {
        public final String text;
        public final ToolCall toolCall;

        Reply(String text, ToolCall toolCall) {
            this.text = text;
            this.toolCall = toolCall;
        }
    }

    public interface Callback {
        void onReply(Reply reply);
        void onError(String message);
    }

    private final String url;
    private final String model;
    private final Handler main = new Handler(Looper.getMainLooper());
    private volatile HttpURLConnection current;
    private volatile boolean cancelled;

    public ChatModel(String baseUrl, String model) {
        this.url = baseUrl + "/chat/completions";
        this.model = model;
    }

    public void chat(final JSONArray messages, final JSONArray tools, final Callback callback) {
        cancelled = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Reply reply = request(messages, tools);
                    main.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!cancelled) {
                                callback.onReply(reply);
                            }
                        }
                    });
                } catch (IOException e) {
                    Log.w(TAG, "chat failed", e);
                    final String message = e.getMessage();
                    main.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!cancelled) {
                                callback.onError(message);
                            }
                        }
                    });
                }
            }
        }, "ChatModel").start();
    }

    public void cancel() {
        cancelled = true;
        HttpURLConnection c = current;
        if (c != null) {
            c.disconnect();
        }
    }

    private Reply request(JSONArray messages, JSONArray tools) throws IOException {
        JSONObject body = new JSONObject();
        try {
            body.put("model", model);
            body.put("temperature", 0);
            body.put("stream", false);
            body.put("max_tokens", 300);
            body.put("messages", messages);
            body.put("tools", tools);
        } catch (JSONException e) {
            throw new IOException(e);
        }
        byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);

        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        current = c;
        try {
            c.setRequestMethod("POST");
            c.setDoOutput(true);
            c.setConnectTimeout(CONNECT_TIMEOUT_MS);
            c.setReadTimeout(READ_TIMEOUT_MS);
            c.setFixedLengthStreamingMode(payload.length);
            c.setRequestProperty("Content-Type", "application/json");
            c.setRequestProperty("Accept", "application/json");
            OutputStream out = c.getOutputStream();
            out.write(payload);
            out.flush();

            int code = c.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                Log.w(TAG, "HTTP " + code + ": " + readAll(c.getErrorStream()));
                if (code == 429) {
                    String retry = c.getHeaderField("Retry-After");
                    throw new IOException(retry == null ? "KI-server ausgelastet"
                            : "KI-server ausgelastet, in " + retry + " s nochmal");
                }
                throw new IOException("HTTP " + code);
            }
            return parse(readAll(c.getInputStream()));
        } finally {
            current = null;
            c.disconnect();
        }
    }

    private static Reply parse(String body) throws IOException {
        try {
            JSONObject message = new JSONObject(body)
                    .getJSONArray("choices").getJSONObject(0).getJSONObject("message");
            JSONArray calls = message.optJSONArray("tool_calls");
            if (calls != null && calls.length() > 0) {
                // One command per recording: further calls in the same reply are ignored.
                JSONObject call = calls.getJSONObject(0);
                JSONObject fn = call.getJSONObject("function");
                return new Reply(null, new ToolCall(call.optString("id", "call-0"),
                        fn.getString("name"), fn.optString("arguments", "{}")));
            }
            return new Reply(message.optString("content", "").trim(), null);
        } catch (JSONException e) {
            throw new IOException("unerwartete antwort");
        }
    }

    private static String readAll(InputStream in) throws IOException {
        if (in == null) {
            return "";
        }
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int n;
        while ((n = in.read(chunk)) > 0) {
            buf.write(chunk, 0, n);
        }
        in.close();
        return buf.toString("UTF-8");
    }
}
