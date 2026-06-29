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
import org.c_base.c_beam.domain.Artefact;
import org.c_base.c_beam.domain.User;
import org.c_base.c_beam.fragment.ArtefactListFragment;
import org.c_base.c_beam.fragment.WebViewFragment;
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
    private static final int WWW_CBO_FRAGMENT = 4;
    private static final int RINGINFO_FRAGMENT = 5;

    /**
     * The PagerAdapter that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

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
        setupCbeamArea();

        initializeBroadcastReceiver();
    }

    @Override
    public void updateLists() {
        ArrayList<User> userList = c_beam.getUsers();
        ToggleButton button = findViewById(R.id.toggleLogin);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        for (User user : userList) {
            if (user.getUsername().equals(sharedPref.getString(Settings.USERNAME, "bernd"))) {
                if (button != null) {
                    button.setChecked(user.getStatus().equals("online"));
                    button.setEnabled(true);
                    if (sharedPref.getBoolean(Settings.DISPLAY_AP, true)) {
                        tvAp.setText(getString(R.string.ap_display, user.getAp()));
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
        ViewPager mViewPager = findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        setupViewPagerIndicator(mViewPager);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
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
                    case INTERFACE_MAP_FRAGMENT:
                        fragment = new WebViewFragment();
                        ((WebViewFragment) fragment).setUrl(getString(R.string.interface_map_url));
                        break;
                    case BLUEPRINT_MAP_FRAGMENT:
                        fragment = new WebViewFragment();
                        ((WebViewFragment) fragment).setUrl(getString(R.string.blueprint_map_url));
                        break;
                    case ARTEFACTS_FRAGMENT:
                        fragment = new ArtefactListFragment();
                        break;
                    case GOOGLE_MAP_FRAGMENT:
                        fragment = new WebViewFragment();
                        ((WebViewFragment) fragment).setUrl(getString(R.string.osm_map_url));
                        break;
                    case WWW_CBO_FRAGMENT:
                        fragment = new WebViewFragment();
                        ((WebViewFragment) fragment).setUrl(getString(R.string.www_cbo_url));
                        break;
                    case RINGINFO_FRAGMENT:
                        fragment = new RinginfoFragment();
                        ((RinginfoFragment) fragment).setRing("clamp");
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
            return 6;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case INTERFACE_MAP_FRAGMENT:
                    return getString(R.string.title_clamp_section1).toUpperCase();
                case ARTEFACTS_FRAGMENT:
                    return getString(R.string.title_clamp_section2).toUpperCase();
                case BLUEPRINT_MAP_FRAGMENT:
                    return getString(R.string.title_clamp_section3).toUpperCase();
                case GOOGLE_MAP_FRAGMENT:
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
