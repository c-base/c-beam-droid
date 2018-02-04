package org.c_base.c_beam;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.c_base.c_beam.domain.C_beam;

public class GCMFacade  {


    public static void setupGCM(final Context context) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                String registrationId = GCMManager.getRegistrationId(context);
                String username = sharedPref.getString(Settings.USERNAME, "bernd");

                C_beam c_beam = C_beam.getInstance();
                c_beam.call("gcm_update", "user", username, "regid", registrationId);
            }
        }).start();



        //c_beam.register_update(registrationId, username);
    }
}
