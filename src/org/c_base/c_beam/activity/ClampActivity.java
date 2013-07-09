package org.c_base.c_beam.activity;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.TitlePageIndicator;

import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.domain.Artefact;
import org.c_base.c_beam.domain.Ring;
import org.c_base.c_beam.domain.User;
import org.c_base.c_beam.fragment.ArtefactListFragment;
import org.c_base.c_beam.fragment.C_portalWebViewFragment;
import org.c_base.c_beam.util.Helper;

import java.text.NumberFormat;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ring);

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
//        updateTimePicker();
        setupCbeamArea();
    }

    private void setupNavigationDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setBackgroundColor(Color.argb(120, 0, 0, 0));

        mDrawerItems = getResources().getStringArray(R.array.drawer_items_array);
        mDrawerImages = getResources().obtainTypedArray(R.array.drawer_images_array);

        ArrayList<Ring> mRings = new ArrayList<Ring>();
        for (int i = 0; i < mDrawerItems.length; i++) {
            mRings.add(new Ring(mDrawerItems[i], mDrawerImages.getDrawable(i)));
        }

        mDrawerList.setAdapter(new RingAdapter(this, R.layout.drawer_list_item,
                R.id.drawer_list_item_textview, mRings));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());


        mTitle = mDrawerTitle = getTitle();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                // TODO Auto-generated method stub
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(mTitle);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.openDrawer(Gravity.LEFT);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
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
        handler.postDelayed(fred, firstThreadDelay);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
//        if (mDrawerToggle.onOptionsItemSelected(item)) {
//            return true;
//        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }


    public void updateLists() {
        Log.i(TAG, "updateLists()");
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

        setupViewPagerIndicator();
//        setupActionBarTabs();
    }

    private void setupViewPagerIndicator() {
        //Bind the title indicator to the adapter
        TitlePageIndicator titleIndicator = (TitlePageIndicator) findViewById(R.id.titles);
        titleIndicator.setViewPager(mViewPager);

        Helper.setFont(titleIndicator);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.titleIndicator.setOnPageChangeListener(mPageChangeListener);
        titleIndicator.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                try {
                    actionBar.setSelectedNavigationItem(position);
                } catch (Exception e) {

                }
            }
        });
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

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            selectItem(position);
        }
    }

    /**
     * Swaps fragments in the main content view
     */
    private void selectItem(int position) {

        /*
        // Create a new fragment and specify the planet to show based on position
        Fragment fragment = new PlanetFragment();
        Bundle args = new Bundle();
        args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
        fragment.setArguments(args);

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

        // Highlight the selected item, update the title, and close the drawer
        mDrawer.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawer);
        */
        setTitle(mDrawerItems[position]);
        System.out.println(mDrawerItems[position]);

    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    public class RingAdapter extends ArrayAdapter {
        private static final String TAG = "UserAdapter";
        private ArrayList<Ring> items;
        private Context context;

        @SuppressWarnings("unchecked")
        public RingAdapter(Context context, int itemLayout, int textViewResourceId, ArrayList<Ring> items) {
            super(context, itemLayout, textViewResourceId, items);
            this.context = context;
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View listview = super.getView(position, convertView, parent);

            TextView textView = (TextView) listview.findViewById(R.id.drawer_list_item_textview);
            Ring r = items.get(position);

            //Helper.setListItemStyle(view);
            //Helper.setFont(getActivity(), view);
            textView.setText(r.getName());

            ImageView b = (ImageView) listview.findViewById(R.id.drawer_ring_imageView);
            b.setImageDrawable(r.getImage());
            return listview;
        }

    }


}
