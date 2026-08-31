package org.c_base.c_beam.boot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.c_base.c_beam.CbeamApplication;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.mqtt.MqttManager;

public class BootReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "BootReceiver";

    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // The receiver has to stay exported so the system can deliver BOOT_COMPLETED, which also
        // lets any installed app hand it an explicit intent carrying an arbitrary action. Throwing
        // on those made the crash remotely triggerable, so unexpected actions are now ignored.
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Log.w(LOG_TAG, "ignoring unexpected action: " + action);
            return;
        }

        startMqttConnection(context);
    }

    private void startMqttConnection(Context context) {
        CbeamApplication app = CbeamApplication.getInstance(context);
        MqttManager connection = app.getMqttManager();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean mqttEnabled = sharedPref.getBoolean(Settings.MQTT_ENABLED, false);
        if (mqttEnabled) {
            connection.startConnection();
        }
    }
}
