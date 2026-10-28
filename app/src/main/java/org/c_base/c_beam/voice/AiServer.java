package org.c_base.c_beam.voice;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.c_base.c_beam.Settings;

/**
 * Where the station's AI server and its knowledge base are and which model to ask. All
 * of it comes from the expert settings and applies whenever filled in; an empty field
 * means the default, except for the knowledge base's key, which has none.
 */
public final class AiServer {
    /**
     * OpenAI-compatible base; Whisper and the chat models hang off it. Crew network only.
     * Despite the path this is Lemonade (llama.cpp) mimicking Ollama's API, which is why
     * `gemma4:e4b` works as a model name although the model list calls it something else.
     */
    public static final String DEFAULT_BASE_URL = "https://ai.cbrp3.c-base.org/ollama/v1";

    private AiServer() {
    }

    public static String baseUrl(Context context) {
        String url = pref(context, Settings.AI_URL);
        if (url.isEmpty()) {
            url = DEFAULT_BASE_URL;
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public static String model(Context context) {
        String model = pref(context, Settings.AI_MODEL);
        return model.isEmpty() ? ChatModel.DEFAULT_MODEL : model;
    }

    /** Value of the knowledge-source setting that routes lookups through Sandy Strands. */
    public static final String SOURCE_SANDY = "sandy";

    /**
     * The station's knowledge source, or null when there is none: with Sandy Strands
     * selected it is Sandy's assistant; otherwise Qdrant directly, which needs an API key
     * (the server refuses requests without one), so without a key the model simply gets
     * no knowledge tool.
     */
    public static KnowledgeSource knowledge(Context context) {
        if (SOURCE_SANDY.equals(pref(context, Settings.KNOWLEDGE_SOURCE))) {
            String url = pref(context, Settings.SANDY_URL);
            boolean audio = PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(Settings.SANDY_AUDIO, false);
            return new SandyStrands(url.isEmpty() ? SandyStrands.DEFAULT_URL : url, audio);
        }
        String key = pref(context, Settings.RAG_KEY);
        if (key.isEmpty()) {
            return null;
        }
        String url = pref(context, Settings.RAG_URL);
        if (url.isEmpty()) {
            url = Knowledge.DEFAULT_URL;
        }
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return new Knowledge(baseUrl(context), url, key, pref(context, Settings.RAG_COLLECTION));
    }

    private static String pref(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String value = prefs.getString(key, "");
        return value == null ? "" : value.trim();
    }
}
