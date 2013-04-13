package org.c_base.c_beam;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationBroadcastReceiver extends BroadcastReceiver {
	GCMIntentService gcmIntentService = null;
	
	@Override
    public void onReceive(Context context, Intent intent)
    {	
        String action = intent.getAction();
        if(action.equals("notification_cancelled"))
        {
            System.out.println("fooooo");

    		System.out.println(intent.getExtras().getInt("org.c_base.c_beam.message_id"));
    		
        }
    }

}
