package org.c_base.c_beam.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.actionbarsherlock.view.Menu;

import org.c_base.c_beam.R;
import org.c_base.c_beam.domain.Notification;
import org.c_base.c_beam.extension.NotificationBroadcast;
import org.c_base.c_beam.fragment.NotificationListFragment;
import org.c_base.c_beam.util.NotificationsDataSource;

import java.util.ArrayList;

@SuppressLint("NewApi")
public class NotificationActivity extends C_beamActivity implements OnClickListener {
	private static final String TAG = "NotificationActivity";

	private static ArrayList<Notification> notificationList = new ArrayList<Notification>();
	private NotificationsDataSource datasource;
	private View mNotificationArea;

//	ViewPager mViewPager;
//	EditText text;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		datasource = new NotificationsDataSource(this);
		datasource.open();

		notificationList = datasource.getAllNotifications();

		if (notificationList.size() == 0) {
			notificationList.add(new Notification(Notification.NO_MESSAGES));
		}
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		setContentView(R.layout.activity_notification);

		mNotificationArea = findViewById(R.id.notification_area);

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.notifications, new NotificationListFragment(notificationList));
		ft.commit();

		setupActionBar();

		Button buttonDeleteNotifications = (Button) findViewById(R.id.button_delete_notifications);
		buttonDeleteNotifications.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showConfirmation();
			}
		});

		NotificationBroadcast.sendReadBroadcast(this);
	}

	public void onStart() {
		super.onStart();
	}

	public void showConfirmation() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.confirm_delete_notifications);
		builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				datasource.deleteAllNotifications();
				onBackPressed();
			}
		});
		builder.setNegativeButton(R.string.button_cancel, null);
		builder.create().show();
	}

	@Override
	protected void onPause() {
		super.onPause();
		datasource.close();
	}

	@Override
	protected void onResume () {
		super.onResume();
		datasource.open();
		mNotificationArea.setVisibility(View.VISIBLE);
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onClick(View v) {
		// do nothing on click
	}
}
