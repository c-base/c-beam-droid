package org.c_base.c_beam.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.TitlePageIndicator;

import org.c_base.c_beam.GCMManager;
import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.domain.C_beam;
import org.c_base.c_beam.domain.Ring;
import org.c_base.c_beam.util.Helper;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by smile on 2013-05-31.
 */
public class RingActivity extends C_beamActivity {

    enum RING {
        CLAMP, CARBON, CIENCE, CREACTIV, CULTURE, COM, CORE
    }

    private static final int threadDelay = 5000;
    private static final int firstThreadDelay = 100;
    private static final String TAG = "RingActivity";

    private static final boolean debug = false;

    protected C_beam c_beam = C_beam.getInstance();

    protected Runnable fred;
    private Handler handler = new Handler();

    protected View mOfflineArea;
    protected View mCbeamArea;
    protected boolean mIsOnline = false;
    protected WifiBroadcastReceiver mWifiReceiver;
    protected IntentFilter mWifiIntentFilter;

    protected TextView tvAp = null;
    protected TextView tvUsername = null;
    private int defaultETA = 30;
    protected TimePicker timePicker;
    private SharedPreferences sharedPref;

    protected String[] mDrawerItems;
    protected TypedArray mDrawerImages;
    private ListView mDrawerList;

    protected DrawerLayout mDrawerLayout;
    protected ActionBarDrawerToggle mDrawerToggle;

    protected CharSequence mTitle;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_ring);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        defaultETA = Integer.parseInt(sharedPref.getString(Settings.DEFAULT_ETA, "30"));
    }

    protected void setupCbeamArea() {
        mCbeamArea = findViewById(R.id.cbeam_area);
        setupButtons();
        setupAPDisplay();
        setupNavigationDrawer();
    }

    protected void setupButtons() {
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

    protected void setupAPDisplay() {
        tvAp = (TextView) findViewById(R.id.textView_ap);
        tvAp.setTextColor(Color.rgb(58, 182, 228));
        tvUsername = (TextView) findViewById(R.id.textView_username);
        tvUsername.setText(sharedPref.getString(Settings.USERNAME, "bernd"));
        tvUsername.setTextColor(Color.rgb(58, 182, 228));
        Helper.setFont(this, tvUsername);
        Helper.setFont(this, tvAp);
        boolean displayAp = sharedPref.getBoolean(Settings.DISPLAY_AP, true);
        if (!displayAp || tvAp.getText().equals("0 AP")) {
            tvAp.setVisibility(View.GONE);
            tvUsername.setVisibility(View.GONE);
        }
    }


    protected void setupViewPagerIndicator(ViewPager mViewPager) {
        TitlePageIndicator titleIndicator = (TitlePageIndicator) findViewById(R.id.titles);
        titleIndicator.setViewPager(mViewPager);
        Helper.setFont(titleIndicator);
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
    }

    protected void stopNetworkingThreads() {
        c_beam.stopThread();
    }

    private void showOfflineView() {
        mCbeamArea.setVisibility(View.GONE);
        mOfflineArea.setVisibility(View.VISIBLE);
    }

    private void showOnlineView() {
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

    protected void showLoginDialog() {
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

    protected void showLoginLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.toggle_login_title);
        builder.setItems(R.array.login_choices_array, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                switch (whichButton) {
                    case 0: // mission complete
                        login();
                        break;
                    case 1: // mission cancelled
                        logout();
                        break;
                    case 2: // oops
                        break;
                }
            }
        });
        builder.create().show();
    }

    protected void showLogoutDialog() {
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

    protected void startC_outActivity() {
        Intent myIntent = new Intent(this, C_outActivity.class);
        startActivityForResult(myIntent, 0);
    }

    protected void startC_mapsActivity() {
        Intent myIntent = new Intent(this, MapActivity.class);
        startActivityForResult(myIntent, 0);
    }

    public void toggleLogin() {
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

    protected void updateLists() {

    }

    protected void updateTimePicker() {
        Calendar rightNow = Calendar.getInstance();
        rightNow.add(Calendar.MINUTE, defaultETA);
        int currentHour = rightNow.get(Calendar.HOUR_OF_DAY);
        int currentMinute = rightNow.get(Calendar.MINUTE);
        timePicker.setCurrentHour(currentHour);
        timePicker.setCurrentMinute(currentMinute);
    }

    protected void setupNavigationDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setBackgroundColor(Color.argb(120, 0, 0, 0));

        mDrawerItems = getResources().getStringArray(R.array.drawer_items_array);
        mDrawerImages = getResources().obtainTypedArray(R.array.drawer_images_array);

        ArrayList<Ring> mRings = new ArrayList<Ring>();
        for (int i = 0; i < mDrawerItems.length; i++) {
            mRings.add(new Ring(mDrawerItems[i], mDrawerImages.getDrawable(i)));
        }

        mDrawerList.setAdapter(new RingAdapter(this, R.layout.drawer_list_item,
                R.id.drawer_list_item_textview, mRings));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                // TODO Auto-generated method stub
                super.onDrawerOpened(drawerView);
                actionBar.setTitle(mTitle);
                sharedPref.edit().putBoolean(Settings.USER_DISCOVERED_NAVDRAWER, true).commit();
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        if (!sharedPref.getBoolean(Settings.USER_DISCOVERED_NAVDRAWER, false)) {
            mDrawerLayout.openDrawer(Gravity.LEFT);
        }

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    protected void setupGCM() {
        if (sharedPref.getBoolean(Settings.PUSH, false)) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String registrationId = GCMManager.getRegistrationId(this);
            String username = sharedPref.getString(Settings.USERNAME, "bernd");
            new C_beamTask().execute("gcm_update", username, registrationId);
            //c_beam.register_update(registrationId, username);
        }
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle the event for ActionBarDrawerToggle and return true to cancel propagation
        if (item.getItemId() == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                mDrawerLayout.closeDrawer(mDrawerList);
            } else {
                mDrawerLayout.openDrawer(mDrawerList);
            }

            mDrawerToggle.syncState();
            return true;
        }
        // Handle your other action bar items...
        return super.onOptionsItemSelected(item);
    }

    /**
     * Swaps fragments in the main content view
     */
    private void selectItem(int position) {

        /*
        // Create a new fragment and specify the planet to show based on position
        Fragment fragment = new PlanetFragment();
        Bundle args = new Bundle();
        args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
        fragment.setArguments(args);

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

        // Highlight the selected item, update the title, and close the drawer
        mDrawer.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawer);
        */
        setTitle(mDrawerItems[position]);

        switch (position) {
            case 0:
                startActivity(ClampActivity.class);
                break;
            case 1:
                startActivity(CarbonActivity.class);
                break;
            case 2:
                startActivity(CcorderActivity.class);
                break;
            case 3:
                startActivity(CreactivActivity.class);
                break;
            case 4:
                startActivity(CultureActivity.class);
                break;
            case 5:
                startActivity(ComActivity.class);
                break;
            case 6:
                startActivity(CoreActivity.class);
                break;
        }
    }

    protected class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            selectItem(position);
        }
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

    protected void initializeBroadcastReceiver() {
        mWifiReceiver = new WifiBroadcastReceiver();
        mWifiIntentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
    }

    protected class RingAdapter extends ArrayAdapter {
        private static final String TAG = "UserAdapter";
        private ArrayList<Ring> items;
        private Context context;

        @SuppressWarnings("unchecked")
        public RingAdapter(Context context, int itemLayout, int textViewResourceId, ArrayList<Ring> items) {
            super(context, itemLayout, textViewResourceId, items);
            this.context = context;
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View listview = super.getView(position, convertView, parent);

            TextView textView = (TextView) listview.findViewById(R.id.drawer_list_item_textview);
            Ring r = items.get(position);

            textView.setText(r.getName());

            ImageView b = (ImageView) listview.findViewById(R.id.drawer_ring_imageView);
            b.setImageDrawable(r.getImage());
            return listview;
        }

    }

}