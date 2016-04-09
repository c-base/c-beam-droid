package org.c_base.c_beam.mqtt;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.c_base.c_beam.NotificationBroadcastReceiver;
import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.activity.NotificationActivity;
import org.c_base.c_beam.domain.Notification;
import org.c_base.c_beam.extension.NotificationBroadcast;
import org.c_base.c_beam.util.NotificationsDataSource;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocketFactory;

public class MqttManager implements MqttCallback, IMqttActionListener {

    private static final String LOG_TAG = "MqttManager";
    private static final int QOS = 2;
    private static final String CHANNEL = "c-beam-droid";
    private static final String OPEN_URL_TOPIC = "open";
    private static final String CLIENT_ID_PREFIX = "c-beam-droid-";

    private static final Pattern ETA_PATTERN = Pattern.compile("^(.*) \\(([^\\)]*)\\)$");
    private static final int NOTIFICATION_ID = 1;
    private static final String DEFAULT_MQTT_URI = "ssl://echelon.c-base.org:1883";
    private static final boolean DEFAULT_MQTT_TLS = false;
    private static final String DEFAULT_MQTT_USERNAME = "";
    private static final String DEFAULT_MQTT_PASSWORD = "";
    private static final String DEFAULT_MQTT_ID = "c-beam-droid-bernd01";

    private static boolean ready = false;
    private static SharedPreferences sharedPref;

    private final Context context;
    private MqttAndroidClient client;
    private NotificationManager mNotificationManager;

    private static Connections connections;
    private String clientHandle;
    private Thread thread;

    private static MqttManager instance = null;

    // TODO: remove workaround or bug in paho 1.0.2 https://github.com/eclipse/paho.mqtt.android/issues/2
    private static boolean subscribed = false;

    public static MqttManager getInstance(Context context) {


        if (instance == null) {
            instance = new MqttManager(context);
        }
        return instance;
    }

    private MqttManager(Context context) {
        this.context = context;

        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        connections = Connections.getInstance(context);
    }

    public void startConnection() {
        if (client != null) {
            Log.i(LOG_TAG, "Reuse client");
            if (!client.isConnected()) {
                Log.i(LOG_TAG, "Try reconnecting");
                connect();
            }
            return;
        }
        MqttConnectOptions options = createMqttConnectOptions();
        client = createMqttClient(options);
        client.setCallback(this);
        connect();
    }

    private void connect() {
        if (client.isConnected()) {
            Log.i(LOG_TAG, "Client already connected");
            return;
        }
        try {
            client.connect(Connections.getInstance(context).getConnection(clientHandle).getConnectionOptions(), null, this);
            Log.i(LOG_TAG, "Connect seems successful");
        } catch (MqttException e) {
            Log.e(LOG_TAG, "Error while connecting to server", e);
        }
    }

    private MqttAndroidClient createMqttClient(MqttConnectOptions options) {
        String serverUri = sharedPref.getString(Settings.MQTT_URI, DEFAULT_MQTT_URI);
        String clientId = sharedPref.getString(Settings.MQTT_ID, DEFAULT_MQTT_ID);//CLIENT_ID_PREFIX + UUID.randomUUID();
        Log.e(LOG_TAG, "ServerURI:" + serverUri);

        MqttAndroidClient client = connections.createClient(context, serverUri, clientId);

        clientHandle = serverUri + clientId;
        Connection connection = new Connection(clientHandle, clientId, "c-beam.cbrp3.c-base.org", 1883,
                context, client, sharedPref.getBoolean(Settings.MQTT_TLS, DEFAULT_MQTT_TLS));

        connection.addConnectionOptions(options);
        Connections.getInstance(context).addConnection(connection);
        return client;
    }

    private MqttConnectOptions createMqttConnectOptions() {
        String userName = sharedPref.getString(Settings.MQTT_USERNAME, DEFAULT_MQTT_USERNAME);
        String password = sharedPref.getString(Settings.MQTT_PASSWORD, DEFAULT_MQTT_PASSWORD);
        boolean useTLS = sharedPref.getBoolean(Settings.MQTT_TLS, DEFAULT_MQTT_TLS);
        MqttConnectOptions options = new MqttConnectOptions();
        if (!TextUtils.isEmpty(userName)) {
            options.setUserName(userName);
        }
        if (!TextUtils.isEmpty(password)) {
            options.setPassword(password.toCharArray());
        }
        if (useTLS) {
            try {
                InputStream certificateInputStream = getCaCertFromResources();
                SSLSocketFactory socketFactory = SslUtil.getSocketFactory(certificateInputStream);
                options.setSocketFactory(socketFactory);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        options.setCleanSession(false);
        return options;
    }

    private void subscribe() {
        Log.i(LOG_TAG, "Subscribe called");
        if (subscribed) {
            return;
        }
        String topic = getTopic(OPEN_URL_TOPIC);
        try {
            client.subscribe(topic, QOS);
            client.subscribe("user/boarding", QOS);
            client.subscribe("test/smile", QOS);
            subscribed = true;
            Log.i(LOG_TAG, "Subscribed successfully");
        } catch (MqttException e) {
            Log.e(LOG_TAG, "Failed to subscribe", e);
        }
    }

    public void crewNetworkConnected() {
        if (!ready || !client.isConnected()) {
            return;
        }
        try {
            client.subscribe("bar/status", QOS);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to subscribe to bar/status", e);
        }
    }

    public void crewNetworkDisconnected() {
        if (!ready || !client.isConnected()) {
            return;
        }
        try {
            client.unsubscribe("bar/status");
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to unsubscribe from bar/status", e);
        }
    }

    @Override
    public void connectionLost(final Throwable throwable) {
        Log.w(LOG_TAG, "Connection lost, will reconnect when network is available"); //, throwable);
    }

    @Override
    public void messageArrived(final String topic, final MqttMessage mqttMessage) throws Exception {
        Log.d(LOG_TAG, "Message arrived: " + topic);
        String payload = new String(mqttMessage.getPayload(), "UTF-8");
        onMessage(topic, payload);
    }

    @Override
    public void deliveryComplete(final IMqttDeliveryToken deliveryToken) {
        Log.d(LOG_TAG, "Delivery complete");
    }

    @Override
    public void onSuccess(final IMqttToken token) {
        ready = true;
        Log.i(LOG_TAG, "Connection successfully established.");
        subscribe();
    }

    @Override
    public void onFailure(final IMqttToken token, final Throwable throwable) {
        showErrorMessage(R.string.connection_to_server_failed);
        Log.e(LOG_TAG, "Connection failed" + throwable.getCause());
        throwable.printStackTrace();
    }

    private void showErrorMessage(int errorMessageResId) {
        String text = context.getString(errorMessageResId);
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.show();
    }

    private String getTopic(String subTopic) {
        return CHANNEL + "/" + subTopic;
    }

    private InputStream getCaCertFromResources() {
        return context.getResources().openRawResource(R.raw.cacert);
    }


    protected void onMessage(String topic, String payload) {
        String title = "";
        JSONObject json = null;
        try {
            String notificationText;
            if (topic.equals("user/boarding")) {
                json = new JSONObject(payload);
                title = "now boarding";
                NotificationBroadcast.sendBoardingBroadcast(context, json.getString("user"), json.getString("timestamp"));
                notificationText = "MQTT: " + json.getString("timestamp") + " " + title + ": " + json.getString("user");
            } else if (topic.equals("test/smile")) {
                title = "test/smile";
                notificationText = payload;
            } else if (topic.equals("user/eta")) {
                json = new JSONObject(payload);
                NotificationBroadcast.sendEtaBroadcast(context, json.getString("user"), json.getString("eta"), json.getString("timestamp"));
                notificationText = json.getString("timestamp") + " ETA " + json.getString("user") + ": " + json.getString("eta");
            } else if (topic.equals("bar/status")) {
                notificationText = payload;
            } else if (title.equals("mission completed")) {
                notificationText = json.getString("timestamp") + "mission completed: " + title + " " + payload;
            } else {
                Log.d(LOG_TAG, "Unknown notification message received: " + topic + " / " + payload);
                return;
            }
            createNotification(notificationText);

        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
    }

    private void createNotification(String notificationText) {
        //TODO: don't access the database from the main thread
        NotificationsDataSource dataSource = new NotificationsDataSource(context);
        dataSource.open();
        dataSource.createNotification(notificationText);

        Intent notificationIntent = new Intent(context, NotificationActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        Intent deleteIntent = new Intent(context, NotificationBroadcastReceiver.class);
        deleteIntent.setAction(NotificationBroadcastReceiver.ACTION_NOTIFICATION_CANCELLED);

        PendingIntent pendingDeleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        try {
            NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
            ArrayList<Notification> notificationList =
                    dataSource.getAllNotifications();

            if (notificationList.size() > 5) {
                for (int i = 0; i < 5; i++) {
                    style.addLine(notificationList.get(i).toString());
                }
                style.setSummaryText("+" + (notificationList.size() - 5) + " more...");
            } else {
                for (org.c_base.c_beam.domain.Notification line : notificationList) {
                    style.addLine(line.toString());
                }
            }

            android.app.Notification notification = new NotificationCompat.Builder(context)
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

    public void startConnectionExt() {
        Log.i(LOG_TAG, "start connection to external MQTT server");
    }
}