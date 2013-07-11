package org.c_base.c_beam.activity;

import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.domain.Article;
import org.c_base.c_beam.domain.Event;
import org.c_base.c_beam.domain.User;
import org.c_base.c_beam.fragment.C_portalListFragment;
import org.c_base.c_beam.fragment.EventListFragment;
import org.c_base.c_beam.util.Helper;

import java.util.ArrayList;

/**
 * Created by smile on 2013-05-31.
 */
public class CultureActivity extends RingActivity implements
        ActionBar.TabListener {
    private static final int EVENTS_TODAY_FRAGMENT = 0;
    private static final int EVENTS_MONTH_FRAGMENT = 1;

    private static final int threadDelay = 5000;
    private static final int firstThreadDelay = 1000;
    private static final String TAG = "CultureActivity";

    private static final boolean debug = false;

    ArrayList<Article> articleList;
    ArrayList<Event> eventList;

    SectionsPagerAdapter mSectionsPagerAdapter;

    ViewPager mViewPager;
    private Handler handler = new Handler();
    EditText text;

    protected Runnable fred;
    private View mInfoArea;
    private View mCbeamArea;
    private boolean mIsOnline = false;
    private WifiBroadcastReceiver mWifiReceiver;
    private IntentFilter mWifiIntentFilter;

    TextView tvAp = null;
    TextView tvUsername = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (android.os.Build.VERSION.SDK_INT > 9) {
//            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//            StrictMode.setThreadPolicy(policy);
//        }

        setContentView(R.layout.activity_main);

        mCbeamArea = findViewById(R.id.cbeam_area);

        setupCbeamArea();


        mInfoArea = findViewById(R.id.info_area);
        TextView textView = (TextView) findViewById(R.id.not_in_crew_network);
        Helper.setFont(this, textView);

        setupActionBar();
        setupViewPager();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        tvAp = (TextView) findViewById(R.id.textView_ap);
        tvAp.setTextColor(Color.rgb(58, 182, 228));
        tvUsername = (TextView) findViewById(R.id.textView_username);
        tvUsername.setText(sharedPref.getString(Settings.USERNAME, "bernd"));
        tvUsername.setTextColor(Color.rgb(58, 182, 228));
        Helper.setFont(this, tvUsername);
        Helper.setFont(this, tvAp);
        boolean displayAp = sharedPref.getBoolean(Settings.DISPLAY_AP, true);
        if (!displayAp) {
            tvUsername.setHeight(0);
            tvAp.setHeight(0);
        }

        //		TextView labelAp = (TextView) findViewById(R.id.label_ap);
        //		Helper.setFont(this, labelAp);

        initializeBroadcastReceiver();
    }

    public void onStart() {
        Log.i(TAG, "onStart()");
        super.onStart();
        startProgress();
    }


    @Override
    protected void onPause() {
        Log.i(TAG, "onPause()");
        unregisterReceiver(mWifiReceiver);
        stopNetworkingThreads();
        super.onPause();
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
        handler.postDelayed(fred, firstThreadDelay );
    }

    public void updateLists() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        EventListFragment events = (EventListFragment) mSectionsPagerAdapter.getItem(EVENTS_TODAY_FRAGMENT);

        ArrayList<User> userList = c_beam.getUsers();

        ToggleButton button = (ToggleButton) findViewById(R.id.toggleLogin);
        for (User user: userList) {
            if(user.getUsername().equals(sharedPref.getString(Settings.USERNAME, "bernd"))) {
                if (button != null) {
                    button.setChecked(user.getStatus().equals("online"));
                    button.setEnabled(true);
                    if(sharedPref.getBoolean(Settings.DISPLAY_AP, true)) {
                        tvAp.setText(user.getAp()+" AP");
                        tvAp.setVisibility(View.VISIBLE);
                        tvUsername.setVisibility(View.VISIBLE);
                    }
                }
            }
        }

        if (events.isAdded()){
            eventList = c_beam.getEvents();
            events.clear();
            if (eventList != null) {
                for(int i=0; i<eventList.size();i++)
                    events.addItem(eventList.get(i));
            }
        }
    }

    protected void onResume () {
        Log.i(TAG, "onResume()");
        super.onResume();

        registerReceiver(mWifiReceiver, mWifiIntentFilter);

        if (c_beam.isInCrewNetwork()) {
            switchToOnlineMode();
        } else {
            switchToOfflineMode();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            menu.findItem(R.id.menu_c_corder).setVisible(false);
        }
        // Hide some menu items when not connected to the crew network
        menu.findItem(R.id.menu_login).setVisible(mIsOnline);
        menu.findItem(R.id.menu_logout).setVisible(mIsOnline);
        menu.findItem(R.id.menu_map).setVisible(mIsOnline);
        menu.findItem(R.id.menu_c_out).setVisible(mIsOnline);

        return true;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab,
                              FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab,
                                FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab,
                                FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        Fragment[] pages;
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            pages = new Fragment[getCount()];
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a DummySectionFragment (defined as a static inner class
            // below) with the page number as its lone argument.
            Fragment fragment;
            if (pages[position] == null) {
                if(position == EVENTS_TODAY_FRAGMENT) {
                    fragment = new EventListFragment();
                } else {
                    fragment = null;
                }
                fragment.setArguments(new Bundle());
                pages[position] = fragment;
            } else {

                fragment = pages[position];
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case EVENTS_TODAY_FRAGMENT:
                    return getString(R.string.title_culture_section1).toUpperCase();
            }
            return null;
        }
    }

    private void setupViewPager() {
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                try {
                    actionBar.setSelectedNavigationItem(position);
                } catch (Exception e) {

                }
            }
        });

        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            ActionBar.Tab tab = actionBar.newTab();
            TextView t = new TextView(getApplicationContext());
            t.setTypeface(Typeface.createFromAsset(getAssets(), "CEVA-CM.TTF"));
            tab.setText(mSectionsPagerAdapter.getPageTitle(i));
            tab.setTabListener(this);
            actionBar.addTab(tab);
        }
    }

    private void switchToOfflineMode() {
        mIsOnline = false;
        showOfflineView();
        stopNetworkingThreads();
    }

    private void switchToOnlineMode() {
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
        mInfoArea.setVisibility(View.VISIBLE);
    }

    private void showOnlineView() {
        mIsOnline = true;
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mInfoArea.setVisibility(View.GONE);
        mCbeamArea.setVisibility(View.VISIBLE);
    }

    private void initializeBroadcastReceiver() {
        mWifiReceiver = new WifiBroadcastReceiver();
        mWifiIntentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
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
                    showOnlineView();
                } else if (mIsOnline) {
                    showOfflineView();
                }
            }
        }
    }
}