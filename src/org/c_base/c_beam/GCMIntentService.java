package org.c_base.c_beam;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gcm.GCMBaseIntentService;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;


public class 	GCMIntentService extends GCMBaseIntentService {
	HashMap<String,Notification> notifications;
	public GCMIntentService() {
		super("GCMIntentService");
		notifications = new HashMap<String,Notification>();
	}

	@Override
	protected void onError(Context context, String arg1) {
		// TODO Auto-generated method stub
		Log.i("GCM Error", arg1);
	}

	@Override
	protected void onMessage(Context context, Intent arg1) {
		// TODO Auto-generated method stub
		String title = arg1.getExtras().get("title").toString();
		String text = arg1.getExtras().get("text").toString();

		//notifications.get("foo").;
		
		Log.i("GCM Message", arg1.getExtras().get("title").toString());
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this)
		.setSmallIcon(R.drawable.ic_launcher)
		.setContentTitle(arg1.getExtras().get("title").toString())
		.setContentText(arg1.getExtras().get("text").toString());

		NotificationManager mNotificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		int id = (int) (Math.random()*10000000)+64;
		if (title.equals("now boarding")) {
			id = 1;
			
		} else if (title.equals("ETA")) {
			id = 2;
		}
		
		Log.i("id", ""+id);
		//mBuilder.setNumber(42);
		mBuilder.setAutoCancel(true);
		long[] l = new long[2];
		l[0] = 500;
		l[1] = 250;
		//mBuilder.setVibrate(l);
		mBuilder.setTicker(title+": "+text);
		mBuilder.setLights(0x00ffff00, 1000, 1000);
		//mBuilder.setSubText("subtext");
		//mBuilder.setSmallIcon(R.drawable.ic_launcher);
		//Intent intent = new Intent(this, NotificationReceiverActivity.class);
	    //PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
	    
		//mBuilder.setDeleteIntent(pIntent);
		Notification notification = mBuilder.build();
		notifications.put("foo", notification);
		//notification.flags = Notification.DEFAULT_ALL;
		notification.flags = Notification.FLAG_SHOW_LIGHTS;
		notification.flags = Notification.DEFAULT_LIGHTS;
		
		mNotificationManager.notify(id, notification);
	}

	@Override
	protected void onRegistered(Context context, String arg1) {
		// TODO Auto-generated method stub
		Log.i("GCM Reistered", arg1);
	}

	@Override
	protected void onUnregistered(Context context, String arg1) {
		// TODO Auto-generated method stub
		Log.i("GCM Unreg", arg1);
	} 
}