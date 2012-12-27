package org.c_base.c_beam;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class NotificationReceiverActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user);
    Log.i("NotificationReceiverActivity", "foo");
  }
} 