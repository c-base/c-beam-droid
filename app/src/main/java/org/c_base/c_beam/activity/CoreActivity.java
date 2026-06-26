package org.c_base.c_beam.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
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
    private static final int MATELIGHT_FRAGMENT = 4;
    private static final int RINGINFO_FRAGMENT = 5;

    SectionsPagerAdapter mSectionsPagerAdapter;

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

        ToggleButton button = findViewById(R.id.toggleLogin);
        for (User user: userList) {
            if(user.getUsername().equals(sharedPref.getString(Settings.USERNAME, "bernd"))) {
                if (button != null) {
                    button.setChecked(user.getStatus().equals("online"));
                    button.setEnabled(true);
                    if(sharedPref.getBoolean(Settings.DISPLAY_AP, true)) {
                        tvAp.setText(getString(R.string.ap_display, user.getAp()));
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
        ViewPager mViewPager = findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                try {
                    actionBar.setSelectedNavigationItem(position);
                } catch (Exception ignored) {

                }
            }
        });

        setupViewPagerIndicator(mViewPager);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final Fragment[] pages;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            pages = new Fragment[getCount()];
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a DummySectionFragment (defined as a static inner class
            // below) with the page number as its lone argument.
            Fragment fragment;
            if (pages[position] == null) {
                switch (position) {
                    case CONTROL_FRAGMENT:
                        fragment = new C_ontrolFragment(c_beam);
                        break;
                    case MEMBERINTERFACE_FRAGMENT:
                        fragment = new C_portalWebViewFragment();
                        ((C_portalWebViewFragment) fragment).setUrl(getString(R.string.memberinterface_url));
                        break;
                    case HYPERBLAST_FRAGMENT:
                        fragment = new C_portalWebViewFragment();
                        ((C_portalWebViewFragment) fragment).setUrl(getString(R.string.hyperblast_url));
                        break;
                    case MEGABLAST_FRAGMENT:
                        fragment = new C_portalWebViewFragment();
                        ((C_portalWebViewFragment) fragment).setUrl(getString(R.string.megablast_url));
                        break;
                    case MATELIGHT_FRAGMENT:
                        fragment = new C_portalWebViewFragment();
                        ((C_portalWebViewFragment) fragment).setUrl(getString(R.string.matelight_url));
                        break;
                    case RINGINFO_FRAGMENT:
                        fragment = new RinginfoFragment();
                        ((RinginfoFragment) fragment).setRing("core");
                        break;
                    default:
                        fragment = new Fragment();
                        break;
                }
                pages[position] = fragment;
            } else {

                fragment = pages[position];
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 6;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case CONTROL_FRAGMENT:
                    return getString(R.string.title_core_section1).toUpperCase();
                case MEMBERINTERFACE_FRAGMENT:
                    return getString(R.string.title_core_section2).toUpperCase();
                case HYPERBLAST_FRAGMENT:
                    return getString(R.string.title_core_section3).toUpperCase();
                case MEGABLAST_FRAGMENT:
                    return getString(R.string.title_core_section4).toUpperCase();
                case MATELIGHT_FRAGMENT:
                    return "matelight".toUpperCase();
                case RINGINFO_FRAGMENT:
                    return getString(R.string.title_ringinfo).toUpperCase();

            }
            return null;
        }
    }

}
