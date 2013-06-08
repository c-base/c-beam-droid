package org.c_base.c_beam.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import org.c_base.c_beam.R;
import org.c_base.c_beam.domain.Artefact;
import org.c_base.c_beam.fragment.ArtefactListFragment;
import org.c_base.c_beam.fragment.C_portalWebViewFragment;

import java.util.ArrayList;

/**
 * Created by smile on 2013-05-31.
 */
public class ClampActivity extends RingActivity implements
        ActionBar.TabListener {

    private static final int INTERFACE_MAP_FRAGMENT = 0;
    private static final int ARTEFACTS_FRAGMENT = 1;
    private static final int BLUEPRINT_MAP_FRAGMENT = 2;
    private static final int GOOGLE_MAP_FRAGMENT = 3;
    private static final int WWW_CBO_FRAGMENT = 4;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clamp);

        // Set up the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        // Show the Up button in the action bar.
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(
                getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager
                .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        actionBar.setSelectedNavigationItem(position);
                    }
                });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(actionBar.newTab()
                    .setText(mSectionsPagerAdapter.getPageTitle(i))
                    .setTabListener(this));
        }

        setupOfflineArea();
//        updateTimePicker();
        setupCbeamArea();


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
        Log.i(TAG, "updateLists()");
        ArtefactListFragment artefacts = (ArtefactListFragment) mSectionsPagerAdapter.getItem(ARTEFACTS_FRAGMENT);
        if(artefacts.isAdded()) {
            ArrayList<Artefact> artefactList;
            artefactList = c_beam.getArtefacts();
            if (artefactList.size() != artefacts.size()) {
                artefacts.clear();
                for(int i=0; i<artefactList.size();i++)
                    artefacts.addItem(artefactList.get(i));
            }
        }

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
            } else {
                fragment = new DummySectionFragment();
                args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
            }

            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 4;
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
                case 4:
                    return getString(R.string.title_clamp_section5).toUpperCase();
            }
            return null;
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public static class DummySectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public DummySectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Create a new TextView and set its text to the fragment's section
            // number argument value.
            TextView textView = new TextView(getActivity());
            textView.setGravity(Gravity.CENTER);
            textView.setText(Integer.toString(getArguments().getInt(
                    ARG_SECTION_NUMBER)));
            return textView;
        }
    }

}
