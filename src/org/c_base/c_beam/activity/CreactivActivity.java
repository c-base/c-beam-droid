package org.c_base.c_beam.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.domain.Mission;
import org.c_base.c_beam.domain.User;
import org.c_base.c_beam.fragment.ActivitylogFragment;
import org.c_base.c_beam.fragment.MissionListFragment;
import org.c_base.c_beam.fragment.RinginfoFragment;
import org.c_base.c_beam.fragment.StatsFragment;
import org.c_base.c_beam.util.Helper;

import java.util.ArrayList;

/**
 * Created by smile on 2013-05-31.
 */
public class CreactivActivity extends RingActivity implements
        View.OnClickListener {
    private static final int MISSIONLIST_FRAGMENT = 0;
    private static final int STATS_FRAGMENT = 2;
    private static final int ACTIVITYLOG_FRAGMENT = 1;
    private static final int RINGINFO_FRAGMENT = 3;

    private EditText activity_text;
    private EditText activity_ap;
    private Button button_log_activity;

    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private TextView tvAp = null;
    private TextView tvUsername = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_creactiv);

        mCbeamArea = findViewById(R.id.cbeam_area);

        setupOfflineArea();
        setupCbeamArea();
        setupViewPager();

        TextView textView = (TextView) findViewById(R.id.not_in_crew_network);
        Helper.setFont(this, textView);

        setupActionBar();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        tvAp = (TextView) findViewById(R.id.textView_ap);
        tvAp.setTextColor(Color.rgb(58, 182, 228));
        tvUsername = (TextView) findViewById(R.id.textView_username);
        tvUsername.setText(sharedPref.getString(Settings.USERNAME, "bernd"));
        tvUsername.setTextColor(Color.rgb(58, 182, 228));
        Helper.setFont(this, tvUsername);
        Helper.setFont(this, tvAp);
        boolean displayAp = sharedPref.getBoolean(Settings.DISPLAY_AP, true);
        if (!displayAp) {
            tvUsername.setHeight(0);
            tvAp.setHeight(0);
        }

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
        ToggleButton button = (ToggleButton) findViewById(R.id.toggleLogin);
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
            // below) wit    private static final int ACTIVITYLOG_FRAGMENT = 1;h the page number as its lone argument.
            Fragment fragment;
            if (pages[position] == null) {
                if (position == MISSIONLIST_FRAGMENT) {
                    fragment = new MissionListFragment();
                } else if (position == STATS_FRAGMENT) {
                    fragment = new StatsFragment();
                } else if (position == ACTIVITYLOG_FRAGMENT) {
                    fragment = new ActivitylogFragment();
                } else if (position == RINGINFO_FRAGMENT) {
                    fragment = new RinginfoFragment("creactiv");
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
                case MISSIONLIST_FRAGMENT:
                    return getString(R.string.title_missions).toUpperCase();
                case STATS_FRAGMENT:
                    return getString(R.string.title_stats).toUpperCase();
                case ACTIVITYLOG_FRAGMENT:
                    return getString(R.string.title_activity).toUpperCase();
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_log_activity: {
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
        c_beam.logactivity(activity_text.getText().toString(), activity_ap.getText().toString());
    }

}
