package org.c_base.c_beam.activity;

import java.util.ArrayList;

import org.c_base.c_beam.R;
import org.c_base.c_beam.fragment.ArrayListFragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.view.Menu;

@SuppressLint("NewApi")
public class NotificationActivity extends C_beamActivity implements OnClickListener {
	private static final String NO_MESSAGES = "keine nachrichten";

	private static final String TAG = "NotificationActivity";

	private static ArrayList<String> notificationList = new ArrayList<String>();

	ViewPager mViewPager;
	EditText text;

	private View mNotificationArea;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (notificationList.size() == 0) {
			notificationList.add(NO_MESSAGES);
		}
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		setContentView(R.layout.activity_notification);

		mNotificationArea = findViewById(R.id.notification_area);
		
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.notifications, new ArrayListFragment(notificationList));
		ft.commit();

		setupActionBar();
		
		Button buttonDeleteNotifications = (Button) findViewById(R.id.button_delete_notifications);
		buttonDeleteNotifications.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showConfirmation();
			}
		});
	}

	public void onStart() {
		Log.i(TAG, "onStart()");
		super.onStart();
	}

	public void showConfirmation() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.confirm_delete_notifications);
		builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				clear();
				onBackPressed();
			}
		});
		builder.setNegativeButton(R.string.button_cancel, null);
		builder.create().show();
	}

	@Override
	protected void onPause() {
		Log.i(TAG, "onPause()");
		super.onPause();
		clear();
	}

	@Override
	protected void onResume () {
		Log.i(TAG, "onResume()");
		super.onResume();
		mNotificationArea.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// Hide some menu items when not connected to the crew network
		return true;
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		clear();
	}

	public static void addNotification(String notification) {
		if (notificationList.size() == 0 || notificationList.get(0).contentEquals(NO_MESSAGES)) {
			notificationList.clear();
		}
		notificationList.add(0,notification);
	}
	
	public static void clear() {
		notificationList.clear();
		notificationList.add(NO_MESSAGES);
	}

	public static ArrayList<String> getNotificationList() {
		return notificationList;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
