package org.c_base.c_beam.boot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.c_base.c_beam.CbeamApplication;
import org.c_base.c_beam.GCMFacade;
import org.c_base.c_beam.Settings;

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
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        // "push" is the user-facing switch. On noGCM it starts the MQTT foreground service,
        // which BOOT_COMPLETED may launch (specialUse is not on the restricted-type list);
        // on withGCM it refreshes the FCM registration.
        if (sharedPref.getBoolean(Settings.PUSH, false)) {
            GCMFacade.setupGCM(context);
        }
        // The expert "MQTT" switch: a bare connection with nothing keeping the process alive.
        if (sharedPref.getBoolean(Settings.MQTT_ENABLED, false)) {
            CbeamApplication.getInstance(context).getMqttManager().startConnection();
        }
    }
}
