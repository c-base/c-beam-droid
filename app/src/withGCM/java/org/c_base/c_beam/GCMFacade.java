package org.c_base.c_beam;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.c_base.c_beam.task.AsyncC_beamTask;

public class GCMFacade  {
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
