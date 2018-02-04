package org.c_base.c_beam;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.android.gcm.GCMBaseIntentService;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.c_base.c_beam.activity.NotificationActivity;
import org.c_base.c_beam.extension.NotificationBroadcast;
import org.c_base.c_beam.util.NotificationsDataSource;


public class GCMIntentService extends GCMBaseIntentService {
    private static final String LOG_TAG = "c-beam";
	private static final Pattern ETA_PATTERN = Pattern.compile("^(.*) \\(([^\\)]*)\\)$");
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm", Locale.US);
    private static final int NOTIFICATION_ID = 1;

    private NotificationManager mNotificationManager;


    public GCMIntentService() {
		super("GCMIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
	protected void onError(Context context, String errorId) {
		Log.i("GCM Error", errorId);
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
        Date today = Calendar.getInstance().getTime();
        //String timestamp = DATE_FORMAT.format(today);

        String title = intent.getExtras().getString("title");
        String text = intent.getExtras().getString("text");
        String timestamp = intent.getExtras().getString("timestamp");

        String notificationText;
        if (title.equals("now boarding")) {
            NotificationBroadcast.sendBoardingBroadcast(context, text, today);
            notificationText = timestamp + ": " + title + ": " + text;
        } else if (title.equals("ETA")) {
            sendEtaBroadcast(context, text, today);
            notificationText = timestamp + ": " + title + " " + text;
        } else if (title.equals("mission completed")) {
            notificationText = timestamp + ": " + text;
        } else {
            Log.d(LOG_TAG, "Unknown notification message received: " + title);
            return;
        }

        createNotification(notificationText);
    }

    private void createNotification(String notificationText) {
        //TODO: don't access the database from the main thread
        NotificationsDataSource dataSource = new NotificationsDataSource(this);
        dataSource.open();
        dataSource.createNotification(notificationText);

        Intent notificationIntent = new Intent(this, NotificationActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent deleteIntent = new Intent(this, NotificationBroadcastReceiver.class);
        deleteIntent.setAction(NotificationBroadcastReceiver.ACTION_NOTIFICATION_CANCELLED);

        PendingIntent pendingDeleteIntent = PendingIntent.getBroadcast(this, 0, deleteIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        try {
            NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
            ArrayList<org.c_base.c_beam.domain.Notification> notificationList =
                    dataSource.getAllNotifications();

            if (notificationList.size() > 5) {
                for (int i = 0; i < 5; i++) {
                    style.addLine(notificationList.get(i).toString());
                }
                style.setSummaryText("+" + (notificationList.size() - 5) + " more...");
            } else {
                for(org.c_base.c_beam.domain.Notification line: notificationList) {
                    style.addLine(line.toString());
                }
            }

            Notification notification = new NotificationCompat.Builder(getApplicationContext())
                    .setContentTitle("c-beam")
                    .setContentText(notificationText)
                    .setAutoCancel(true)
                    .setSubText(null)
                    .setTicker(notificationText)
                    .setDeleteIntent(pendingDeleteIntent)
                    .setContentIntent(pIntent)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setStyle(style)
                    .build();

            mNotificationManager.notify(NOTIFICATION_ID, notification);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while creating notification", e);
        }

        dataSource.close();
    }

    private void sendEtaBroadcast(Context context, String text,  Date today) {
		Matcher matcher = ETA_PATTERN.matcher(text);
		if (matcher.matches()) {
			String member = matcher.group(1);
			String eta = matcher.group(2).replaceFirst("^(\\d{2})(\\d{2})$", "$1:$2");

			NotificationBroadcast.sendEtaBroadcast(context, member, eta, today);
		}
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		Log.i("GCM Registered", registrationId);
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
		Log.i("GCM Unregistered", registrationId);
	}
}
