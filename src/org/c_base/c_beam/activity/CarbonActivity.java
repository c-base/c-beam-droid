package org.c_base.c_beam.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.domain.Article;
import org.c_base.c_beam.domain.Event;
import org.c_base.c_beam.domain.Mission;
import org.c_base.c_beam.domain.User;
import org.c_base.c_beam.fragment.*;
import org.c_base.c_beam.util.Helper;

import java.util.ArrayList;

/**
 * Created by smile on 2013-05-31.
 */
public class CarbonActivity extends RingActivity implements
        View.OnClickListener {
    private static final int USERLIST_FRAGMENT = 0;
    private static final int ETALIST_FRAGMENT = 1;

    private static final int threadDelay = 5000;
    private static final int firstThreadDelay = 1000;
    private static final String TAG = "CarbonActivity";

    private EditText activity_text;
    private EditText activity_ap;
    private Button button_log_activity;

    private static final boolean debug = false;

    ArrayList<Article> articleList;
    ArrayList<Event> eventList;

    SectionsPagerAdapter mSectionsPagerAdapter;

    ViewPager mViewPager;
    private Handler handler = new Handler();
    EditText text;

    protected Runnable fred;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mCbeamArea = findViewById(R.id.cbeam_area);

        setupOfflineArea();
        setupCbeamArea();
        setupActionBar();
        setupViewPager();
        setupActivityLogger();

        initializeBroadcastReceiver();
    }

    private void setupActivityLogger() {
        activity_text = (EditText) findViewById(R.id.edit_log_activity);
        activity_ap = (EditText) findViewById(R.id.edit_log_activity_ap);
        TextWatcher tw = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
                enableSubmitIfReady();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //		    	  enableSubmitIfReady();
            }
        };
    }

    private void enableSubmitIfReady() {
        if (activity_text.getText().length() > 0 && activity_ap.getText().length() > 0) {
            button_log_activity.setEnabled(true);
        } else {
            button_log_activity.setEnabled(false);
        }

    }

    public void updateLists() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        UserListFragment online = (UserListFragment) mSectionsPagerAdapter.getItem(USERLIST_FRAGMENT);
        UserListFragment eta = (UserListFragment) mSectionsPagerAdapter.getItem(ETALIST_FRAGMENT);

        ArrayList<User> onlineList = c_beam.getOnlineList();
        ArrayList<User> etaList = c_beam.getEtaList();
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
        if (online.isAdded()) {
            online.clear();
            for(int i=0; i<onlineList.size();i++) {
                online.addItem(onlineList.get(i));
            }
        }
        if (eta.isAdded()) {
            eta.clear();
            for(int i=0; i<etaList.size();i++) {
                eta.addItem(etaList.get(i));
            }
        }
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

        return true;
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
                if(position == USERLIST_FRAGMENT) {
                    fragment = new UserListFragment();
                } else if(position == ETALIST_FRAGMENT) {
                    fragment = new UserListFragment();
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
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case USERLIST_FRAGMENT:
                    return getString(R.string.title_carbon_section1).toUpperCase();
                case ETALIST_FRAGMENT:
                    return getString(R.string.title_carbon_section2).toUpperCase();
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_log_activity: {
                //			Button b = (Button) view;
                showLogActivityDialog();
                break;
            }
        }
    }

    private void showLogActivityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_log_activity);
        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                logActivity();
            }

        });
        builder.setNegativeButton(R.string.button_cancel, null);
        builder.create().show();
    }

    private void logActivity() {
//		String activity_text = ((EditText) findViewById(R.id.edit_log_activity)).getText().toString();
        Log.i("FOO", activity_text.getText().toString());
        c_beam.logactivity(activity_text.getText().toString(), activity_ap.getText().toString());
        // TODO Auto-generated method stub

    }

}
