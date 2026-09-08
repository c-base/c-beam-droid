package org.c_base.c_beam;

import android.app.Application;
import android.content.Context;
import android.preference.PreferenceManager;

import org.c_base.c_beam.mqtt.MqttManager;

public class CbeamApplication extends Application {

    private static volatile Context appContext;
    private MqttManager connection;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        // Make preferences.xml the single source of preference defaults. Without this a
        // default only took effect once the settings screen had been opened, and code
        // reading the preference directly saw its own, different fallback (the MQTT URI
        // was the case that mattered).
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    /**
     * The application context, for singletons that need preferences or system services
     * without holding on to an Activity. Null only before {@link #onCreate()} has run.
     */
    public static Context getAppContext() {
        return appContext;
    }

    public static CbeamApplication getInstance(Context context) {
        return (CbeamApplication) context.getApplicationContext();
    }

    public MqttManager getMqttManager() {
        if (connection == null) {
            connection = MqttManager.getInstance(this);
        }
        return connection;
    }
}
