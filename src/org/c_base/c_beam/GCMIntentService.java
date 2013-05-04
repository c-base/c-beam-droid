package org.c_base.c_beam;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.c_base.c_beam.activity.MainActivity;
import org.c_base.c_beam.activity.NotificationActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.gcm.GCMBaseIntentService;


public class GCMIntentService extends GCMBaseIntentService {
	private static HashMap<String,Notification> notifications = new HashMap<String,Notification>();
	private static String boardingCache = "";
	private static String etaCache = "";
	
	public GCMIntentService() {
		super("GCMIntentService");
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

		// mId allows you to update the notification later on.
		int id = (int) (Math.random()*10000000)+64;
		if (title.equals("now boarding")) {
			id = 1;
			NotificationActivity.addBoardingNotification(text);
//			if (boardingCache.equals("")) { 
//				boardingCache = text;
//			} else {
//				boardingCache = text + ", " + boardingCache;
//				text = boardingCache;
//			}
//			Log.i(TAG, boardingCache);
		} else if (title.equals("ETA")) {
			id = 2;
			NotificationActivity.addETANotification(text);
		} else if (title.equals("mission completed")) {
			id = 3;
			NotificationActivity.addMissionNotification(text);
		} else if (title.equals("AES")) {
			byte[] keyStart = "a1b2c3d4e5f6g7h8a1b2c3d4e5f6g7h8".getBytes();
			KeyGenerator kgen = null;
			SecureRandom sr = null;
			try {
				kgen = KeyGenerator.getInstance("AES");
				sr = SecureRandom.getInstance("SHA1PRNG");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			sr.setSeed(keyStart);
			kgen.init(256, sr); // 192 and 256 bits may not be available
			SecretKey skey = kgen.generateKey();
			byte[] key = skey.getEncoded();    
			try {
				byte[] decryptedData = decrypt(key,text.getBytes());
				Log.i("Decrypt", ""+decryptedData);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}

		Intent intent = new Intent(this, NotificationActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

		Intent deleteIntent = new Intent(this, NotificationBroadcastReceiver.class);
	    deleteIntent.setAction("notification_cancelled");
	    deleteIntent.putExtra("org.c_base.c_beam.message_id", id);

		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this)
		.setSmallIcon(R.drawable.ic_launcher)
		.setContentTitle(title)
		.setContentText(text);
		
		NotificationManager mNotificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

//		mBuilder.setAutoCancel(true);
//		mBuilder.setTicker(title+": "+text);
//		mBuilder.setLights(0x00ffff00, 1000, 1000);
//	    mBuilder.setDeleteIntent(PendingIntent.getBroadcast(this, 0, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT));
//	 
//		mBuilder.setContentIntent(pIntent);
//		RemoteViews expandedView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.expanded_view);
//		expandedView.setTextViewText(R.id.title, title);
//		expandedView.setTextViewText(R.id.text, text);
//		Notification notification = mBuilder.getNotification();
//		notification.bigContentView = expandedView;
//
//		//notification.flags = Notification.DEFAULT_ALL;
//		notification.flags = Notification.FLAG_SHOW_LIGHTS;
//		notification.flags = Notification.DEFAULT_LIGHTS;
		
		Notification notification = new Notification.Builder(getApplicationContext())
	     .setContentTitle(title)
	     .setContentText(text)
	     .setTicker(title+": "+text)
	     .setDeleteIntent(PendingIntent.getBroadcast(this, 0, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT))
	     .setContentIntent(pIntent)
	     .setSmallIcon(R.drawable.ic_launcher)
//	     .setLargeIcon(R.drawable.ic_launcher)
	     .setStyle(new Notification.InboxStyle()
	         .addLine(text)
//	         .addLine("bar")
//	         .setContentTitle("")
	         .setSummaryText(""))
	     .build();
		
		notifications.put(title, notification);

		mNotificationManager.notify(id, notification);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		System.out.println("bar");
	}

	private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		
		Log.i("encrypted", Base64.decode(encrypted, 0)+"");
		byte[] decrypted = cipher.doFinal(Base64.decode(encrypted, 0));
		return decrypted;
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
	
	public void clearMessages() {
		boardingCache = "";
		etaCache = "";
	}
	
}