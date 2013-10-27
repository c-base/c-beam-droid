package org.c_base.c_beam;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.c_base.c_beam.extension.NotificationBroadcast;

public class NotificationBroadcastReceiver extends BroadcastReceiver {
//	GCMIntentService gcmIntentService = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        System.out.println(action);
        if (action.equals("notification_cancelled")) {
//        	NotificationActivity.clear();
            NotificationBroadcast.sendCancelledBroadcast(context);
        }
    }

//	public void setGcmIntentService(GCMIntentService gcmIntentService) {
//		this.gcmIntentService = gcmIntentService;
//	}

}
