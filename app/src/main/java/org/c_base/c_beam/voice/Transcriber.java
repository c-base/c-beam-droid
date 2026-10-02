package org.c_base.c_beam.voice;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Sends a WAV recording to the Whisper endpoint on the station's AI server and returns the
 * transcript. OpenAI-compatible: multipart POST to /v1/audio/transcriptions.
 *
 * Only reachable from the crew network; the caller checks that. One request at a time.
 */
public class Transcriber {
    private static final String TAG = "Transcriber";

    public static final String DEFAULT_URL =
            "https://ai.cbrp3.c-base.org/ollama/v1/audio/transcriptions";
    private static final String MODEL = "Whisper-Large-v3-Turbo";
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 30000;

    public interface Callback {
        void onTranscript(String text);
        void onError(String message);
    }

    private final String url;
    private final Handler main = new Handler(Looper.getMainLooper());
    private volatile HttpURLConnection current;
    private volatile boolean cancelled;

    public Transcriber(String url) {
        this.url = url;
    }

    /** Uploads on a background thread and reports on the main thread, unless cancelled. */
    public void transcribe(final byte[] wav, final Callback callback) {
        cancelled = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                String text;
                try {
                    text = upload(wav);
                } catch (IOException e) {
                    Log.w(TAG, "transcription failed", e);
                    report(callback, null, e.getMessage());
                    return;
                }
                report(callback, text, null);
            }
        }, "Transcriber").start();
    }

    /** Drops the running request; its result is never reported. */
    public void cancel() {
        cancelled = true;
        HttpURLConnection c = current;
        if (c != null) {
            c.disconnect();
        }
    }

    private void report(final Callback callback, final String text, final String error) {
        main.post(new Runnable() {
            @Override
            public void run() {
                if (cancelled) {
                    return;
                }
                if (error != null) {
                    callback.onError(error);
                } else {
                    callback.onTranscript(text);
                }
            }
        });
    }

    private String upload(byte[] wav) throws IOException {
        String boundary = "----c-beam-" + System.currentTimeMillis();
        byte[] head = ("--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"model\"\r\n\r\n"
                + MODEL + "\r\n"
                + "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"speech.wav\"\r\n"
                + "Content-Type: audio/wav\r\n\r\n").getBytes(StandardCharsets.UTF_8);
        byte[] tail = ("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);

        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        current = c;
        try {
            c.setRequestMethod("POST");
            c.setDoOutput(true);
            c.setConnectTimeout(CONNECT_TIMEOUT_MS);
            c.setReadTimeout(READ_TIMEOUT_MS);
            c.setFixedLengthStreamingMode(head.length + wav.length + tail.length);
            c.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            c.setRequestProperty("Accept", "application/json");

            OutputStream out = c.getOutputStream();
            out.write(head);
            out.write(wav);
            out.write(tail);
            out.flush();

            int code = c.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                String body = readAll(c.getErrorStream());
                Log.w(TAG, "HTTP " + code + ": " + body);
                throw new IOException(code == 429 ? "KI-server ausgelastet" : "HTTP " + code);
            }
            String body = readAll(c.getInputStream());
            try {
                return new JSONObject(body).optString("text", "").trim();
            } catch (org.json.JSONException e) {
                throw new IOException("unerwartete antwort");
            }
        } finally {
            current = null;
            c.disconnect();
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
