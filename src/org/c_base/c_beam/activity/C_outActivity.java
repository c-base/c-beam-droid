package org.c_base.c_beam.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;

import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.domain.C_beam;
import org.c_base.c_beam.fragment.C_outListFragment;

import java.util.ArrayList;
import java.util.Calendar;

public class C_outActivity extends C_beamActivity {
    C_beam c_beam = C_beam.getInstance();
    EditText et;

    ActionBar actionBar;
    private Runnable fred;
    private Handler handler = new Handler();
    private long threadDelay = 5000;
    private long firstThreadDelay = 1000;

    protected WifiBroadcastReceiver mWifiReceiver;
    protected IntentFilter mWifiIntentFilter;
    private TimePicker timePicker;
    private boolean mIsOnline = false;

    private int defaultETA = 30;
    private SharedPreferences sharedPref;
    private boolean debug = false;
    private C_outListFragment c_outList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_c_out);

        c_outList = (C_outListFragment) this.getSupportFragmentManager().findFragmentById(R.id.fragment1);

        et = (EditText) findViewById(R.id.c_outEditText);

        Button b = (Button) findViewById(R.id.button_announce);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = et.getText().toString();
                if (text.length() != 0) {
                    c_beam.announce(text);
                }
            }
        });

        b = (Button) findViewById(R.id.button_r2d2);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = et.getText().toString();
                if (text.length() != 0) {
                    c_beam.r2d2(text);
                }
            }
        });

        b = (Button) findViewById(R.id.button_tts);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = et.getText().toString();
                if (text.length() != 0) {
                    c_beam.tts(text);
                }
            }
        });

        final ViewGroup mContainer = (ViewGroup) findViewById(
                android.R.id.content).getRootView();
        setAppFont(mContainer);
//		Helper.setButtonStyle(mContainer);
        setupActionBar();
        initializeBroadcastReceiver();
    }

    public void startProgress() {
        // Do something long
        fred = new Runnable() {
            @Override
            public void run() {
                updateLists();
                handler.postDelayed(fred, threadDelay);
            }

        };
        handler.postDelayed(fred, firstThreadDelay);
    }

    private void updateLists() {
        ArrayList<String> sounds = c_beam.getSounds();

        if (c_outList != null && c_outList.isAdded()) {
            c_outList.clear();
            for (int i = 0; i < sounds.size(); i++) {
                c_outList.addItem(sounds.get(i));
            }
        }
    }

    public void onStart() {
        super.onStart();
        startProgress();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mWifiReceiver);
        stopNetworkingThreads();
        super.onPause();
    }

    protected void onResume() {

        super.onResume();
        c_beam.setActivity(this);
        //c_beam.testJsonRPC2();

        registerReceiver(mWifiReceiver, mWifiIntentFilter);
        if (c_beam.isInCrewNetwork()) {
            switchToOnlineMode();
        } else {
            updateTimePicker();
            switchToOfflineMode();
        }
    }

    protected void updateTimePicker() {
        Calendar rightNow = Calendar.getInstance();
        rightNow.add(Calendar.MINUTE, defaultETA);
        int currentHour = rightNow.get(Calendar.HOUR_OF_DAY);
        int currentMinute = rightNow.get(Calendar.MINUTE);
        timePicker.setCurrentHour(currentHour);
        timePicker.setCurrentMinute(currentMinute);
    }

    protected void switchToOfflineMode() {
        mIsOnline = false;
        showOfflineView();
        stopNetworkingThreads();
    }

    protected void switchToOnlineMode() {
        if (mIsOnline) {
            return;
        }
        mIsOnline = true;
        showOnlineView();
        startNetworkingThreads();
    }

    private void startNetworkingThreads() {
        c_beam.startThread();
        //updateLists();
    }

    protected void stopNetworkingThreads() {
        c_beam.stopThread();
    }

    private void showOfflineView() {
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        //mCbeamArea.setVisibility(View.GONE);
        //mOfflineArea.setVisibility(View.VISIBLE);
    }

    private void showOnlineView() {
        //getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        //mOfflineArea.setVisibility(View.GONE);
        //mCbeamArea.setVisibility(View.VISIBLE);
    }

    private String getETA() {
        Integer currentMinute = timePicker.getCurrentMinute();
        String eta = "" + timePicker.getCurrentHour() + (currentMinute < 10 ? "0" : "") + currentMinute;
        return eta;
    }

    private void showETAConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.confirm_eta, getETA()));
        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                new SetETATask().execute(getETA());
            }
        });
        builder.setNegativeButton(R.string.button_cancel, null);
        builder.create().show();
    }

    private void showResetETAConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.confirm_reset_eta));
        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                new SetETATask().execute("0");
            }
        });
        builder.setNegativeButton(R.string.button_cancel, null);
        builder.create().show();
    }

    class WifiBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (debug) {
                showOnlineView();
                return;
            }
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                int previousState = intent.getIntExtra(WifiManager.EXTRA_PREVIOUS_WIFI_STATE, -1);

                if (state == previousState) {
                    return;
                }

                if (state == WifiManager.WIFI_STATE_ENABLED && c_beam.isInCrewNetwork()) {
                    switchToOnlineMode();
                } else if (mIsOnline) {
                    switchToOfflineMode();
                }
            }
        }
    }

    protected void initializeBroadcastReceiver() {
        mWifiReceiver = new WifiBroadcastReceiver();
        mWifiIntentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
    }

    protected class SetETATask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return c_beam.setETA(sharedPref.getString(Settings.USERNAME, "bernd"), params.length == 1 ? params[0] : getETA());
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.contentEquals("eta_set")) {
                result = getText(R.string.eta_set).toString();
            } else if (result.contentEquals("eta_removed")) {
                result = getText(R.string.eta_removed).toString();
            } else {
                result = getText(R.string.eta_failure).toString();
            }
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }


}
