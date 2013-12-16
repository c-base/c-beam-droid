package org.c_base.c_beam;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.c_base.c_beam.extension.NotificationBroadcast;

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_NOTIFICATION_CANCELLED = "notification_cancelled";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_NOTIFICATION_CANCELLED.equals(action)) {
            NotificationBroadcast.sendCancelledBroadcast(context);
        }
    }
}
