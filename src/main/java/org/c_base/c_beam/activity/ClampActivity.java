package org.c_base.c_beam.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
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

    private RING currentRing = RING.CLAMP;

    private static final String TAG = "ClampActivity";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(
                getSupportFragmentManager());

        setupViewPager();
        setupNavigationDrawer();
        setupOfflineArea();
        setupActionBar();
        setupCbeamArea();

        initializeBroadcastReceiver();
    }

    public void updateLists() {
        ArrayList<User> userList = c_beam.getUsers();
        ToggleButton button = (ToggleButton) findViewById(R.id.toggleLogin);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
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

        ArtefactListFragment artefacts = (ArtefactListFragment) mSectionsPagerAdapter.getItem(ARTEFACTS_FRAGMENT);
        if (artefacts.isAdded()) {
            ArrayList<Artefact> artefactList;
            artefactList = c_beam.getArtefacts();
            artefacts.clear();
            for (Artefact artefact : artefactList)
                artefacts.addItem(artefact);
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
    }

    /**
     * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
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
                fragment.setArguments(new Bundle());
                pages[position] = fragment;
            } else {

                fragment = pages[position];
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
