package org.c_base.c_beam.activity;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
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
public class CreactivActivity extends RingActivity {
    private static final int MISSIONLIST_FRAGMENT = 0;
    private static final int ACTIVITYLOG_FRAGMENT = 1;
    private static final int STATS_FRAGMENT = 2;
    private static final int RINGINFO_FRAGMENT = 3;

    private EditText activity_text;
    private EditText activity_ap;
    private Button button_log_activity;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_creactiv);

        mCbeamArea = findViewById(R.id.cbeam_area);

        setupOfflineArea();
        setupCbeamArea();
        setupViewPager();

        TextView textView = findViewById(R.id.not_in_crew_network);
        if (textView != null) {
            Helper.setFont(this, textView);
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        tvAp = findViewById(R.id.textView_ap);
        tvAp.setTextColor(Color.rgb(58, 182, 228));
        tvUsername = findViewById(R.id.textView_username);
        tvUsername.setText(sharedPref.getString(Settings.USERNAME, "bernd"));
        tvUsername.setTextColor(Color.rgb(58, 182, 228));
        Helper.setFont(this, tvUsername);
        Helper.setFont(this, tvAp);
        boolean displayAp = sharedPref.getBoolean(Settings.DISPLAY_AP, true);
        if (!displayAp) {
            tvUsername.setVisibility(View.GONE);
            tvAp.setVisibility(View.GONE);
        }

        activity_text = findViewById(R.id.edit_log_activity);
        activity_ap = findViewById(R.id.edit_log_activity_ap);
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

        button_log_activity = findViewById(R.id.button_log_activity);
        button_log_activity.setOnClickListener(v -> showLogActivityDialog());
        button_log_activity.setEnabled(false);

        initializeBroadcastReceiver();
    }

    private void enableSubmitIfReady() {
        button_log_activity.setEnabled(activity_text.getText().length() > 0 && activity_ap.getText().length() > 0);
    }

    @Override
    public void updateLists() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        MissionListFragment missions = (MissionListFragment) mSectionsPagerAdapter.getItem(MISSIONLIST_FRAGMENT);
        StatsFragment statsFragment = (StatsFragment) mSectionsPagerAdapter.getItem(STATS_FRAGMENT);
        ActivitylogFragment activitylog = (ActivitylogFragment) mSectionsPagerAdapter.getItem(ACTIVITYLOG_FRAGMENT);

        ArrayList<User> userList = c_beam.getUsers();
        ToggleButton button = findViewById(R.id.toggleLogin);
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
        if (missions.isAdded()) {
            ArrayList<Mission> missionList = c_beam.getMissions();
            missions.clear();
            for (Mission m : missionList)
                missions.addItem(m);
        }
        if (statsFragment.isAdded()) {
            statsFragment.clear();
            for (User user : c_beam.getStats())
                statsFragment.addItem(user);
        }
        activitylog.updateLog(c_beam.getActivityLog());
    }

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
                    case MISSIONLIST_FRAGMENT:
                        fragment = new MissionListFragment();
                        break;
                    case STATS_FRAGMENT:
                        fragment = new StatsFragment();
                        break;
                    case ACTIVITYLOG_FRAGMENT:
                        fragment = new ActivitylogFragment();
                        break;
                    case RINGINFO_FRAGMENT:
                        fragment = new RinginfoFragment();
                        ((RinginfoFragment) fragment).setRing("creactiv");
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

    private void showLogActivityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_log_activity);
        builder.setPositiveButton(R.string.button_ok, (dialog, whichButton) -> logActivity());
        builder.setNegativeButton(R.string.button_cancel, null);
        builder.create().show();
    }

    private void logActivity() {
        c_beam.logactivity(activity_text.getText().toString(), activity_ap.getText().toString());
        activity_text.setText("");
        activity_ap.setText("");
    }

}
