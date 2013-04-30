	package org.c_base.c_beam.activity;

import java.util.ArrayList;

import org.c_base.c_beam.GCMManager;
import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.domain.Artefact;
import org.c_base.c_beam.domain.Article;
import org.c_base.c_beam.domain.C_beam;
import org.c_base.c_beam.domain.Event;
import org.c_base.c_beam.domain.Mission;
import org.c_base.c_beam.domain.User;
import org.c_base.c_beam.fragment.AboutDialogFragment;
import org.c_base.c_beam.fragment.ActivitylogFragment;
import org.c_base.c_beam.fragment.ArtefactListFragment;
import org.c_base.c_beam.fragment.C_ontrolFragment;
import org.c_base.c_beam.fragment.C_portalListFragment;
import org.c_base.c_beam.fragment.EventListFragment;
import org.c_base.c_beam.fragment.MissionListFragment;
import org.c_base.c_beam.fragment.UserListFragment;
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
import android.net.wifi.WifiManager;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

@SuppressLint("NewApi")
public class MainActivity extends C_beamActivity implements
ActionBar.TabListener, OnClickListener {
	private static final int USER_FRAGMENT = 0;
	private static final int C_PORTAL_FRAGMENT = 1;
	private static final int ARTEFACTS_FRAGMENT = 6;
	private static final int EVENTS_FRAGMENT = 5;
	private static final int C_ONTROL_FRAGMENT = 2;
	private static final int MISSION_FRAGMENT = 3;
	private static final int ACTIVITYLOG_FRAGMENT = 4;

	private static final int threadDelay = 5000;
	private static final int firstThreadDelay = 1000;
	private static final String TAG = "MainActivity";

	private static final boolean debug = false;

	ArrayList<Article> articleList;
	ArrayList<Event> eventList;

	SectionsPagerAdapter mSectionsPagerAdapter;

	ViewPager mViewPager;
	private Handler handler = new Handler();
	EditText text;

	

	C_beam c_beam = C_beam.getInstance();
	
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

	@SuppressLint("NewApi")
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

		setContentView(R.layout.activity_main);

		mCbeamArea = findViewById(R.id.cbeam_area);

		mInfoArea = findViewById(R.id.info_area);
		TextView textView = (TextView) findViewById(R.id.not_in_crew_network);
		Helper.setFont(this, textView);

//		setupActionBar();
		setupViewPager();

		ToggleButton b = (ToggleButton) findViewById(R.id.toggleLogin);
		b.setOnClickListener(this);

		Button buttonC_out = (Button) findViewById(R.id.buttonC_out);
		buttonC_out.setOnClickListener(this);

		Button button_c_maps = (Button) findViewById(R.id.button_c_maps);
		button_c_maps.setOnClickListener(this);

		Button button_c_mission = (Button) findViewById(R.id.button_c_mission);
		button_c_mission.setOnClickListener(this);

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		tvAp = (TextView) findViewById(R.id.textView_ap);
		tvAp.setTextColor(Color.rgb(58, 182, 228));
		tvUsername = (TextView) findViewById(R.id.textView_username);
		tvUsername.setText(sharedPref.getString(Settings.USERNAME, "bernd"));
		tvUsername.setTextColor(Color.rgb(58, 182, 228));
		Helper.setFont(this, tvUsername);
		Helper.setFont(this, tvAp);
		boolean displayAp = sharedPref.getBoolean(Settings.DISPLAY_AP, true);
		if (!displayAp || tvAp.getText().equals("0 AP")) {
			//			tvUsername.setHeight(0);
			//			tvAp.setHeight(0);
			tvAp.setVisibility(View.GONE);
			tvUsername.setVisibility(View.GONE);
		}
		setupGCM();
		if (checkUserName() && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
			processNfcIntent(getIntent());
			toggleLogin();
		}

		initializeBroadcastReceiver();
	}

	void processNfcIntent(Intent intent) {
        System.out.println(intent.getData());
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
//		c_beam.toggleLogin(sharedPref.getString(Settings.USERNAME, "bernd"));
		if (c_beam.isLoggedIn(sharedPref.getString(Settings.USERNAME, "bernd"))) {
			showLogoutDialog();
		} else {
			showLoginDialog();
		}
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
		UserListFragment online = (UserListFragment) mSectionsPagerAdapter.getItem(USER_FRAGMENT);
		EventListFragment events = (EventListFragment) mSectionsPagerAdapter.getItem(EVENTS_FRAGMENT);
		MissionListFragment missions = (MissionListFragment) mSectionsPagerAdapter.getItem(MISSION_FRAGMENT);
		C_portalListFragment c_portal = (C_portalListFragment) mSectionsPagerAdapter.getItem(C_PORTAL_FRAGMENT);
		ArtefactListFragment artefacts = (ArtefactListFragment) mSectionsPagerAdapter.getItem(ARTEFACTS_FRAGMENT);
		ActivitylogFragment activitylog = (ActivitylogFragment) mSectionsPagerAdapter.getItem(ACTIVITYLOG_FRAGMENT);

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
			for(int i=0; i<onlineList.size();i++)
				online.addItem(onlineList.get(i));
			for(int i=0; i<etaList.size();i++)
				online.addItem(etaList.get(i));
		}
		if (events.isAdded()){
			eventList = c_beam.getEvents();
			events.clear();
			if (eventList != null) {
				for(int i=0; i<eventList.size();i++)
					events.addItem(eventList.get(i));
			}
		}

		if (missions.isAdded()){
			ArrayList<Mission> missionList = new ArrayList<Mission>();
			missionList = c_beam.getMissions();
			missions.clear();
			for(int i=0; i<missionList.size();i++)
				missions.addItem(missionList.get(i));
		}

		if(c_portal.isAdded()) {
			ArrayList<Article> tmpList = c_beam.getArticles();
			articleList = tmpList;
			c_portal.clear();
			for(int i=0; i<articleList.size();i++)
				c_portal.addItem(articleList.get(i));
		}

		if(artefacts.isAdded()) {
			ArrayList<Artefact> artefactList = new ArrayList<Artefact>();
			artefactList = c_beam.getArtefacts();
			if (artefactList.size() != artefacts.size()) {
				//artefacts.setArrayList(artefactList);
				artefacts.clear();
				for(int i=0; i<artefactList.size();i++)
					artefacts.addItem(artefactList.get(i));
			}
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
		menu.findItem(R.id.menu_c_mission).setVisible(mIsOnline);

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
				if(position == USER_FRAGMENT) {
					fragment = new UserListFragment();
				} else if(position == C_PORTAL_FRAGMENT) {
					fragment = new C_portalListFragment();
				} else if(position == ARTEFACTS_FRAGMENT) {
					fragment = new ArtefactListFragment();
				} else if(position == EVENTS_FRAGMENT) {
					fragment = new EventListFragment();
				} else if(position == C_ONTROL_FRAGMENT) {
					fragment = new C_ontrolFragment(c_beam);
				} else if(position == MISSION_FRAGMENT) {
					fragment = new MissionListFragment();
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
			return 7;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case USER_FRAGMENT:
				return getString(R.string.title_users);
			case C_PORTAL_FRAGMENT:
				return getString(R.string.title_c_portal);
			case ARTEFACTS_FRAGMENT:
				return getString(R.string.title_artefacts);
			case EVENTS_FRAGMENT:
				return getString(R.string.title_events);
			case C_ONTROL_FRAGMENT:
				return getString(R.string.title_c_ontrol);
			case MISSION_FRAGMENT:
				return getString(R.string.title_missions);
			case ACTIVITYLOG_FRAGMENT:
				return getString(R.string.title_activity);
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
			tab.setText(mSectionsPagerAdapter.getPageTitle(i));
			System.out.println(mSectionsPagerAdapter.getPageTitle(i));
			tab.setTabListener(this);
			actionBar.addTab(tab);
		}
	}

	private void setupGCM() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		if (sharedPref.getBoolean(Settings.PUSH, false)) {
			String registrationId = GCMManager.getRegistrationId(this);
			String username = sharedPref.getString(Settings.USERNAME, "bernd");
			c_beam.register_update(registrationId, username);
		}
	}

	private boolean checkUserName() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

		String defaultUsername = "bernd";
		String user = sharedPref.getString(Settings.USERNAME, defaultUsername);

		if (user.equals(defaultUsername) || user.length() == 0) {
			AlertDialog.Builder b = new AlertDialog.Builder(this);
			b.setTitle(R.string.set_username_title);
			b.setMessage(R.string.set_username_message);
			b.setPositiveButton("OK", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
					startActivityForResult(myIntent, 0);
				}
			});
			b.show();
			return false;
		}

		return true;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.toggleLogin: {
			ToggleButton b = (ToggleButton) view;
			if (b.isChecked()) {
				showLoginDialog();
			} else {
				showLogoutDialog();
			}
			break;
		}
		case R.id.buttonC_out: {
			startC_outActivity();
			break;
		}
		case R.id.button_c_maps: {
			startC_mapsActivity();
			break;
		}
		case R.id.button_c_mission: {
			startC_missionActivity();
			break;
		}
		}
	}

	private void startC_missionActivity() {
		Intent myIntent = new Intent(this, MissionActivity.class);
		startActivityForResult(myIntent, 0);
	}

	private void showLoginDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.confirm_login);
		builder.setPositiveButton(R.string.button_login, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				login();
			}
		});
		builder.setNegativeButton(R.string.button_cancel, null);
		builder.create().show();
	}

	private void showLogoutDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.confirm_logout);
		builder.setPositiveButton(R.string.button_logout, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				logout();
			}
		});
		builder.setNegativeButton(R.string.button_cancel, null);
		builder.create().show();
	}

	private void startC_outActivity() {
		Intent myIntent = new Intent(this, C_outActivity.class);
		startActivityForResult(myIntent, 0);
	}

	private void startC_mapsActivity() {
		Intent myIntent = new Intent(this, MapActivity.class);
		startActivityForResult(myIntent, 0);
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
