package org.c_base.c_beam.voice;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * The language models the AI server currently serves, from its OpenAI-compatible
 * `/models` route. Embedding models are left out: they cannot chat. The ids are what the
 * server wants in a chat request's `model` field (verified against Lemonade: the id from
 * this list and the Ollama-style alias both reach the same model).
 */
public final class ModelCatalog {
    private static final String TAG = "ModelCatalog";
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 10000;

    public static final class Model {
        public final String id;
        /** Server-side tags such as "coding", "tool-calling", "vision", "hot". */
        public final List<String> labels;

        Model(String id, List<String> labels) {
            this.id = id;
            this.labels = labels;
        }

        /** Something to show next to the id; the labels Lemonade attaches are terse. */
        public String hint() {
            List<String> parts = new ArrayList<String>();
            if (labels.contains("coding")) {
                parts.add("coding");
            }
            if (labels.contains("vision")) {
                parts.add("vision");
            }
            if (labels.contains("tool-calling")) {
                parts.add("tools");
            }
            StringBuilder sb = new StringBuilder();
            for (String p : parts) {
                sb.append(sb.length() == 0 ? "" : ", ").append(p);
            }
            return sb.toString();
        }
    }

    private ModelCatalog() {
    }

    /**
     * Blocking; call off the main thread. Returns an empty list when the server cannot be
     * reached or answers something unexpected, so the caller can fall back to a built-in
     * list rather than fail.
     */
    public static List<Model> fetch(String baseUrl) {
        List<Model> models = new ArrayList<Model>();
        HttpURLConnection c = null;
        try {
            c = (HttpURLConnection) new URL(baseUrl + "/models").openConnection();
            c.setConnectTimeout(CONNECT_TIMEOUT_MS);
            c.setReadTimeout(READ_TIMEOUT_MS);
            c.setRequestProperty("Accept", "application/json");
            int status = c.getResponseCode();
            if (status != 200) {
                Log.w(TAG, "/models answered HTTP " + status);
                return models;
            }
            JSONObject body = new JSONObject(readAll(c.getInputStream()));
            JSONArray data = body.getJSONArray("data");
            for (int i = 0; i < data.length(); i++) {
                JSONObject m = data.getJSONObject(i);
                String id = m.optString("id", "");
                if (id.isEmpty()) {
                    continue;
                }
                List<String> labels = new ArrayList<String>();
                JSONArray tags = m.optJSONArray("labels");
                for (int j = 0; tags != null && j < tags.length(); j++) {
                    labels.add(tags.optString(j, ""));
                }
                if (labels.contains("embeddings") || id.toLowerCase().contains("embed")) {
                    continue;
                }
                models.add(new Model(id, labels));
            }
        } catch (Exception e) {
            Log.w(TAG, "fetching /models failed: " + e.getMessage());
            models.clear();
        } finally {
            if (c != null) {
                c.disconnect();
            }
        }
        return models;
    }

    private static String readAll(InputStream in) throws java.io.IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }
}
