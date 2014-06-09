package org.c_base.c_beam.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ToggleButton;

import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.domain.Article;
import org.c_base.c_beam.domain.User;
import org.c_base.c_beam.fragment.C_portalListFragment;
import org.c_base.c_beam.fragment.C_portalWebViewFragment;
import org.c_base.c_beam.fragment.RinginfoFragment;

import java.util.ArrayList;

/**
 * Created by smile on 2013-05-31.
 */
public class ComActivity extends RingActivity {
    private static final int C_PORTAL_FRAGMENT = 0;
    private static final int LOGBUCH_FRAGMENT = 1;
    private static final int COREDUMP_FRAGMENT = 4;
    private static final int CIMP_FRAGMENT = 2;
    private static final int RINGINFO_FRAGMENT = 3;

    ArrayList<Article> articleList;

    ViewPager mViewPager;
    SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setupOfflineArea();
        setupCbeamArea();
        setupActionBar();
        setupViewPager();

        initializeBroadcastReceiver();
    }

    public void updateLists() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        C_portalListFragment c_portal = (C_portalListFragment) mSectionsPagerAdapter.getItem(C_PORTAL_FRAGMENT);

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

        if(c_portal.isAdded()) {
            articleList = c_beam.getArticles();
            c_portal.clear();
            for(int i=0; i<articleList.size();i++)
                c_portal.addItem(articleList.get(i));
        }
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
                if(position == C_PORTAL_FRAGMENT) {
                    fragment = new C_portalListFragment();
                } else if(position == LOGBUCH_FRAGMENT) {
                    fragment = new C_portalWebViewFragment();
                    ((C_portalWebViewFragment) fragment).setUrl(getString(R.string.logbuch_url));
                } else if(position == COREDUMP_FRAGMENT) {
                    fragment = new C_portalWebViewFragment();
                    ((C_portalWebViewFragment) fragment).setUrl(getString(R.string.coredump_url));
                } else if(position == CIMP_FRAGMENT) {
                    fragment = new C_portalWebViewFragment();
                    ((C_portalWebViewFragment) fragment).setUrl(getString(R.string.cimp_url));
                } else if (position == RINGINFO_FRAGMENT) {
                    fragment = new RinginfoFragment("com");
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
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case C_PORTAL_FRAGMENT:
                    return getString(R.string.title_com_section1).toUpperCase();
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

}