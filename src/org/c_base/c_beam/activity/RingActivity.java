package org.c_base.c_beam.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.actionbarsherlock.app.ActionBar;
import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.domain.C_beam;
import org.c_base.c_beam.util.Helper;

/**
 * Created by smile on 2013-05-31.
 */
public class RingActivity extends C_beamActivity {
    private static final int threadDelay = 5000;
    private static final int firstThreadDelay = 100;
    private static final String TAG = "RingActivity";

    private static final boolean debug = false;

    protected C_beam c_beam = C_beam.getInstance();

    protected Runnable fred;

    private View mOfflineArea;
    private View mCbeamArea;
    private boolean mIsOnline = false;
    private WifiBroadcastReceiver mWifiReceiver;
    private IntentFilter mWifiIntentFilter;

    private TextView tvAp = null;
    private TextView tvUsername = null;
    private int defaultETA = 30;
    private TimePicker timePicker;
    private SharedPreferences sharedPref;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_ring);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        defaultETA = Integer.parseInt(sharedPref.getString(Settings.DEFAULT_ETA, "30"));

    }

    protected void setupCbeamArea() {
        mCbeamArea = findViewById(R.id.cbeam_area);
//        setupViewPager();
        setupButtons();
        setupAPDisplay();
        //		Helper.setButtonStyle((ViewGroup) mCbeamArea);
    }

    private void setupButtons() {
        ((ToggleButton) findViewById(R.id.toggleLogin)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((ToggleButton) view).isChecked()) {
                    showLoginDialog();
                } else {
                    showLogoutDialog();
                }
            }
        });

        ((Button) findViewById(R.id.buttonC_out)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startC_outActivity();
            }
        });

        ((Button) findViewById(R.id.button_c_mission)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startC_missionActivity();
            }
        });

        ((Button) findViewById(R.id.button_c_maps)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startC_mapsActivity();
            }
        });

        setupRingButtons();
    }

    private void setupRingButtons() {
        ((ToggleButton) findViewById(R.id.button_toggle_rings)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleRings(((ToggleButton) view).isChecked());
            }
        });

        if (this instanceof ClampActivity) {
            ((ImageButton) findViewById(R.id.imageButtonClamp)).setEnabled(false);
        } else {
            ((ImageButton) findViewById(R.id.imageButtonClamp)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(ClampActivity.class);
                }
            });
        }

        if (this instanceof CarbonActivity) {
            ((ImageButton) findViewById(R.id.imageButtonCarbon)).setEnabled(false);
        } else {
            ((ImageButton) findViewById(R.id.imageButtonCarbon)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(CarbonActivity.class);
                }
            });
        }

        if (this instanceof CienceActivity) {
            ((ImageButton) findViewById(R.id.imageButtonCience)).setEnabled(false);
        } else {
            ((ImageButton) findViewById(R.id.imageButtonCience)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(CcorderActivity.class);
                }
            });
        }

        if (this instanceof CreactivActivity) {
            ((ImageButton) findViewById(R.id.imageButtonCreactiv)).setEnabled(false);
        } else {
            ((ImageButton) findViewById(R.id.imageButtonCreactiv)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(CreactivActivity.class);
                }
            });
        }

        if (this instanceof CultureActivity) {
            ((ImageButton) findViewById(R.id.imageButtonCulture)).setEnabled(false);
        } else {
            ((ImageButton) findViewById(R.id.imageButtonCulture)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(CultureActivity.class);
                }
            });
        }

        if (this instanceof ComActivity) {
            ((ImageButton) findViewById(R.id.imageButtonCom)).setEnabled(false);
        } else {
            ((ImageButton) findViewById(R.id.imageButtonCom)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(ComActivity.class);
                }
            });
        }

        if (this instanceof CoreActivity) {
            ((ImageButton) findViewById(R.id.imageButtonCore)).setEnabled(false);
        } else {
            ((ImageButton) findViewById(R.id.imageButtonCore)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(CoreActivity.class);
                }
            });
        }
    }

    private void startActivity(Class activityClass) {
        Log.i(TAG, "startActivity");
        Intent myIntent = new Intent(this, activityClass);
        startActivityForResult(myIntent, 0);
    }

    protected void setupOfflineArea() {
        mOfflineArea = findViewById(R.id.info_area);
        TextView textView = (TextView) findViewById(R.id.not_in_crew_network);
        Helper.setFont(this, textView);
        timePicker = (TimePicker) this.findViewById(R.id.timePicker1);
        timePicker.setIs24HourView(true);
        ((Button) findViewById(R.id.button_set_eta)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showETAConfirmationDialog();
            }
        });
        ((Button) findViewById(R.id.button_reset_eta)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showResetETAConfirmationDialog();
            }
        });
    }

    private void setupAPDisplay() {
        tvAp = (TextView) findViewById(R.id.textView_ap);
        tvAp.setTextColor(Color.rgb(58, 182, 228));
        tvUsername = (TextView) findViewById(R.id.textView_username);
        tvUsername.setText(sharedPref.getString(Settings.USERNAME, "bernd"));
        tvUsername.setTextColor(Color.rgb(58, 182, 228));
        Helper.setFont(this, tvUsername);
        Helper.setFont(this, tvAp);
        boolean displayAp = sharedPref.getBoolean(Settings.DISPLAY_AP, true);
        if (!displayAp || tvAp.getText().equals("0 AP")) {
            //			tvUsername.setHeight(0);
            //			tvAp.setHeight(0);
            tvAp.setVisibility(View.GONE);
            tvUsername.setVisibility(View.GONE);
        }
    }

    private void switchToOfflineMode() {
        mIsOnline = false;
        showOfflineView();
        stopNetworkingThreads();
    }

    private void switchToOnlineMode() {
        if(mIsOnline) {
            return;
        }
        mIsOnline = true;
        showOnlineView();
        startNetworkingThreads();
    }

    private void startNetworkingThreads() {
        c_beam.startThread();
        updateLists();
    }

    private void stopNetworkingThreads() {
        c_beam.stopThread();
    }

    private void showOfflineView() {
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        mCbeamArea.setVisibility(View.GONE);
        mOfflineArea.setVisibility(View.VISIBLE);
    }

    private void showOnlineView() {
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mOfflineArea.setVisibility(View.GONE);
        mCbeamArea.setVisibility(View.VISIBLE);
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

    private void startC_missionActivity() {
        Intent myIntent = new Intent(this, MissionActivity.class);
        startActivityForResult(myIntent, 0);
    }

    private void showLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_login);
        builder.setPositiveButton(R.string.button_login, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                login();
            }
        });
        builder.setNegativeButton(R.string.button_cancel, null);
        builder.create().show();
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_logout);
        builder.setPositiveButton(R.string.button_logout, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                logout();
            }
        });
        builder.setNegativeButton(R.string.button_cancel, null);
        builder.create().show();
    }

    private void startC_outActivity() {
        Intent myIntent = new Intent(this, C_outActivity.class);
        startActivityForResult(myIntent, 0);
    }

    private void startC_mapsActivity() {
        Intent myIntent = new Intent(this, MapActivity.class);
        startActivityForResult(myIntent, 0);
    }

    public void toggleLogin() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (c_beam.isLoggedIn(sharedPref.getString(Settings.USERNAME, "bernd"))) {
            showLogoutDialog();
        } else {
            showLoginDialog();
        }
    }

    public void login() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        c_beam.force_login(sharedPref.getString(Settings.USERNAME, "bernd"));
    }

    public void logout() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        c_beam.force_logout(sharedPref.getString(Settings.USERNAME, "bernd"));
    }

    private void toggleRings(Boolean state) {
        Log.i(TAG, "toggleRings(" + state + ")");
        if (state) {
            findViewById(R.id.ringbuttons).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.ringbuttons).setVisibility(View.GONE);
        }
    }

    private void startClampActivity() {
        Intent myIntent = new Intent(getApplicationContext(), ClampActivity.class);
        startActivityForResult(myIntent, 0);
    }

    protected void updateLists() {

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

    private class SetETATask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return c_beam.setETA(sharedPref.getString(Settings.USERNAME, "bernd"), params.length == 1 ? params[0] : getETA());
        }

        @Override
        protected void onPostExecute(String result) {
            System.out.println(result);
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