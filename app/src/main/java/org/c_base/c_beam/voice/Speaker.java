package org.c_base.c_beam.voice;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;

import org.c_base.c_beam.Settings;

import java.util.Locale;
import java.util.Set;

/**
 * Reads c-beam's answers aloud on the phone with Android's own text-to-speech, using only
 * a German voice that synthesises on the device, so the answer never reaches a cloud
 * service. Optional: the caller checks the setting; this class only says whether a usable
 * voice exists.
 *
 * The station's names are English words that a German voice reads letter by letter, so
 * they are respelled the way a German reader would say the English before speaking. The
 * respelling sits in the expert settings because what sounds right depends on the voice
 * installed, and trying variants by ear beats guessing.
 */
public class Speaker {
    private static final String TAG = "Speaker";

    public static final String DEFAULT_CBASE = "Sieh Behs";
    public static final String DEFAULT_CBEAM = "Sieh Biehm";

    private final Context context;
    private final TextToSpeech tts;
    private boolean ready;
    private boolean germanVoice;

    public Speaker(Context context) {
        this.context = context.getApplicationContext();
        tts = new TextToSpeech(this.context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.SUCCESS) {
                    Log.w(TAG, "text-to-speech not available: " + status);
                    return;
                }
                ready = true;
                germanVoice = pickGermanOfflineVoice();
            }
        });
    }

    /** True once the engine is up and a German on-device voice was found. */
    public boolean isAvailable() {
        return ready && germanVoice;
    }

    /** True if the engine started but has no German voice; the user needs to install one. */
    public boolean lacksGermanVoice() {
        return ready && !germanVoice;
    }

    public void speak(String text) {
        if (!isAvailable() || text == null || text.trim().isEmpty()) {
            return;
        }
        Bundle params = new Bundle();
        params.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC);
        tts.speak(respell(text), TextToSpeech.QUEUE_FLUSH, params, "c-beam");
    }

    /** Swaps the station's names for their respellings; read from the settings each time. */
    private String respell(String text) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return respell(text, pref(prefs, Settings.VOICE_SAY_CBASE, DEFAULT_CBASE),
                pref(prefs, Settings.VOICE_SAY_CBEAM, DEFAULT_CBEAM));
    }

    static String respell(String text, String cbase, String cbeam) {
        return text.replaceAll("(?i)\\bc-base\\b", java.util.regex.Matcher.quoteReplacement(cbase))
                .replaceAll("(?i)\\bc-beam\\b", java.util.regex.Matcher.quoteReplacement(cbeam));
    }

    private static String pref(SharedPreferences prefs, String key, String fallback) {
        String value = prefs.getString(key, "");
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    /** Cuts the voice off, e.g. because the microphone is about to open. */
    public void stop() {
        if (ready) {
            tts.stop();
        }
    }

    public void shutdown() {
        ready = false;
        tts.shutdown();
    }

    private boolean pickGermanOfflineVoice() {
        Set<Voice> voices;
        try {
            voices = tts.getVoices();
        } catch (Exception e) {
            voices = null;
        }
        Voice best = null;
        if (voices != null) {
            for (Voice v : voices) {
                if (!"de".equals(v.getLocale().getLanguage()) || v.isNetworkConnectionRequired()) {
                    continue;
                }
                if (best == null || v.getQuality() > best.getQuality()) {
                    best = v;
                }
            }
        }
        if (best != null) {
            return tts.setVoice(best) == TextToSpeech.SUCCESS;
        }
        // Engines that do not enumerate voices may still speak German when asked for it.
        int result = tts.setLanguage(Locale.GERMAN);
        return result == TextToSpeech.LANG_AVAILABLE
                || result == TextToSpeech.LANG_COUNTRY_AVAILABLE
                || result == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE;
    }
}
