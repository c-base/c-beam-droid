package org.c_base.c_beam;

import android.content.Context;

import org.c_base.c_beam.mqtt.MqttForegroundService;

/**
 * noGCM flavor: there is no Firebase, so "push" means holding the MQTT connection in a
 * foreground service. The name is kept so the shared code can call one facade per flavor.
 */
public class GCMFacade {
    public static void setupGCM(Context context) {
        MqttForegroundService.start(context);
    }

    public static void disablePush(Context context) {
        MqttForegroundService.stop(context);
    }
}
