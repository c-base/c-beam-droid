package org.c_base.c_beam.activity;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import org.c_base.c_beam.R;

public class NotificationReceiverActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user);
    Log.i("NotificationReceiverActivity", "foo");
  }
} 