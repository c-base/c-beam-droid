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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * The station's knowledge base: a Qdrant vector store with chunks of c-base lore and
 * documentation. A question is embedded with the same model on the AI server that the
 * store was filled with, the nearest chunks are fetched, and their text goes back to the
 * language model as a tool result, so that it answers from what the station actually
 * knows instead of making something up.
 *
 * Qdrant itself is not a RAG server: there is no "ask" endpoint, only vectors. The two
 * requests (embed, then query) run on a background thread; the collection's name and
 * vector layout are looked up once per process so that nothing about the store has to
 * be hard-coded here beyond the embedding model.
 */
public class Knowledge {
    private static final String TAG = "Knowledge";

    /** Qdrant's HTTP port on lazee-nerdserv; plain HTTP, crew network only. */
    public static final String DEFAULT_URL = "http://lazee-nerdserv.cbrp3.c-base.org:8123";
    /** Chunks of the dokuwiki, the coredump and the lore, 768-dimensional, cosine. */
    public static final String DEFAULT_COLLECTION = "cbase_knowledge";
    /**
     * The embedding model the store was built with. nomic-embed-text v1 wants a task
     * prefix in front of every input; queries get {@link #QUERY_PREFIX}.
     */
    public static final String EMBEDDING_MODEL = "nomic-embed-text-v1-GGUF";
    private static final String QUERY_PREFIX = "search_query: ";

    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 30000;
    /** Chunks handed to the model; each capped so five of them stay well under a page. */
    private static final int MAX_HITS = 5;
    private static final int MAX_CHUNK_CHARS = 700;
    /**
     * Below this cosine score a chunk is unrelated noise rather than a weak match. The
     * scores are compressed: with nomic v1 and German text, a chunk that answers the
     * question sits around 0.55, generic hub pages that match anything around 0.36 to 0.41.
     */
    private static final double MIN_SCORE = 0.40;

    /** Payload fields that commonly hold the chunk text, most specific first. */
    private static final String[] TEXT_KEYS = {
            "text", "content", "page_content", "chunk", "document", "body"};
    /** Payload fields that say where a chunk came from. */
    private static final String[] SOURCE_KEYS = {"title", "source", "url", "file", "path", "name"};

    public interface Callback {
        /** The chunks as one text for the model, or a short note that nothing was found. */
        void onResult(String text);
        void onError(String message);
    }

    /** What was learnt about the store; the same for every request, hence static. */
    private static class Layout {
        final String collection;
        /** Name of the dense vector when the collection uses named vectors, else null. */
        final String vectorName;
        final int size;

        Layout(String collection, String vectorName, int size) {
            this.collection = collection;
            this.vectorName = vectorName;
            this.size = size;
        }
    }

    private static volatile Layout layout;
    private static String layoutFor;

    private final String embeddingsUrl;
    private final String qdrantUrl;
    private final String apiKey;
    private final String collection;
    private final Handler main = new Handler(Looper.getMainLooper());
    private volatile HttpURLConnection current;
    private volatile boolean cancelled;

    /**
     * @param aiBaseUrl  OpenAI-compatible base of the AI server, for `/embeddings`
     * @param qdrantUrl  Qdrant's base URL, without trailing slash
     * @param apiKey     Qdrant's `api-key`; the server refuses everything without one
     * @param collection collection name, or empty for {@link #DEFAULT_COLLECTION}
     */
    public Knowledge(String aiBaseUrl, String qdrantUrl, String apiKey, String collection) {
        this.embeddingsUrl = aiBaseUrl + "/embeddings";
        this.qdrantUrl = qdrantUrl;
        this.apiKey = apiKey;
        this.collection = collection;
    }

    public void search(final String query, final Callback callback) {
        cancelled = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String result = lookup(query);
                    main.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!cancelled) {
                                callback.onResult(result);
                            }
                        }
                    });
                } catch (IOException e) {
                    Log.w(TAG, "knowledge lookup failed", e);
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
        }, "Knowledge").start();
    }

    public void cancel() {
        cancelled = true;
        HttpURLConnection c = current;
        if (c != null) {
            c.disconnect();
        }
    }

    private String lookup(String query) throws IOException {
        Log.d(TAG, "query: " + query);
        Layout l = layout(qdrantUrl + "|" + collection);
        float[] vector = embed(query);
        if (vector.length != l.size) {
            throw new IOException("wissensdatenbank: embedding passt nicht (" + vector.length
                    + " statt " + l.size + " dimensionen)");
        }
        return format(query(l, vector));
    }

    /** Finds the collection and its vector layout, once per process and configuration. */
    private Layout layout(String key) throws IOException {
        Layout l = layout;
        if (l != null && key.equals(layoutFor)) {
            return l;
        }
        String name = collection.isEmpty() ? DEFAULT_COLLECTION : collection;
        try {
            JSONObject result = new JSONObject(get(qdrantUrl + "/collections/"
                    + URLEncoder.encode(name, "UTF-8"))).getJSONObject("result");
            JSONObject vectors = result.getJSONObject("config").getJSONObject("params")
                    .getJSONObject("vectors");
            String vectorName = null;
            if (!vectors.has("size")) {
                // Named vectors: pick the first dense one (sparse ones live elsewhere).
                Iterator<String> names = vectors.keys();
                if (!names.hasNext()) {
                    throw new IOException("wissensdatenbank: collection ohne vektoren");
                }
                vectorName = names.next();
                vectors = vectors.getJSONObject(vectorName);
            }
            l = new Layout(name, vectorName, vectors.getInt("size"));
        } catch (JSONException e) {
            throw new IOException("wissensdatenbank: unerwartete antwort");
        }
        layoutFor = key;
        layout = l;
        return l;
    }

    private float[] embed(String text) throws IOException {
        JSONObject body = new JSONObject();
        try {
            body.put("model", EMBEDDING_MODEL);
            body.put("input", QUERY_PREFIX + text);
        } catch (JSONException e) {
            throw new IOException(e);
        }
        String response = post(embeddingsUrl, body, false);
        try {
            JSONArray values = new JSONObject(response).getJSONArray("data").getJSONObject(0)
                    .getJSONArray("embedding");
            float[] vector = new float[values.length()];
            for (int i = 0; i < vector.length; i++) {
                vector[i] = (float) values.getDouble(i);
            }
            return vector;
        } catch (JSONException e) {
            throw new IOException("embedding: unerwartete antwort");
        }
    }

    private JSONArray query(Layout l, float[] vector) throws IOException {
        JSONObject body = new JSONObject();
        try {
            JSONArray values = new JSONArray();
            for (float v : vector) {
                values.put(v);
            }
            body.put("query", values);
            if (l.vectorName != null) {
                body.put("using", l.vectorName);
            }
            body.put("limit", MAX_HITS);
            body.put("score_threshold", MIN_SCORE);
            body.put("with_payload", true);
            body.put("with_vector", false);
        } catch (JSONException e) {
            throw new IOException(e);
        }
        String response = post(qdrantUrl + "/collections/"
                + URLEncoder.encode(l.collection, "UTF-8") + "/points/query", body, true);
        try {
            JSONArray points = new JSONObject(response).getJSONObject("result").getJSONArray("points");
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                StringBuilder b = new StringBuilder(points.length() + " hits:");
                for (int i = 0; i < points.length(); i++) {
                    JSONObject point = points.getJSONObject(i);
                    b.append(String.format(Locale.ROOT, " %.3f ", point.optDouble("score")))
                            .append(sourceOf(point.optJSONObject("payload")));
                }
                Log.d(TAG, b.toString());
            }
            return points;
        } catch (JSONException e) {
            throw new IOException("wissensdatenbank: unerwartete antwort");
        }
    }

    /** One block per chunk, source first, for the model to quote from. */
    static String format(JSONArray points) {
        List<String> blocks = new ArrayList<String>();
        for (int i = 0; i < points.length(); i++) {
            JSONObject payload = points.optJSONObject(i) == null ? null
                    : points.optJSONObject(i).optJSONObject("payload");
            if (payload == null) {
                continue;
            }
            String text = textOf(payload);
            if (text.isEmpty()) {
                continue;
            }
            if (text.length() > MAX_CHUNK_CHARS) {
                text = text.substring(0, MAX_CHUNK_CHARS - 1) + "…";
            }
            String source = sourceOf(payload);
            blocks.add((source.isEmpty() ? "" : "[" + source + "]\n") + text);
        }
        if (blocks.isEmpty()) {
            return "nichts gefunden: die wissensdatenbank enthält nichts dazu";
        }
        // The similarity scores are too compressed to filter on, so the model gets told
        // what it is looking at; without this it once invented a CNC mill out of chunks
        // about the server landscape.
        StringBuilder b = new StringBuilder("Fundstellen der Ähnlichkeitssuche, die passendste "
                + "zuerst; unpassende ignorieren, nichts hinzuerfinden:");
        for (String block : blocks) {
            b.append("\n\n").append(block);
        }
        return b.toString();
    }

    /** The chunk text: a known field if there is one, otherwise the longest string. */
    private static String textOf(JSONObject payload) {
        for (String key : TEXT_KEYS) {
            String s = payload.optString(key, "").trim();
            if (!s.isEmpty()) {
                return s;
            }
        }
        String longest = "";
        Iterator<String> keys = payload.keys();
        while (keys.hasNext()) {
            String s = payload.optString(keys.next(), "").trim();
            if (s.length() > longest.length()) {
                longest = s;
            }
        }
        return longest;
    }

    private static String sourceOf(JSONObject payload) {
        if (payload == null) {
            return "";
        }
        JSONObject meta = payload.optJSONObject("metadata");
        for (String key : SOURCE_KEYS) {
            String s = payload.optString(key, "").trim();
            if (s.isEmpty() && meta != null) {
                s = meta.optString(key, "").trim();
            }
            if (!s.isEmpty()) {
                return s.length() > 80 ? s.substring(0, 79) + "…" : s;
            }
        }
        return "";
    }

    private String get(String url) throws IOException {
        return exchange(url, null, true);
    }

    private String post(String url, JSONObject body, boolean qdrant) throws IOException {
        return exchange(url, body.toString().getBytes(StandardCharsets.UTF_8), qdrant);
    }

    private String exchange(String url, byte[] payload, boolean qdrant) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        current = c;
        try {
            c.setConnectTimeout(CONNECT_TIMEOUT_MS);
            c.setReadTimeout(READ_TIMEOUT_MS);
            c.setRequestProperty("Accept", "application/json");
            if (qdrant) {
                c.setRequestProperty("api-key", apiKey);
            }
            if (payload != null) {
                c.setRequestMethod("POST");
                c.setDoOutput(true);
                c.setFixedLengthStreamingMode(payload.length);
                c.setRequestProperty("Content-Type", "application/json");
                OutputStream out = c.getOutputStream();
                out.write(payload);
                out.flush();
            }
            int code = c.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                String error = readAll(c.getErrorStream());
                Log.w(TAG, "HTTP " + code + " from " + url + ": " + error);
                if (qdrant && (code == 401 || code == 403)) {
                    throw new IOException("wissensdatenbank: zugriff verweigert, api-key prüfen");
                }
                if (qdrant && code == 404) {
                    throw new IOException("wissensdatenbank: collection nicht gefunden");
                }
                throw new IOException((qdrant ? "wissensdatenbank" : "embedding") + ": HTTP " + code);
            }
            return readAll(c.getInputStream());
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
