package org.c_base.c_beam.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.domain.Mission;
import org.c_base.c_beam.domain.User;
import org.c_base.c_beam.fragment.ActivitylogFragment;
import org.c_base.c_beam.fragment.MissionListFragment;
import org.c_base.c_beam.fragment.StatsFragment;

import java.util.ArrayList;

@SuppressLint("NewApi")
public class MissionActivity extends RingActivity implements
        OnClickListener {
    private static final int MISSIONLIST_FRAGMENT = 0;
    private static final int STATS_FRAGMENT = 2;
    private static final int ACTIVITYLOG_FRAGMENT = 1;

    private EditText activity_text;
    private EditText activity_ap;
    private Button button_log_activity;

    ViewPager mViewPager;
    SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_creactiv);

        mCbeamArea = findViewById(R.id.cbeam_area);

        setupOfflineArea();
        setupActionBar();
        setupCbeamArea();
        setupViewPager();
        setupGCM();

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
        activity_text.addTextChangedListener(tw);
        activity_ap.addTextChangedListener(tw);

        button_log_activity = (Button) findViewById(R.id.button_log_activity);
        button_log_activity.setOnClickListener(this);
        button_log_activity.setEnabled(false);

        initializeBroadcastReceiver();
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
        MissionListFragment missions = (MissionListFragment) mSectionsPagerAdapter.getItem(MISSIONLIST_FRAGMENT);
        StatsFragment stats = (StatsFragment) mSectionsPagerAdapter.getItem(STATS_FRAGMENT);
        ActivitylogFragment activitylog = (ActivitylogFragment) mSectionsPagerAdapter.getItem(ACTIVITYLOG_FRAGMENT);

        ArrayList<User> userList = c_beam.getUsers();
        for (User user : userList) {
            if (user.getUsername().equals(sharedPref.getString(Settings.USERNAME, "bernd"))) {
                tvAp.setText(user.getAp() + " AP");
            }
        }
        if (missions.isAdded()) {
            ArrayList<Mission> missionList = new ArrayList<Mission>();
            missionList = c_beam.getMissions();
            missions.clear();
            for (int i = 0; i < missionList.size(); i++)
                missions.addItem(missionList.get(i));
        }
        if (stats.isAdded()) {
            stats.clear();
            for (User user : c_beam.getStats())
                stats.addItem(user);
        }
        activitylog.updateLog(c_beam.getActivityLog());
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
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
                if (position == MISSIONLIST_FRAGMENT) {
                    fragment = new MissionListFragment();
                } else if (position == STATS_FRAGMENT) {
                    fragment = new StatsFragment();
                } else if (position == ACTIVITYLOG_FRAGMENT) {
                    fragment = new ActivitylogFragment();
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
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case MISSIONLIST_FRAGMENT:
                    return getString(R.string.title_missions).toUpperCase();
                case STATS_FRAGMENT:
                    return getString(R.string.title_stats).toUpperCase();
                case ACTIVITYLOG_FRAGMENT:
                    return getString(R.string.title_activity).toUpperCase();
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
        c_beam.logactivity(activity_text.getText().toString(), activity_ap.getText().toString());
        // TODO Auto-generated method stub

    }

}
