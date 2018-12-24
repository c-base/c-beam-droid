package org.c_base.c_beam.boot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;

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
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            startMqttConnection(context);
        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            /*
            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            boolean hasConnectivity = !noConnectivity;
            if (hasConnectivity) {
                boolean mIsOnline = C_beam.getInstance().isInCrewNetwork();
                if (mIsOnline) {
                    //startMqttConnection(context);
                } else {
                    //startMqttConnectionExt(context);
                }
            }
            */
        } else {
            throw new UnsupportedOperationException("Unsupported action: " + action);
        }
    }

    private void startMqttConnectionExt(Context context) {
        CbeamApplication app = CbeamApplication.getInstance(context);
        MqttManager connection = app.getMqttManager();
        connection.startConnectionExt();
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
