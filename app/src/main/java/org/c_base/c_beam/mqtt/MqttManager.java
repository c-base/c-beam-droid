package org.c_base.c_beam.mqtt;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedContext;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientBuilder;
import com.hivemq.client.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3ConnectBuilder;

import org.c_base.c_beam.NotificationBroadcastReceiver;
import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.activity.NotificationActivity;
import org.c_base.c_beam.domain.Notification;
import org.c_base.c_beam.extension.NotificationBroadcast;
import org.c_base.c_beam.util.NotificationsDataSource;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The app's single MQTT connection, on the HiveMQ client (MQTT 3.1.1 -- the broker is
 * mosquitto 1.5.6, which speaks nothing newer; switch to the Mqtt5 client once it does).
 *
 * The client is a plain Java object with no Android service behind it: reconnecting is the
 * client's own job (automatic reconnect with backoff), keeping the process alive is
 * {@link MqttForegroundService}'s. Every callback below arrives on a HiveMQ/Netty thread,
 * never on the main thread.
 */
public class MqttManager {

    private static final String LOG_TAG = "MqttManager";
    private static final MqttQos QOS = MqttQos.EXACTLY_ONCE;
    private static final int KEEP_ALIVE_SECONDS = 60;

    // The id carries a version suffix because a channel's vibration setting is fixed at
    // creation: an existing installation would keep the old, silent channel forever.
    private static final String NOTIFICATION_CHANNEL_ID = "cbeam_channel_v2";
    private static final long[] VIBRATION_PATTERN = {0, 400};
    private static final int NOTIFICATION_ID = 1;

    // Only reached when no pref_mqtt_uri is persisted at all; CbeamApplication seeds the
    // preference defaults from preferences.xml, so normally that file's value wins. The
    // host names the external broker that was planned but never built (see CODE-AUDIT.md).
    private static final String DEFAULT_MQTT_URI = "ssl://echelon.c-base.org:1883";
    private static final String CLIENT_ID_PREFIX = "c-beam-droid-";
    /** The fixed id every installation used to share; see {@link #clientId()}. */
    private static final String LEGACY_SHARED_CLIENT_ID = "c-beam-droid-bernd01";

    /** Topics every client subscribes to. user/eta was handled but never subscribed before. */
    private static final String[] TOPICS = {"user/boarding", "user/eta", "test/smile"};
    /** Subscribed only while on the crew network, see {@link #crewNetworkConnected()}. */
    private static final String BAR_STATUS_TOPIC = "bar/status";

    /** Lets the foreground service mirror the connection state in its notification. */
    public interface StateListener {
        void onStateChanged(boolean connected);
    }

    // The static instance holds the application context, never an Activity.
    @SuppressLint("StaticFieldLeak")
    private static MqttManager instance;

    private final Context context;
    private final SharedPreferences sharedPref;
    private final NotificationManager mNotificationManager;
    private final ExecutorService notificationExecutor = Executors.newSingleThreadExecutor();

    private volatile Mqtt3AsyncClient client;
    private volatile boolean barStatusWanted = false;
    private volatile StateListener stateListener;

    public static synchronized MqttManager getInstance(Context context) {
        if (instance == null) {
            instance = new MqttManager(context.getApplicationContext());
        }
        return instance;
    }

    private MqttManager(Context context) {
        this.context = context;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.channel_name), NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(context.getString(R.string.channel_description));
        channel.enableVibration(true);
        channel.setVibrationPattern(VIBRATION_PATTERN);
        mNotificationManager.createNotificationChannel(channel);
    }

    // ---- connection lifecycle -------------------------------------------------------------

    /** Connects if not already connected or reconnecting. Safe to call repeatedly. */
    public synchronized void startConnection() {
        if (client == null) {
            client = createClient();
        }
        if (client.getState().isConnectedOrReconnect()) {
            Log.i(LOG_TAG, "already connected or reconnecting");
            return;
        }
        connect();
    }

    /** Disconnects and stops automatic reconnects. The client is rebuilt on the next start. */
    public synchronized void stopConnection() {
        Mqtt3AsyncClient c = client;
        client = null;
        if (c != null && c.getState().isConnectedOrReconnect()) {
            c.disconnect().whenComplete((v, t) -> Log.i(LOG_TAG, "disconnected"));
        }
        notifyState(false);
    }

    public boolean isConnected() {
        Mqtt3AsyncClient c = client;
        return c != null && c.getState().isConnected();
    }

    public void setStateListener(StateListener listener) {
        stateListener = listener;
        if (listener != null) {
            listener.onStateChanged(isConnected());
        }
    }

    private Mqtt3AsyncClient createClient() {
        String serverUri = sharedPref.getString(Settings.MQTT_URI, DEFAULT_MQTT_URI);
        URI uri;
        try {
            uri = new URI(serverUri);
            if (uri.getHost() == null) {
                throw new URISyntaxException(serverUri, "no host");
            }
        } catch (URISyntaxException e) {
            Log.e(LOG_TAG, "invalid MQTT URI '" + serverUri + "', using default", e);
            uri = URI.create(DEFAULT_MQTT_URI);
        }
        boolean tls = "ssl".equalsIgnoreCase(uri.getScheme())
                || sharedPref.getBoolean(Settings.MQTT_TLS, false);
        int port = uri.getPort() > 0 ? uri.getPort() : (tls ? 8883 : 1883);
        Log.i(LOG_TAG, "MQTT server " + uri.getHost() + ":" + port + (tls ? " (TLS)" : ""));

        Mqtt3ClientBuilder builder = Mqtt3Client.builder()
                .identifier(clientId())
                .serverHost(uri.getHost())
                .serverPort(port)
                .automaticReconnectWithDefaultConfig()
                .addConnectedListener(ctx -> onConnected())
                .addDisconnectedListener(this::onDisconnected);
        if (tls) {
            // System trust store: the c-base hosts serve publicly trusted certificates.
            builder = builder.sslWithDefaultConfig();
        }
        Mqtt3AsyncClient c = builder.buildAsync();
        c.publishes(MqttGlobalPublishFilter.ALL, publish -> onMessage(
                publish.getTopic().toString(),
                new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8)));
        return c;
    }

    /**
     * A per-installation client id. The old fixed default "c-beam-droid-bernd01" was shared
     * by every install, and a broker drops the previous session whenever a second client
     * connects with the same id -- so two members with push on kept kicking each other off.
     */
    private String clientId() {
        String id = sharedPref.getString(Settings.MQTT_ID, "");
        if (TextUtils.isEmpty(id) || LEGACY_SHARED_CLIENT_ID.equals(id)) {
            id = CLIENT_ID_PREFIX + UUID.randomUUID().toString().substring(0, 8);
            sharedPref.edit().putString(Settings.MQTT_ID, id).apply();
            Log.i(LOG_TAG, "generated MQTT client id " + id);
        }
        return id;
    }

    private void connect() {
        // Clean session, deliberately. With a persistent session the broker queues every
        // boarding and bar-status event while the phone is offline and replays the lot on
        // reconnect -- the first connect with the new client delivered a burst of twenty
        // stale "now boarding" messages from the previous days. These are presence pings;
        // one that is hours old is noise, not news. Short gaps are covered by the automatic
        // reconnect, and subscriptions are re-established in onConnected() on every connect.
        Mqtt3ConnectBuilder connect = Mqtt3Connect.builder()
                .cleanSession(true)
                .keepAlive(KEEP_ALIVE_SECONDS);
        String userName = sharedPref.getString(Settings.MQTT_USERNAME, "");
        String password = sharedPref.getString(Settings.MQTT_PASSWORD, "");
        if (!TextUtils.isEmpty(userName)) {
            connect = connect.simpleAuth(Mqtt3SimpleAuth.builder()
                    .username(userName)
                    .password(password.getBytes(StandardCharsets.UTF_8))
                    .build());
        }
        client.connect(connect.build()).whenComplete((ack, throwable) -> {
            if (throwable != null) {
                // The client keeps retrying on its own; this is just the first attempt.
                Log.w(LOG_TAG, "connect failed, will retry: " + throwable);
            } else {
                Log.i(LOG_TAG, "connected, session present: " + ack.isSessionPresent());
            }
        });
    }

    private void onConnected() {
        Log.i(LOG_TAG, "connection established, subscribing");
        for (String topic : TOPICS) {
            subscribe(topic);
        }
        if (barStatusWanted) {
            subscribe(BAR_STATUS_TOPIC);
        }
        notifyState(true);
    }

    private void onDisconnected(MqttClientDisconnectedContext ctx) {
        Log.w(LOG_TAG, "disconnected (" + ctx.getSource() + "): " + ctx.getCause()
                + (ctx.getReconnector().isReconnect() ? ", reconnecting" : ""));
        notifyState(false);
    }

    private void notifyState(boolean connected) {
        StateListener listener = stateListener;
        if (listener != null) {
            listener.onStateChanged(connected);
        }
    }

    // ---- subscriptions --------------------------------------------------------------------

    private void subscribe(String topic) {
        Mqtt3AsyncClient c = client;
        if (c == null) {
            return;
        }
        c.subscribeWith().topicFilter(topic).qos(QOS).send().whenComplete((ack, throwable) -> {
            if (throwable != null) {
                Log.e(LOG_TAG, "subscribe " + topic + " failed", throwable);
            } else {
                Log.i(LOG_TAG, "subscribed " + topic + ": " + ack.getReturnCodes());
            }
        });
    }

    private void unsubscribe(String topic) {
        Mqtt3AsyncClient c = client;
        if (c == null) {
            return;
        }
        c.unsubscribeWith().topicFilter(topic).send().whenComplete((v, throwable) -> {
            if (throwable != null) {
                Log.e(LOG_TAG, "unsubscribe " + topic + " failed", throwable);
            }
        });
    }

    /**
     * Called by the screens when the WiFi receiver sees the crew network. The bar status is
     * only interesting on base, so it is subscribed here rather than in {@link #TOPICS}. The
     * flag also makes the subscription survive a reconnect, and applies it if the WiFi
     * transition happens to land before the connection is up.
     */
    public void crewNetworkConnected() {
        barStatusWanted = true;
        if (isConnected()) {
            subscribe(BAR_STATUS_TOPIC);
        }
    }

    public void crewNetworkDisconnected() {
        barStatusWanted = false;
        if (isConnected()) {
            unsubscribe(BAR_STATUS_TOPIC);
        }
    }

    // ---- messages -------------------------------------------------------------------------

    protected void onMessage(String topic, String payload) {
        Log.d(LOG_TAG, "message on " + topic);
        try {
            String notificationText;
            if (topic.equals("user/boarding")) {
                JSONObject json = new JSONObject(payload);
                NotificationBroadcast.sendBoardingBroadcast(context, json.getString("user"), json.getString("timestamp"));
                notificationText = "MQTT: " + json.getString("timestamp") + " now boarding: " + json.getString("user");
            } else if (topic.equals("test/smile")) {
                notificationText = payload;
            } else if (topic.equals("user/eta")) {
                JSONObject json = new JSONObject(payload);
                NotificationBroadcast.sendEtaBroadcast(context, json.getString("user"), json.getString("eta"), json.getString("timestamp"));
                notificationText = json.getString("timestamp") + " ETA " + json.getString("user") + ": " + json.getString("eta");
            } else if (topic.equals(BAR_STATUS_TOPIC)) {
                notificationText = payload;
            } else {
                Log.d(LOG_TAG, "Unknown notification message received: " + topic + " / " + payload);
                return;
            }
            createNotification(notificationText);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "malformed payload on " + topic + ": " + payload, e);
        }
    }

    /**
     * The SQLite write and read below are kept off the delivering thread (and off the main
     * thread) by handing the whole notification build to a single worker.
     */
    private void createNotification(final String notificationText) {
        notificationExecutor.execute(() -> writeAndShowNotification(notificationText));
    }

    private void writeAndShowNotification(String notificationText) {
        NotificationsDataSource dataSource = new NotificationsDataSource(context);
        dataSource.open();
        dataSource.createNotification(notificationText);

        Intent notificationIntent = new Intent(context, NotificationActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE);

        Intent deleteIntent = new Intent(context, NotificationBroadcastReceiver.class);
        deleteIntent.setAction(NotificationBroadcastReceiver.ACTION_NOTIFICATION_CANCELLED);

        PendingIntent pendingDeleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

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

            android.app.Notification notification =
                    new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle("c-beam")
                    .setContentText(notificationText)
                    .setAutoCancel(true)
                    .setSubText(null)
                    .setTicker(notificationText)
                    .setDeleteIntent(pendingDeleteIntent)
                    .setContentIntent(pIntent)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setStyle(style)
                    // Ignored on API 26+, where the channel above supplies the vibration.
                    .setVibrate(VIBRATION_PATTERN)
                    .build();

            mNotificationManager.notify(NOTIFICATION_ID, notification);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while creating notification", e);
        }

        dataSource.close();
    }
}
