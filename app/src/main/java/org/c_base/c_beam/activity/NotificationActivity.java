package org.c_base.c_beam.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.StrictMode;
import androidx.fragment.app.FragmentTransaction;
import android.widget.Button;

import org.c_base.c_beam.R;
import org.c_base.c_beam.domain.Notification;
import org.c_base.c_beam.fragment.NotificationListFragment;
import org.c_base.c_beam.util.NotificationsDataSource;

import java.util.ArrayList;

public class NotificationActivity extends C_beamActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification);

		NotificationsDataSource datasource = new NotificationsDataSource(this);
		datasource.open();
		ArrayList<Notification> notificationList = datasource.getAllNotifications();
		datasource.close();

		if (notificationList.isEmpty()) {
			notificationList.add(new Notification("keine nachrichten fu:r dich"));
		}
		enableStrictMode();

		NotificationListFragment nlf = new NotificationListFragment();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		nlf.setItems(notificationList);
		ft.add(R.id.notifications, nlf);
		ft.commit();

		Button buttonDeleteNotifications = findViewById(R.id.button_delete_notifications);
		buttonDeleteNotifications.setOnClickListener(v -> showDeleteNotificationsConfirmationDialog());
	}

	private void showDeleteNotificationsConfirmationDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.confirm_delete_notifications);
		builder.setPositiveButton(R.string.button_ok, (dialog, whichButton) -> {
            NotificationsDataSource datasource = new NotificationsDataSource(NotificationActivity.this);
            datasource.open();
            datasource.deleteAllNotifications();
            datasource.close();
            finish();
        });
		builder.setNegativeButton(R.string.button_cancel, null);
		builder.create().show();
	}

	private void enableStrictMode() {
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
	}
}
