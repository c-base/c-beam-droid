package org.c_base.c_beam.voice;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.c_base.c_beam.Settings;

/**
 * Where the station's AI server is and which model to ask. Both come from the expert
 * settings and apply whenever they are filled in; an empty field means the default.
 */
public final class AiServer {
    /** OpenAI-compatible base; Whisper and the chat models hang off it. Crew network only. */
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

    private static String pref(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String value = prefs.getString(key, "");
        return value == null ? "" : value.trim();
    }
}
