package org.c_base.c_beam.boot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.c_base.c_beam.Settings;
import org.c_base.c_beam.mqtt.MqttManager;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            startMqttConnection(context);
        }
    }

    private void startMqttConnection(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean mqttEnabled = sharedPref.getBoolean(Settings.MQTT_ENABLED, false);
        if (mqttEnabled) {
            MqttManager mqttManager = MqttManager.getInstance(context);
            mqttManager.startConnection();
        }
    }
}
