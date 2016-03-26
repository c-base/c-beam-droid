package org.c_base.c_beam;

import android.app.Application;
import android.content.Context;

import org.c_base.c_beam.mqtt.MqttManager;
import org.c_base.c_beam.settings.Settings;

public class CbeamApplication extends Application {

    private MqttManager connection;

    public static CbeamApplication getInstance(Context context) {
        return (CbeamApplication) context.getApplicationContext();
    }

    public MqttManager getMqttManager() {
        if (connection == null) {
            Settings settings = new Settings(this);
            connection = new MqttManager(this, settings);
        }

        return connection;
    }
}
