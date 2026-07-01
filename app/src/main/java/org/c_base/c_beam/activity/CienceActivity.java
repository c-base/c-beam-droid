package org.c_base.c_beam.activity;

import android.os.Bundle;
import org.c_base.c_beam.R;

/**
 * Created by smile on 2013-05-31.
 */
public class CienceActivity extends RingActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupOfflineArea();
        setupCbeamArea();
        initializeBroadcastReceiver();
    }
}