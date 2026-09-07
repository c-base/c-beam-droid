package org.c_base.c_beam;

import android.app.Application;
import android.content.Context;

import org.c_base.c_beam.mqtt.MqttManager;

public class CbeamApplication extends Application {

    private static volatile Context appContext;
    private MqttManager connection;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
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
