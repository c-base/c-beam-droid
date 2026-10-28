package org.c_base.c_beam.voice;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Sandy Strands, the station's experimental voice assistant, used as a knowledge source:
 * it has its own RAG over the c-base knowledge and answers a question outright, with the
 * documents it drew on. The app's own model then answers from Sandy's answer the way it
 * answers from Qdrant chunks, so the station tools (login, announcements, ...) stay with
 * the app's model.
 *
 * Sandy only talks over its voice WebSocket: a {@code text_input} message goes in, JSON
 * frames come back ({@code status}, {@code tool_log}, streamed {@code bot_text}, ...)
 * along with binary audio of Sandy reading its answer aloud: raw 16-bit mono PCM at
 * 24 kHz in 100 ms frames. By default that audio is dropped; with the setting on it is
 * played as it streams (Sandy switched to its German voice first), and the phone's own
 * voice then stays quiet for that answer. The final {@code bot_text} carries
 * {@code done: true}, the full text and the sources; it arrives once Sandy has finished
 * speaking, typically 10 to 15 s after the question. Each lookup opens its own
 * connection, so nothing of a previous question leaks in.
 */
public class SandyStrands implements KnowledgeSource {
    private static final String TAG = "SandyStrands";

    /** The voice WebSocket of the Sandy Strands docker stack; crew network only. */
    public static final String DEFAULT_URL = "wss://ai.cbrp3.c-base.org:8444/ws/voice";

    private static final int CONNECT_TIMEOUT_MS = 5000;
    /** Sandy thinks, searches and speaks before the final frame; allow for a slow day. */
    private static final long ANSWER_TIMEOUT_MS = 90000;
    private static final int MAX_ANSWER_CHARS = 1500;
    private static final int SAMPLE_RATE = 24000;
    /** Sandy's voice for the audio; its default is English, whatever the question's language. */
    private static final String VOICE = "de";

    private final String url;
    private final boolean playAudio;
    private final Handler main = new Handler(Looper.getMainLooper());
    private volatile WebSocketClient current;
    private volatile PcmPlayer player;
    private volatile boolean spoke;
    private volatile boolean cancelled;

    /**
     * @param url       Sandy's voice WebSocket
     * @param playAudio play Sandy's spoken answer on the phone instead of dropping it
     */
    public SandyStrands(String url, boolean playAudio) {
        this.url = url;
        this.playAudio = playAudio;
    }

    @Override
    public void search(final String query, final Callback callback) {
        cancelled = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String result = ask(query);
                    main.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!cancelled) {
                                callback.onResult(result);
                            }
                        }
                    });
                } catch (IOException e) {
                    Log.w(TAG, "lookup failed: " + e.getMessage());
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
        }, "SandyStrands").start();
    }

    @Override
    public void cancel() {
        cancelled = true;
        WebSocketClient c = current;
        if (c != null) {
            c.close();
        }
        hush();
    }

    @Override
    public boolean spokeAloud() {
        return spoke;
    }

    @Override
    public void hush() {
        PcmPlayer p = player;
        if (p != null) {
            p.stop();
        }
    }

    /** One answer per connection; the latch opens on the final frame or on failure. */
    private static class Exchange {
        final CountDownLatch done = new CountDownLatch(1);
        volatile String answer = "";
        volatile String error;
        final List<String> sources = new ArrayList<String>();
    }

    private String ask(final String query) throws IOException {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new IOException("sandy strands: ungültige adresse " + url);
        }
        final Exchange x = new Exchange();
        WebSocketClient client = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                try {
                    if (playAudio) {
                        send(new JSONObject().put("type", "control").put("action", "set_voice")
                                .put("voice", VOICE).toString());
                    }
                    send(new JSONObject().put("type", "text_input").put("text", query).toString());
                } catch (JSONException e) {
                    x.error = "sandy strands: frage nicht kodierbar";
                    x.done.countDown();
                }
            }

            @Override
            public void onMessage(String message) {
                handle(message, x);
            }

            @Override
            public void onMessage(ByteBuffer bytes) {
                if (!playAudio || cancelled) {
                    return;
                }
                PcmPlayer p = player;
                if (p == null) {
                    p = new PcmPlayer(SAMPLE_RATE);
                    player = p;
                }
                byte[] pcm = new byte[bytes.remaining()];
                bytes.get(pcm);
                p.feed(pcm);
                spoke = true;
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                if (x.done.getCount() > 0) {
                    x.error = "sandy strands: verbindung beendet" + (remote ? " (" + code + ")" : "");
                    x.done.countDown();
                }
            }

            @Override
            public void onError(Exception e) {
                x.error = "sandy strands: " + e.getMessage();
                x.done.countDown();
            }
        };
        // No ping/pong watchdog: the answer takes longer than its default interval.
        client.setConnectionLostTimeout(0);
        current = client;
        try {
            boolean connected;
            try {
                connected = client.connectBlocking(CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new IOException("sandy strands: abgebrochen");
            }
            if (!connected) {
                throw new IOException(x.error != null ? x.error : "sandy strands nicht erreichbar");
            }
            boolean finished;
            try {
                finished = x.done.await(ANSWER_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new IOException("sandy strands: abgebrochen");
            }
            if (!finished) {
                throw new IOException("sandy strands: keine antwort in "
                        + (ANSWER_TIMEOUT_MS / 1000) + " s");
            }
        } finally {
            current = null;
            client.close();
            PcmPlayer p = player;
            if (p != null) {
                p.finish();
            }
        }
        if (x.error != null) {
            throw new IOException(x.error);
        }
        return format(query, x.answer, x.sources);
    }

    private static void handle(String message, Exchange x) {
        JSONObject frame;
        try {
            frame = new JSONObject(message);
        } catch (JSONException e) {
            return;
        }
        String type = frame.optString("type", "");
        if ("bot_text".equals(type)) {
            // Streamed sentence by sentence; the final frame repeats the whole answer.
            String text = frame.optString("text", "").trim();
            if (!text.isEmpty()) {
                x.answer = text;
            }
            if (frame.optBoolean("done", false)) {
                JSONArray sources = frame.optJSONArray("sources");
                for (int i = 0; sources != null && i < sources.length(); i++) {
                    JSONObject s = sources.optJSONObject(i);
                    String name = s == null ? "" : s.optString("src", s.optString("path", "")).trim();
                    if (!name.isEmpty() && !x.sources.contains(name)) {
                        x.sources.add(name);
                    }
                }
                x.done.countDown();
            }
        } else if ("error".equals(type)) {
            x.error = "sandy strands: " + frame.optString("message", frame.optString("msg", "fehler"));
            x.done.countDown();
        } else if (Log.isLoggable(TAG, Log.DEBUG) && ("tool_log".equals(type) || "sys_log".equals(type))) {
            Log.d(TAG, message.length() > 300 ? message.substring(0, 299) + "…" : message);
        }
    }

    /**
     * Sandy answers like an assistant, with follow-up questions and all. The model gets
     * told what it is looking at so it keeps the facts and drops the small talk.
     */
    static String format(String query, String answer, List<String> sources) {
        if (answer.isEmpty()) {
            return "nichts gefunden: sandy strands hat zu „" + query + "“ nichts gesagt. sage das "
                    + "und verweise auf die datenrettungssonde siri, die diese information "
                    + "leider auch nicht enthält";
        }
        if (answer.length() > MAX_ANSWER_CHARS) {
            answer = answer.substring(0, MAX_ANSWER_CHARS - 1) + "…";
        }
        StringBuilder b = new StringBuilder("Antwort des Stations-Assistenten Sandy Strands auf "
                + "die Suchanfrage „" + query + "“. Sie ist die Fundstelle: die Fakten daraus "
                + "verwenden, Sandys Rückfragen und Höflichkeiten weglassen, nichts hinzuerfinden:"
                + "\n\n" + answer);
        if (!sources.isEmpty()) {
            b.append("\n\nQuellen: ");
            for (int i = 0; i < sources.size(); i++) {
                b.append(i == 0 ? "" : ", ").append(sources.get(i));
            }
        }
        return b.toString();
    }
}
