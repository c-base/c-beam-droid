package org.c_base.c_beam.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.View;
import android.widget.ToggleButton;

import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.domain.User;
import org.c_base.c_beam.fragment.WebViewFragment;
import org.c_base.c_beam.fragment.RinginfoFragment;

import java.util.ArrayList;

/**
 * Created by smile on 2013-05-31.
 */
public class ComActivity extends RingActivity {
    private static final int LOGBUCH_FRAGMENT = 0;
    private static final int CIMP_FRAGMENT = 1;
    private static final int COREDUMP_FRAGMENT = 2;
    private static final int RINGINFO_FRAGMENT = 3;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setupOfflineArea();
        setupCbeamArea();
        setupViewPager();

        initializeBroadcastReceiver();
    }

    @Override
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

    /**
     * A {@link androidx.fragment.app.FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        Fragment[] pages;
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            pages = new Fragment[getCount()];
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            if (pages[position] == null) {
                switch (position) {
                    case LOGBUCH_FRAGMENT:
                        fragment = new WebViewFragment();
                        ((WebViewFragment) fragment).setUrl(getString(R.string.logbuch_url));
                        break;
                    case CIMP_FRAGMENT:
                        fragment = new WebViewFragment();
                        ((WebViewFragment) fragment).setUrl(getString(R.string.cimp_url));
                        break;
                    case COREDUMP_FRAGMENT:
                        fragment = new WebViewFragment();
                        ((WebViewFragment) fragment).setUrl(getString(R.string.coredump_url));
                        break;
                    case RINGINFO_FRAGMENT:
                        fragment = new RinginfoFragment();
                        ((RinginfoFragment) fragment).setRing("com");
                        break;
                    default:
                        fragment = new Fragment();
                        break;
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
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case LOGBUCH_FRAGMENT:
                    return getString(R.string.title_com_section2).toUpperCase();
                case CIMP_FRAGMENT:
                    return getString(R.string.title_com_section3).toUpperCase();
                case COREDUMP_FRAGMENT:
                    return getString(R.string.title_com_section4).toUpperCase();
                case RINGINFO_FRAGMENT:
                    return getString(R.string.title_ringinfo).toUpperCase();
            }
            return null;
        }
    }

    private void setupViewPager() {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        ViewPager mViewPager = findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                try {
                    if (actionBar != null) {
                        actionBar.setSelectedNavigationItem(position);
                    }
                } catch (Exception ignored) {
                }
            }
        });

        setupViewPagerIndicator(mViewPager);
    }

}
