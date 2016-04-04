package org.c_base.c_beam.mqtt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.c_base.c_beam.CbeamApplication;
import org.c_base.c_beam.domain.C_beam;

/**
 * Created by sebastian on 03.04.16.
 */
public class NetworkBroadcastReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "NBR";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(LOG_TAG, "Received network intent");
        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);

        NetworkInfo networkInfo = intent
                .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if (networkInfo != null) {
            Log.i(LOG_TAG, "Type : " + networkInfo.getType() + "State : " + networkInfo.getState());


            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {

                //get the different network states
                if (networkInfo.getState() == NetworkInfo.State.CONNECTING || networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                }
            }
        }


        boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
        boolean hasConnectivity = !noConnectivity;
        if (hasConnectivity) {
            boolean mIsOnline = C_beam.getInstance().isInCrewNetwork();
            if (mIsOnline) {
                startMqttConnection(context);
            } else {
                //startMqttConnectionExt(context);
            }


        }

    }

    private void startMqttConnection(Context context) {
        CbeamApplication app = CbeamApplication.getInstance(context);
        MqttManager connection = app.getMqttManager();
        connection.startConnection();
//        Intent serviceIntent = new Intent(context, MqttManager.class);
//        context.startService(serviceIntent);
//        IBinder binder = peekService(context, new Intent(context, MqttManager.class));
//        //Toast.makeText(context, "Connected", 1000).show();
//        if (binder != null) {
//            //mChatService = ((LocalBinder) binder).getService();
//            //.... other code here
//        }
//
//        MqttManager connection = MqttManager.getInstance(context);
//        connection.startConnection();
    }

    private void startMqttConnectionExt(Context context) {
        MqttManager connection = MqttManager.getInstance(context);
        connection.startConnectionExt();
    }
}