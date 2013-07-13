package org.c_base.c_beam.activity;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ToggleButton;

import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.domain.Artefact;
import org.c_base.c_beam.domain.User;
import org.c_base.c_beam.fragment.ArtefactListFragment;
import org.c_base.c_beam.fragment.C_portalWebViewFragment;
import org.c_base.c_beam.fragment.RinginfoFragment;

import java.util.ArrayList;

/**
 * Created by smile on 2013-05-31.
 */
public class ClampActivity extends RingActivity {

    private static final int INTERFACE_MAP_FRAGMENT = 0;
    private static final int ARTEFACTS_FRAGMENT = 1;
    private static final int BLUEPRINT_MAP_FRAGMENT = 2;
    private static final int GOOGLE_MAP_FRAGMENT = 3;
    private static final int WWW_CBO_FRAGMENT = 5;
    private static final int RINGINFO_FRAGMENT = 4;


    enum RING {
        CLAMP, CARBON, CIENCE, CREACTIV, CULTURE, COM, CORE
    }

    private RING currentRing = RING.CLAMP;

    private static final int threadDelay = 5000;
    private static final int firstThreadDelay = 1000;
    private static final String TAG = "ClampActivity";
    private Handler handler = new Handler();
    protected Runnable fred;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link android.support.v4.view.ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String[] mDrawerItems;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private TypedArray mDrawerImages;

    private WifiBroadcastReceiver mWifiReceiver;
    private IntentFilter mWifiIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        // Show the Up button in the action bar.
        //actionBar.setDisplayHomeAsUpEnabled(true);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(
                getSupportFragmentManager());

        setupViewPager();
        setupNavigationDrawer();
        setupOfflineArea();
        setupCbeamArea();

        initializeBroadcastReceiver();
    }

    public void onStart() {
        Log.i(TAG, "onStart()");
        super.onStart();
        startProgress();
    }

    public void updateLists() {
        ArrayList<User> userList = c_beam.getUsers();
        ArrayList<User> onlineList = c_beam.getOnlineList();
        ArrayList<User> etaList = c_beam.getEtaList();
        ToggleButton button = (ToggleButton) findViewById(R.id.toggleLogin);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Log.i(TAG, "updateLists()");
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

        ArtefactListFragment artefacts = (ArtefactListFragment) mSectionsPagerAdapter.getItem(ARTEFACTS_FRAGMENT);
        if (artefacts.isAdded()) {
            ArrayList<Artefact> artefactList;
            artefactList = c_beam.getArtefacts();
            if (artefactList.size() != artefacts.size()) {
                artefacts.clear();
                for (int i = 0; i < artefactList.size(); i++)
                    artefacts.addItem(artefactList.get(i));
            }
        }

    }

    private void setupViewPager() {
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        setupViewPagerIndicator(mViewPager);
//        setupActionBarTabs();
    }

    /**
     * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a DummySectionFragment (defined as a static inner class
            // below) with the page number as its lone argument.
            Fragment fragment;
            Bundle args = new Bundle();
            if (position == INTERFACE_MAP_FRAGMENT) {
                fragment = new C_portalWebViewFragment();
                ((C_portalWebViewFragment) fragment).setUrl(getString(R.string.interface_map_url));
            } else if (position == BLUEPRINT_MAP_FRAGMENT) {
                fragment = new C_portalWebViewFragment();
                ((C_portalWebViewFragment) fragment).setUrl(getString(R.string.blueprint_map_url));
            } else if (position == ARTEFACTS_FRAGMENT) {
                fragment = new ArtefactListFragment();
            } else if (position == GOOGLE_MAP_FRAGMENT) {
                fragment = new C_portalWebViewFragment();
                ((C_portalWebViewFragment) fragment).setUrl(getString(R.string.google_map_url));
            } else if (position == WWW_CBO_FRAGMENT) {
                fragment = new C_portalWebViewFragment();
                ((C_portalWebViewFragment) fragment).setUrl(getString(R.string.www_cbo_url));
            } else if (position == RINGINFO_FRAGMENT) {
                fragment = new RinginfoFragment("clamp");
            } else {
                fragment = null;
            }

            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.title_clamp_section1).toUpperCase();
                case 1:
                    return getString(R.string.title_clamp_section2).toUpperCase();
                case 2:
                    return getString(R.string.title_clamp_section3).toUpperCase();
                case 3:
                    return getString(R.string.title_clamp_section4).toUpperCase();
                case WWW_CBO_FRAGMENT:
                    return getString(R.string.title_clamp_section5).toUpperCase();
                case RINGINFO_FRAGMENT:
                    return getString(R.string.title_ringinfo).toUpperCase();

            }
            return null;
        }
    }

}
