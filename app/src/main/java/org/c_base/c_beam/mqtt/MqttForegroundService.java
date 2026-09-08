package org.c_base.c_beam.mqtt;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.net.Uri;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;
import androidx.core.content.ContextCompat;

import org.c_base.c_beam.R;
import org.c_base.c_beam.activity.MainActivity;

/**
 * Keeps the process, and with it the MQTT connection, alive while push is enabled. Only the
 * noGCM flavor declares this service in its manifest: the Play build receives push over FCM
 * and never starts it. The foreground service type is {@code specialUse}, as ntfy does it,
 * because the {@code dataSync} type is time-limited since Android 15 and would drop the
 * connection after a few hours.
 */
public class MqttForegroundService extends Service {

    private static final String LOG_TAG = "MqttForegroundService";
    private static final String CHANNEL_ID = "cbeam_mqtt_service";
    private static final int NOTIFICATION_ID = 2;
    private static final int RESTRICTED_NOTIFICATION_ID = 3;

    private MqttManager mqtt;

    public static void start(Context context) {
        ContextCompat.startForegroundService(context, new Intent(context, MqttForegroundService.class));
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, MqttForegroundService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
    }

    // FOREGROUND_SERVICE_TYPE_SPECIAL_USE is a compile-time constant (inlined), and
    // ServiceCompat ignores the type on API levels that do not know it.
    @SuppressLint("InlinedApi")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Must be called promptly after startForegroundService(), before anything that can block.
        ServiceCompat.startForeground(this, NOTIFICATION_ID, buildNotification(false),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);

        warnIfBackgroundRestricted();

        mqtt = MqttManager.getInstance(this);
        mqtt.setStateListener(this::updateNotification);
        mqtt.startConnection();
        Log.i(LOG_TAG, "started");
        return START_STICKY;
    }

    /**
     * With the battery setting "Restricted" the system stops this service as soon as the app
     * goes idle -- seen on a device as "Stopping service due to app idle" about a minute
     * after the screen went off. Nothing the app can do about it except tell the user, so
     * post a notification that opens the app's settings page.
     */
    private void warnIfBackgroundRestricted() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P || !am.isBackgroundRestricted()) {
            nm.cancel(RESTRICTED_NOTIFICATION_ID);
            return;
        }
        Log.w(LOG_TAG, "app is background restricted; the system will stop this service when idle");
        Intent settings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", getPackageName(), null));
        PendingIntent pending = PendingIntent.getActivity(this, 1, settings, PendingIntent.FLAG_IMMUTABLE);
        Notification n = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getString(R.string.mqtt_restricted_title))
                .setContentText(getString(R.string.mqtt_restricted_text))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.mqtt_restricted_text)))
                .setContentIntent(pending)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
        nm.notify(RESTRICTED_NOTIFICATION_ID, n);
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "stopping");
        if (mqtt != null) {
            mqtt.setStateListener(null);
            mqtt.stopConnection();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void updateNotification(boolean connected) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID, buildNotification(connected));
    }

    private Notification buildNotification(boolean connected) {
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(this, 0, open, PendingIntent.FLAG_IMMUTABLE);
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(connected
                        ? R.string.mqtt_service_connected : R.string.mqtt_service_connecting))
                .setContentIntent(pending)
                .setOngoing(true)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                getString(R.string.mqtt_service_channel), NotificationManager.IMPORTANCE_LOW);
        channel.setShowBadge(false);
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
    }
}
