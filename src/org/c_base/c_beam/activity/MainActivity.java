package org.c_base.c_beam.activity;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.*;

import org.c_base.c_beam.GCMManager;
import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.domain.Artefact;
import org.c_base.c_beam.domain.Article;
import org.c_base.c_beam.domain.C_beam;
import org.c_base.c_beam.domain.Event;
import org.c_base.c_beam.domain.Mission;
import org.c_base.c_beam.domain.Ring;
import org.c_base.c_beam.domain.User;
import org.c_base.c_beam.fragment.ActivitylogFragment;
import org.c_base.c_beam.fragment.ArtefactListFragment;
import org.c_base.c_beam.fragment.C_ontrolFragment;
import org.c_base.c_beam.fragment.C_portalListFragment;
import org.c_base.c_beam.fragment.EventListFragment;
import org.c_base.c_beam.fragment.MissionListFragment;
import org.c_base.c_beam.fragment.RinginfoFragment;
import org.c_base.c_beam.fragment.UserListFragment;
import org.c_base.c_beam.util.Helper;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.TitlePageIndicator;

import static org.c_base.c_beam.domain.C_beam.*;

@SuppressLint("NewApi")
public class MainActivity extends RingActivity {
    private static final int USER_FRAGMENT = 0;
    private static final int C_PORTAL_FRAGMENT = 1;
    private static final int ARTEFACTS_FRAGMENT = 6;
    private static final int RINGINFO_FRAGMENT = 7;
    private static final int EVENTS_FRAGMENT = 5;
    private static final int C_ONTROL_FRAGMENT = 2;
    private static final int MISSION_FRAGMENT = 3;
    private static final int ACTIVITYLOG_FRAGMENT = 4;

    private static final int threadDelay = 5000;
    private static final int firstThreadDelay = 100;
    private static final String TAG = "MainActivity";

    private static final boolean debug = false;

    private ArrayList<Article> articleList;
    private ArrayList<Event> eventList;


    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;


    private EditText text;

    private C_beam c_beam = getInstance();

    protected Runnable fred;
    private boolean mIsOnline = false;

    private int defaultETA = 30;
    private SharedPreferences sharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getSupportActionBar();
        c_beam.setActivity(this);
        setContentView(R.layout.activity_main);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        defaultETA = Integer.parseInt(sharedPref.getString(Settings.DEFAULT_ETA, "30"));

        setupOfflineArea();
        setupActionBar();
        setupCbeamArea();
        setupViewPager();
        setupGCM();

        if (checkUserName() && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processNfcIntent(getIntent());
            toggleLogin();
        }

        initializeBroadcastReceiver();
    }

    void processNfcIntent(Intent intent) {
        System.out.println(intent.getData());
    }

    public void toggleLogin() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (c_beam.isLoggedIn(sharedPref.getString(Settings.USERNAME, "bernd"))) {
            showLogoutDialog();
        } else {
            showLoginDialog();
        }
    }

    public void updateLists() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        UserListFragment online = (UserListFragment) mSectionsPagerAdapter.getItem(USER_FRAGMENT);
        EventListFragment events = (EventListFragment) mSectionsPagerAdapter.getItem(EVENTS_FRAGMENT);
        MissionListFragment missions = (MissionListFragment) mSectionsPagerAdapter.getItem(MISSION_FRAGMENT);
        C_portalListFragment c_portal = (C_portalListFragment) mSectionsPagerAdapter.getItem(C_PORTAL_FRAGMENT);
        ArtefactListFragment artefacts = (ArtefactListFragment) mSectionsPagerAdapter.getItem(ARTEFACTS_FRAGMENT);
        ActivitylogFragment activitylog = (ActivitylogFragment) mSectionsPagerAdapter.getItem(ACTIVITYLOG_FRAGMENT);

        ArrayList<User> onlineList = c_beam.getOnlineList();
        ArrayList<User> etaList = c_beam.getEtaList();

        ArrayList<User> userList = c_beam.getUsers();
        ToggleButton button = (ToggleButton) findViewById(R.id.toggleLogin);
        for (User user : userList) {
            if (user.getUsername().equals(sharedPref.getString(Settings.USERNAME, "bernd"))) {
                if (button != null) {
                    button.setChecked(user.getStatus().equals("online"));
                    button.setEnabled(true);
                    if (sharedPref.getBoolean(Settings.DISPLAY_AP, true)) {
                        tvAp.setText(user.getAp() + " AP");
                        tvAp.setVisibility(View.VISIBLE);
                        tvUsername.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
        if (online.isAdded()) {
            online.clear();
            for (int i = 0; i < onlineList.size(); i++)
                online.addItem(onlineList.get(i));
            for (int i = 0; i < etaList.size(); i++)
                online.addItem(etaList.get(i));
        }
        if (events.isAdded()) {
            eventList = c_beam.getEvents();
            events.clear();
            if (eventList != null) {
                for (int i = 0; i < eventList.size(); i++)
                    events.addItem(eventList.get(i));
            }
        }

        if (missions.isAdded()) {
            ArrayList<Mission> missionList = new ArrayList<Mission>();
            missionList = c_beam.getMissions();
            missions.clear();
            for (int i = 0; i < missionList.size(); i++)
                missions.addItem(missionList.get(i));
        }

        if (c_portal.isAdded()) {
            articleList = c_beam.getArticles();
            c_portal.clear();
            for (int i = 0; i < articleList.size(); i++)
                c_portal.addItem(articleList.get(i));
        }

        if (artefacts.isAdded()) {
            ArrayList<Artefact> artefactList;
            artefactList = c_beam.getArtefacts();
            artefacts.clear();
            for (Artefact artefact : artefactList)
                artefacts.addItem(artefact);
        }
        activitylog.updateLog(c_beam.getActivityLog());
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
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
        menu.findItem(R.id.menu_c_mission).setVisible(mIsOnline);
        return true;
    }

    protected void setupViewPager() {
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        setupViewPagerIndicator(mViewPager);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
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
                if (position == USER_FRAGMENT) {
                    fragment = new UserListFragment();
                } else if (position == C_PORTAL_FRAGMENT) {
                    fragment = new C_portalListFragment();
                } else if (position == ARTEFACTS_FRAGMENT) {
                    fragment = new ArtefactListFragment();
                } else if (position == EVENTS_FRAGMENT) {
                    fragment = new EventListFragment();
                } else if (position == C_ONTROL_FRAGMENT) {
                    fragment = new C_ontrolFragment(c_beam);
                } else if (position == MISSION_FRAGMENT) {
                    fragment = new MissionListFragment();
                } else if (position == ACTIVITYLOG_FRAGMENT) {
                    fragment = new ActivitylogFragment();
                } else if (position == RINGINFO_FRAGMENT) {
                    fragment = new RinginfoFragment();
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
            return 7;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case USER_FRAGMENT:
                    return getString(R.string.title_users);
                case C_PORTAL_FRAGMENT:
                    return getString(R.string.title_c_portal);
                case ARTEFACTS_FRAGMENT:
                    return getString(R.string.title_artefacts);
                case EVENTS_FRAGMENT:
                    return getString(R.string.title_events);
                case C_ONTROL_FRAGMENT:
                    return getString(R.string.title_c_ontrol);
                case MISSION_FRAGMENT:
                    return getString(R.string.title_missions);
                case ACTIVITYLOG_FRAGMENT:
                    return getString(R.string.title_activity);
                case RINGINFO_FRAGMENT:
                    return getString(R.string.title_ringinfo);
            }
            return null;
        }
    }



    private boolean checkUserName() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        String defaultUsername = "bernd";
        String user = sharedPref.getString(Settings.USERNAME, defaultUsername);

        if (user.equals(defaultUsername) || user.length() == 0) {
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle(R.string.set_username_title);
            b.setMessage(R.string.set_username_message);
            b.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivityForResult(myIntent, 0);
                }
            });
            b.show();
            return false;
        }

        return true;
    }

}
