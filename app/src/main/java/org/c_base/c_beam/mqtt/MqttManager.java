package org.c_base.c_beam.mqtt;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.activity.MainActivity;
import org.c_base.c_beam.util.NotificationsDataSource;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class MqttManager implements MqttCallback {
	private static final String TAG = "MqttManager";
	private static final String BOARDING_TOPIC = "user/boarding";
	private static final String ETA_TOPIC = "user/eta";
	private static final String MISSION_TOPIC = "mission/completed";
	private static final String CHANNEL_ID = "c_beam_mqtt_channel";

	private final Context context;
	private MqttAndroidClient mqttClient;
	private final SharedPreferences sharedPref;

	private static MqttManager instance = null;

	private MqttManager(Context context) {
		this.context = context.getApplicationContext();
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context);
		createNotificationChannel();
	}

	public static synchronized MqttManager getInstance(Context context) {
		if (instance == null) {
			instance = new MqttManager(context);
		}
		return instance;
	}

	public void startConnection() {
		String uri = sharedPref.getString(Settings.MQTT_URI, "tcp://c-beam.cbrp3.c-base.org:1883");
		String clientId = sharedPref.getString(Settings.MQTT_ID, "c-beam-droid-" + System.currentTimeMillis());

		mqttClient = new MqttAndroidClient(context, uri, clientId);
		mqttClient.setCallback(this);

		MqttConnectOptions options = new MqttConnectOptions();
		options.setAutomaticReconnect(true);
		options.setCleanSession(false);

		try {
			mqttClient.connect(options, null, new IMqttActionListener() {
				@Override
				public void onSuccess(IMqttToken asyncActionToken) {
					subscribeToTopics();
				}

				@Override
				public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
					Log.e(TAG, "Connection failed", exception);
				}
			});
		} catch (MqttException e) {
			Log.e(TAG, "MqttException on connect", e);
		}
	}

	private void subscribeToTopics() {
		try {
			mqttClient.subscribe(BOARDING_TOPIC, 0);
			mqttClient.subscribe(ETA_TOPIC, 0);
			mqttClient.subscribe(MISSION_TOPIC, 0);
		} catch (MqttException e) {
			Log.e(TAG, "Subscription failed", e);
		}
	}

	@Override
	public void connectionLost(Throwable cause) {
		Log.w(TAG, "Connection lost", cause);
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) {
		String payload = new String(message.getPayload());
		Log.d(TAG, "Message arrived. Topic: " + topic + ", Payload: " + payload);

		String notificationTitle = "";
        if (topic != null) {
            switch (topic) {
                case BOARDING_TOPIC:
                    notificationTitle = "Boarding";
                    break;
                case ETA_TOPIC:
                    notificationTitle = "ETA Update";
                    break;
                case MISSION_TOPIC:
                    notificationTitle = "Mission Completed";
                    break;
            }
        }

		if (!notificationTitle.isEmpty()) {
			showNotification(notificationTitle, payload);
			saveNotification(payload);
		}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
	}

	public void crewNetworkConnected() {
		if (mqttClient != null && !mqttClient.isConnected()) {
			startConnection();
		}
	}

	public void crewNetworkDisconnected() {
		if (mqttClient != null && mqttClient.isConnected()) {
			try {
				mqttClient.disconnect();
			} catch (MqttException e) {
				Log.e(TAG, "Disconnect failed", e);
			}
		}
	}

	@SuppressLint("MissingPermission")
    private void showNotification(String title, String message) {
		Intent intent = new Intent(context, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(title)
				.setContentText(message)
				.setPriority(NotificationCompat.PRIORITY_DEFAULT)
				.setContentIntent(pendingIntent)
				.setAutoCancel(true);

		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
			notificationManager.notify((int) System.currentTimeMillis(), builder.build());
		}
	}

	private void saveNotification(String message) {
		new Thread(() -> {
            NotificationsDataSource dataSource = new NotificationsDataSource(context);
            dataSource.open();
            dataSource.createNotification(message);
            dataSource.close();
        }).start();
	}

	private void createNotificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = "c-beam Notifications";
			String description = "Mqtt notifications for c-beam";
			int importance = NotificationManager.IMPORTANCE_DEFAULT;
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
			channel.setDescription(description);
			NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
			if (notificationManager != null) {
				notificationManager.createNotificationChannel(channel);
			}
		}
	}
}
