package org.c_base.c_beam;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.c_base.c_beam.domain.C_beam;

public class GCMFacade  {
    public static void setupGCM(final Context context) {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                        String username = sharedPref.getString(Settings.USERNAME, "bernd");
                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        C_beam.getInstance().call("fcm_update", "user", username, "regid", token);;
                    }
                });
    }
}
