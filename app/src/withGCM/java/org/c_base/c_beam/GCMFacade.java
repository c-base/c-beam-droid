package org.c_base.c_beam;

import android.content.Context;
import android.util.Log;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.c_base.c_beam.task.AsyncC_beamTask;

public class GCMFacade  {
    private static final String LOG_TAG = "GCMFacade";

    /**
     * There is no server-side unregister RPC, so the FCM token c-beam already holds stays
     * valid and it may keep pushing. Opting out is not yet complete on this flavor.
     */
    public static void disablePush(Context context) {
        Log.w(LOG_TAG, "push disabled locally; the server still holds the FCM token");
    }

    public static void setupGCM(final Context context) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                        String username = sharedPref.getString(Settings.USERNAME, "bernd");
                        // Get new Instance ID token
                        String token = task.getResult();
                        AsyncC_beamTask act = new AsyncC_beamTask();
                        act.execute("fcm_update", "user", username, "regid", token);
                        // C_beam.getInstance().call("fcm_update", "user", username, "regid", token);;
                    }
                });
    }

}
