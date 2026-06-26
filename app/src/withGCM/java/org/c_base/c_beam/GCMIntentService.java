package org.c_base.c_beam;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.c_base.c_beam.activity.NotificationActivity;
import org.c_base.c_beam.domain.C_beam;
import org.c_base.c_beam.util.NotificationsDataSource;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;


public class GCMIntentService extends FirebaseMessagingService {
    private static final String LOG_TAG = "c-beam";
	private static final Pattern ETA_PATTERN = Pattern.compile("^(.*) \\(([^\\)]*)\\)$");
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm", Locale.US);
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "cbeam_channel";

    private NotificationManager mNotificationManager;


    public GCMIntentService() {
		super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // The id of the channel.
        String id = "my_channel_01";

        // The user-visible name of the channel.
        CharSequence name = "c-beam";

        // The user-visible description of the channel.
        String description = "c-beam notifications";

        int importance = NotificationManager.IMPORTANCE_LOW;

        NotificationChannel mChannel = new NotificationChannel(id, name,importance);

        // Configure the notification channel.
        mChannel.setDescription(description);

        // mChannel.enableLights(true);
        // Sets the notification light color for notifications posted to this
        // channel, if the device supports this feature.
//        mChannel.setLightColor(Color.RED);
//
//        mChannel.enableVibration(true);
//        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

        // mNotificationManager.createNotificationChannel(mChannel);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this.
            //NotificationManager notificationManager = getSystemService(NotificationManager.class);
            mNotificationManager.createNotificationChannel(channel);
        }
    }


    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d("FCM", "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        final String regid = token;
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String username = sharedPref.getString(Settings.USERNAME, "bernd");

                C_beam c_beam = C_beam.getInstance();
                c_beam.call("fcm_update", "user", username, "regid", regid);
            }
        }).start();
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        // String title = message.getNotification().getTitle();
        // String text = message.getNotification().getBody();
        // String timestamp = new SimpleDateFormat("HH:mm").format(new Date(new Long(message.getSentTime()) * 1000));
        String title = message.getData().get("title");
        String text = message.getData().get("text");
        String timestamp = message.getData().get("timestamp");

        String notificationText;
        if (title.equals("now boarding")) {
            notificationText = timestamp + ": " + title + ": " + text;
        } else if (title.equals("ETA")) {
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
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent deleteIntent = new Intent(this, NotificationBroadcastReceiver.class);
        deleteIntent.setAction(NotificationBroadcastReceiver.ACTION_NOTIFICATION_CANCELLED);

        PendingIntent pendingDeleteIntent = PendingIntent.getBroadcast(this, 0, deleteIntent,
                PendingIntent.FLAG_IMMUTABLE
        //        PendingIntent.FLAG_CANCEL_CURRENT
        );

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
                    .setChannelId("cbeam_channel")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setStyle(style)
                    .build();

            mNotificationManager.notify(NOTIFICATION_ID, notification);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while creating notification", e);
        }

        dataSource.close();
    }
    /*
    private void sendEtaBroadcast(Context context, String text,  Date today) {
		Matcher matcher = ETA_PATTERN.matcher(text);
		if (matcher.matches()) {
			String member = matcher.group(1);
			String eta = matcher.group(2).replaceFirst("^(\\d{2})(\\d{2})$", "$1:$2");

			NotificationBroadcast.sendEtaBroadcast(context, member, eta, today);
		}
	}*/
}
