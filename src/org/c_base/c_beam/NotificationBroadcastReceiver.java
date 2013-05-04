package org.c_base.c_beam;

import org.c_base.c_beam.activity.NotificationActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationBroadcastReceiver extends BroadcastReceiver {
	GCMIntentService gcmIntentService = null;
	
	@Override
    public void onReceive(Context context, Intent intent)
    {	
        String action = intent.getAction();
        System.out.println(action);
        if(action.equals("notification_cancelled"))
        {
        	NotificationActivity.clear();
//        	gcmIntentService.clearMessages();
    		System.out.println(intent.getExtras().getInt("org.c_base.c_beam.message_id"));
        }
    }
	
	public void setGcmIntentService(GCMIntentService gcmIntentService) {
		this.gcmIntentService = gcmIntentService;
	}

}
