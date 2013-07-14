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
import org.c_base.c_beam.domain.User;
import org.c_base.c_beam.fragment.C_ontrolFragment;
import org.c_base.c_beam.fragment.C_portalWebViewFragment;
import org.c_base.c_beam.fragment.RinginfoFragment;

import java.util.ArrayList;

/**
 * Created by smile on 2013-05-31.
 */
public class CoreActivity extends RingActivity {

    private static final int CONTROL_FRAGMENT = 0;
    private static final int MEMBERINTERFACE_FRAGMENT = 1;
    private static final int HYPERBLAST_FRAGMENT = 2;
    private static final int MEGABLAST_FRAGMENT = 3;
    private static final int RINGINFO_FRAGMENT = 4;

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupOfflineArea();
        setupViewPager();
        setupCbeamArea();
        setupActionBar();
        initializeBroadcastReceiver();

    }

    public void updateLists() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

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

        setupViewPagerIndicator(mViewPager);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final Fragment[] pages;

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
                if (position == CONTROL_FRAGMENT) {
                    fragment = new C_ontrolFragment(c_beam);
                } else if (position == MEMBERINTERFACE_FRAGMENT) {
                    fragment = new C_portalWebViewFragment();
                    ((C_portalWebViewFragment) fragment).setUrl(getString(R.string.memberinterface_url));
                } else if (position == HYPERBLAST_FRAGMENT) {
                    fragment = new C_portalWebViewFragment();
                    ((C_portalWebViewFragment) fragment).setUrl(getString(R.string.hyperblast_url));
                } else if (position == MEGABLAST_FRAGMENT) {
                    fragment = new C_portalWebViewFragment();
                    ((C_portalWebViewFragment) fragment).setUrl(getString(R.string.megablast_url));
                } else if (position == RINGINFO_FRAGMENT) {
                    fragment = new RinginfoFragment("core");
                } else {
                    fragment = null;
                }
            } else {

                fragment = pages[position];
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.title_core_section1).toUpperCase();
                case 1:
                    return getString(R.string.title_core_section2).toUpperCase();
                case 2:
                    return getString(R.string.title_core_section3).toUpperCase();
                case 3:
                    return getString(R.string.title_core_section4).toUpperCase();
                case RINGINFO_FRAGMENT:
                    return getString(R.string.title_ringinfo).toUpperCase();

            }
            return null;
        }
    }

}