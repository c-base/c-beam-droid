package org.c_base.c_beam.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GCMBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
	        //GCMIntentService.runIntentInService(context, intent);
	        setResult(Activity.RESULT_OK, null, null);
			
		}
	}