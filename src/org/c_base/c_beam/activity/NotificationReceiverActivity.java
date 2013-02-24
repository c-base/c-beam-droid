package org.c_base.c_beam.activity;

import android.os.Bundle;
import android.util.Log;
import com.actionbarsherlock.app.SherlockActivity;
import org.c_base.c_beam.R;

public class NotificationReceiverActivity extends SherlockActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user);
    Log.i("NotificationReceiverActivity", "foo");
  }
} 