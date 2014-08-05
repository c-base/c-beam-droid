package org.c_base.c_beam.extension;


import android.content.Context;
import android.content.Intent;

import java.util.Date;

public class NotificationBroadcast {
	public static final String ACTION_BOARDING = "org.c_base.c_beam.extension.NOTIFICATION_BOARDING";
	public static final String ACTION_ETA = "org.c_base.c_beam.extension.NOTIFICATION_ETA";
	public static final String ACTION_READ = "org.c_base.c_beam.extension.NOTIFICATION_READ";
	public static final String ACTION_CANCELLED = "org.c_base.c_beam.extension.NOTIFICATION_CANCELLED";

	public static final String EXTRA_MEMBER = "member";
	public static final String EXTRA_ETA = "eta";
	public static final String EXTRA_TIMESTAMP = "timestamp";

	public static final String NOTIFICATION_PERMISSION = "org.c_base.c_beam.permission.NOTIFICATION";


	public static void sendBoardingBroadcast(Context context, String member, Date date) {
		Intent intent = new Intent(ACTION_BOARDING);
		intent.putExtra(EXTRA_MEMBER, member);
		intent.putExtra(EXTRA_TIMESTAMP, date.getTime());
		context.sendBroadcast(intent, NOTIFICATION_PERMISSION);
	}

	public static void sendEtaBroadcast(Context context, String member, String eta, Date date) {
		Intent intent = new Intent(ACTION_ETA);
		intent.putExtra(EXTRA_MEMBER, member);
		intent.putExtra(EXTRA_ETA, eta);
		intent.putExtra(EXTRA_TIMESTAMP, date.getTime());
		context.sendBroadcast(intent, NOTIFICATION_PERMISSION);
	}

	public static void sendReadBroadcast(Context context) {
		Intent intent = new Intent(ACTION_READ);
		context.sendBroadcast(intent, NOTIFICATION_PERMISSION);
	}

	public static void sendCancelledBroadcast(Context context) {
		Intent intent = new Intent(ACTION_CANCELLED);
		context.sendBroadcast(intent, NOTIFICATION_PERMISSION);
	}
}
