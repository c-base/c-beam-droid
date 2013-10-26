package org.c_base.c_beam;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.c_base.c_beam.activity.NotificationActivity;
import org.c_base.c_beam.extension.NotificationBroadcast;
import org.c_base.c_beam.util.NotificationsDataSource;

import android.app.Notification;
import android.app.Notification.InboxStyle;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;


public class GCMIntentService extends GCMBaseIntentService {
	private static final Pattern ETA_PATTERN = Pattern.compile("^(.*) \\(([^\\)]*)\\)$");

	private static HashMap<String,Notification> notifications = new HashMap<String,Notification>();

	private NotificationsDataSource datasource;


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
		DateFormat df = new SimpleDateFormat("HH:mm");
		Date today = Calendar.getInstance().getTime();
		String timestamp = df.format(today);
		// TODO Auto-generated method stub
		String title = arg1.getExtras().get("title").toString();
		String text = arg1.getExtras().get("text").toString();

		String nuText = "";
		// mId allows you to update the notification later on.
		int id = (int) (Math.random()*10000000)+64;
		ArrayList<String> lines = new ArrayList<String>();
		if (title.equals("now boarding")) {
			NotificationBroadcast.sendBoardingBroadcast(context, text, today);

			id = 1;
			nuText = timestamp + ": " + title + ": " + text;
			text = nuText;
		} else if (title.equals("ETA")) {
			sendEtaBroadcast(context, today, text);

			id = 1;
			nuText = timestamp + ": " + title + " " + text;
			text = nuText;
		} else if (title.equals("mission completed")) {
			id = 1;
			nuText = timestamp + ": " + text;
			text = nuText;
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

		datasource = new NotificationsDataSource(this);
		datasource.open();
		datasource.createNotification(text);

		Intent intent = new Intent(this, NotificationActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

		Intent deleteIntent = new Intent(this, NotificationBroadcastReceiver.class);
		deleteIntent.setAction("notification_cancelled");
		deleteIntent.putExtra("org.c_base.c_beam.message_id", id);

		//		NotificationCompat.Builder mBuilder =
		//				new NotificationCompat.Builder(this)
		//		.setSmallIcon(R.drawable.ic_launcher)
		//		.setContentTitle("c-beam").setContentTitle(title)
		//		.setContentText(text);
		//
		//		mBuilder.setAutoCancel(true);
		//	    mBuilder.setTicker(text);
		//		mBuilder.setLights(0x00ffff00, 1000, 1000);
		//	    mBuilder.setDeleteIntent(PendingIntent.getBroadcast(this, 0, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT));
		//
		//		mBuilder.setContentIntent(pIntent);
		//		RemoteViews expandedView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.expanded_view);
		//		String boardingText = "";
		//		for(String line: NotificationActivity.getBoardingList()) {
		//			boardingText += line + "\n";
		//		}
		//		expandedView.setTextViewText(R.id.title, title);
		//		expandedView.setTextViewText(R.id.text, text);
		//		Notification notification = mBuilder.getNotification();
		//		notification.bigContentView = expandedView;
		//
		//		//notification.flags = Notification.DEFAULT_ALL;
		//		notification.flags = Notification.FLAG_SHOW_LIGHTS;
		//		notification.flags = Notification.DEFAULT_LIGHTS;

		try {
			InboxStyle style = new Notification.InboxStyle();
			//		ArrayList<org.c_base.c_beam.domain.Notification> notificationList = NotificationActivity.getNotificationList();
			ArrayList<org.c_base.c_beam.domain.Notification> notificationList = datasource.getAllNotifications();
			if (notificationList.size() > 5) {
				for(int i=0; i<5; i++) {
					style.addLine(notificationList.get(i).toString());
				}
				style.setSummaryText("+" + (notificationList.size() - 5)+" more...");
			} else {
				for(org.c_base.c_beam.domain.Notification line: notificationList) {
					style.addLine(line.toString());
				}
			}

			Notification notification = new Notification.Builder(getApplicationContext())
			.setContentTitle("c-beam")
			.setContentText(text)
			.setAutoCancel(true)
			.setSubText(null)
			.setTicker(text)
			.setDeleteIntent(PendingIntent.getBroadcast(this, 0, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT))
			.setContentIntent(pIntent)
			.setSmallIcon(R.drawable.ic_launcher)
			.setStyle(style)
			.build();

			notifications.put(title, notification);

			NotificationManager mNotificationManager =
					(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(id, notification);
		} catch (Exception e) {
			e.printStackTrace();
		}
		datasource.close();
	}

	private void sendEtaBroadcast(Context context, Date today, String text) {
		Matcher matcher = ETA_PATTERN.matcher(text);
		if (matcher.matches()) {
			String member = matcher.group(1);
			String eta = matcher.group(2).replaceFirst("^(\\d{2})(\\d{2})$", "$1:$2");

			NotificationBroadcast.sendEtaBroadcast(context, member, eta, today);
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
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
}
