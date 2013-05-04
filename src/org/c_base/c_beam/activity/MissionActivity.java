package org.c_base.c_beam.activity;

import java.util.ArrayList;

import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.domain.Article;
import org.c_base.c_beam.domain.Event;
import org.c_base.c_beam.domain.Mission;
import org.c_base.c_beam.domain.User;
import org.c_base.c_beam.fragment.ActivitylogFragment;
import org.c_base.c_beam.fragment.MissionListFragment;
import org.c_base.c_beam.fragment.StatsFragment;
import org.c_base.c_beam.util.Helper;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.Menu;

@SuppressLint("NewApi")
public class MissionActivity extends C_beamActivity implements
ActionBar.TabListener, OnClickListener {
	private static final int MISSIONLIST_FRAGMENT = 0;
	private static final int STATS_FRAGMENT = 2;
	private static final int ACTIVITYLOG_FRAGMENT = 1;

	private static final int threadDelay = 5000;
	private static final int firstThreadDelay = 1000;
	private static final String TAG = "MissionActivity";

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
	private View mInfoArea;
	private View mCbeamArea;
	private boolean mIsOnline = false;
	private WifiBroadcastReceiver mWifiReceiver;
	private IntentFilter mWifiIntentFilter;

	TextView tvAp = null;
	TextView tvUsername = null;

	public void setOnline() {
		if (android.os.Build.VERSION.SDK_INT > 13) {
			actionBar.setIcon(R.drawable.ic_launcher_c_beam_online);
		}
	}

	public void setOffline() {
		if (android.os.Build.VERSION.SDK_INT > 13) {
			actionBar.setIcon(R.drawable.ic_launcher);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		setContentView(R.layout.activity_mission);

		mCbeamArea = findViewById(R.id.cbeam_area);

		mInfoArea = findViewById(R.id.info_area);
		TextView textView = (TextView) findViewById(R.id.not_in_crew_network);
		Helper.setFont(this, textView);

		setupActionBar();
		setupViewPager();

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

		//		TextView labelAp = (TextView) findViewById(R.id.label_ap);
		//		Helper.setFont(this, labelAp);

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

	public void onStart() {
		Log.i(TAG, "onStart()");
		super.onStart();
		startProgress();
	}


	@Override
	protected void onPause() {
		Log.i(TAG, "onPause()");
		unregisterReceiver(mWifiReceiver);
		stopNetworkingThreads();
		super.onPause();
	}

	public void toggleLogin() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		c_beam.toggleLogin(sharedPref.getString(Settings.USERNAME, "bernd"));
	}
	public void login() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		c_beam.force_login(sharedPref.getString(Settings.USERNAME, "bernd"));
	}
	public void logout() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		c_beam.force_logout(sharedPref.getString(Settings.USERNAME, "bernd"));
	}

	public void updateLists() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		MissionListFragment missions = (MissionListFragment) mSectionsPagerAdapter.getItem(MISSIONLIST_FRAGMENT);
		StatsFragment stats = (StatsFragment) mSectionsPagerAdapter.getItem(STATS_FRAGMENT);
		ActivitylogFragment activitylog = (ActivitylogFragment) mSectionsPagerAdapter.getItem(ACTIVITYLOG_FRAGMENT);

		ArrayList<User> userList = c_beam.getUsers();
//		boolean found = false;
		for (User user: userList) {
			if(user.getUsername().equals(sharedPref.getString(Settings.USERNAME, "bernd"))) {
//				found = true;
				tvAp.setText(user.getAp()+" AP");
			}
		}
		if (missions.isAdded()){
			ArrayList<Mission> missionList = new ArrayList<Mission>();
			missionList = c_beam.getMissions();
			missions.clear();
			for(int i=0; i<missionList.size();i++)
				missions.addItem(missionList.get(i));
		}
		if(stats.isAdded()) {
			stats.clear();
			for(User user: c_beam.getStats())
				stats.addItem(user);
		}
		activitylog.updateLog(c_beam.getActivityLog());
	}

	public void startProgress() {
		// Do something long
		fred = new Runnable() {
			@Override
			public void run() {
				updateLists();
				if (c_beam.isInCrewNetwork())
					setOnline();
				else
					setOffline();
				handler.postDelayed(fred, threadDelay);
			}

		};
		handler.postDelayed(fred, firstThreadDelay );
	}

	protected void onResume () {
		Log.i(TAG, "onResume()");
		super.onResume();

		registerReceiver(mWifiReceiver, mWifiIntentFilter);

		if (c_beam.isInCrewNetwork()) {
			switchToOnlineMode();
		} else {
			switchToOfflineMode();
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// Hide some menu items when not connected to the crew network
		menu.findItem(R.id.menu_login).setVisible(mIsOnline);
		menu.findItem(R.id.menu_logout).setVisible(mIsOnline);
		menu.findItem(R.id.menu_map).setVisible(mIsOnline);
		menu.findItem(R.id.menu_c_out).setVisible(mIsOnline);

		return true;
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
				if(position == MISSIONLIST_FRAGMENT) {
					fragment = new MissionListFragment();
				} else if(position == STATS_FRAGMENT) {
					fragment = new StatsFragment();
				} else if(position == ACTIVITYLOG_FRAGMENT) {
					fragment = new ActivitylogFragment();
				} else {
					fragment = null;
				}
				fragment.setArguments(new Bundle());
				pages[position] = fragment;
			} else {

				fragment = pages[position];
			}
			return (Fragment) fragment;
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

		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			Tab tab = actionBar.newTab();
			TextView t = new TextView(getApplicationContext());
			t.setTypeface(Typeface.createFromAsset(getAssets(), "CEVA-CM.TTF"));
			tab.setText(mSectionsPagerAdapter.getPageTitle(i));
			tab.setTabListener(this);
			actionBar.addTab(tab);
		}
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

	private void switchToOfflineMode() {
		mIsOnline = false;
		showOfflineView();
		stopNetworkingThreads();
	}

	private void switchToOnlineMode() {
		mIsOnline = true;
		showOnlineView();
		startNetworkingThreads();
	}

	private void startNetworkingThreads() {
		c_beam.startThread();
		updateLists();
	}

	private void stopNetworkingThreads() {
		c_beam.stopThread();
	}

	private void showOfflineView() {
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		mCbeamArea.setVisibility(View.GONE);
		mInfoArea.setVisibility(View.VISIBLE);
	}

	private void showOnlineView() {
		mIsOnline = true;
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		mInfoArea.setVisibility(View.GONE);
		mCbeamArea.setVisibility(View.VISIBLE);
	}

	private void initializeBroadcastReceiver() {
		mWifiReceiver = new WifiBroadcastReceiver();
		mWifiIntentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
	}


	class WifiBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (debug) {
				showOnlineView();
				return;
			}
			if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
				int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
				int previousState = intent.getIntExtra(WifiManager.EXTRA_PREVIOUS_WIFI_STATE, -1);

				if (state == previousState) {
					return;
				}

				if (state == WifiManager.WIFI_STATE_ENABLED && c_beam.isInCrewNetwork()) {
					showOnlineView();
				} else if (mIsOnline) {
					showOfflineView();
				}
			}
		}
	}
}
