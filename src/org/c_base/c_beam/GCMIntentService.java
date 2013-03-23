package org.c_base.c_beam;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.c_base.c_beam.activity.MainActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;


public class GCMIntentService extends GCMBaseIntentService {
	HashMap<String,Notification> notifications;
	String boardingCache = "";
	
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
			boardingCache += text + "\n";
			Log.i(TAG, boardingCache);
		} else if (title.equals("ETA")) {
			id = 2;
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

		Log.i("id", ""+id);
		mBuilder.setAutoCancel(true);
//		long[] l = new long[2];
//		l[0] = 500;
//		l[1] = 250;
//		mBuilder.setVibrate(l);
		mBuilder.setTicker(title+": "+text);
		mBuilder.setLights(0x00ffff00, 1000, 1000);
//		mBuilder.setSubText("subtext");
//		mBuilder.setSmallIcon(R.drawable.ic_launcher);
		Intent intent = new Intent(this, MainActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

		//mBuilder.setDeleteIntent(pIntent);
		mBuilder.setContentIntent(pIntent);
		Notification notification = mBuilder.getNotification();
		notifications.put("foo", notification);
		//notification.flags = Notification.DEFAULT_ALL;
		notification.flags = Notification.FLAG_SHOW_LIGHTS;
		notification.flags = Notification.DEFAULT_LIGHTS;

		mNotificationManager.notify(id, notification);

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