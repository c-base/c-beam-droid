package org.c_base.c_beam;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.google.android.gcm.GCMRegistrar;

public class MainActivity extends FragmentActivity implements
ActionBar.TabListener {
	SectionsPagerAdapter mSectionsPagerAdapter;

	ViewPager mViewPager;
	private Handler handler;
	EditText text;

	C_beam c_beam = new C_beam();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		Log.i("FOOOOO", ""+Formatter.formatIpAddress(wifiManager.getDhcpInfo().ipAddress) );

		if (isInCrewNetwork()) {
			c_beam.startThread();
		} else {
			Log.i("c-beam", "not starting thread, not in crew network");
		}

		super.onCreate(savedInstanceState);

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		setContentView(R.layout.activity_main);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

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
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));

		}
		ToggleButton b = (ToggleButton) findViewById(R.id.toggleLogin);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ToggleButton b = (ToggleButton) v;
				if (b.isChecked()) {
					login();
				} else {
					logout();
				}

			}

		});

		Button buttonC_out = (Button) findViewById(R.id.buttonC_out);
		buttonC_out.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//c_out();
				Intent myIntent = new Intent(v.getContext(), C_outActivity.class);
				startActivityForResult(myIntent, 0);

			}

		});
		// add extras here..
		handler = new Handler();

		ArrayListFragment online = (ArrayListFragment) mSectionsPagerAdapter.getItem(0);
		online.setNextActivity(UserActivity.class);
		ArrayListFragment missions = (ArrayListFragment) mSectionsPagerAdapter.getItem(3);
		missions.setNextActivity(MissionActivity.class);

		if (sharedPref.getBoolean("pref_push", false)) {
			GCMRegistrar.checkDevice(this);
			GCMRegistrar.checkManifest(this);
			String regId = GCMRegistrar.getRegistrationId(this);
			if (regId.equals("")) {
				GCMRegistrar.register(this, "987966345562");
				regId = GCMRegistrar.getRegistrationId(this);
				if (isInCrewNetwork())
					c_beam.register(regId, sharedPref.getString("pref_username", "dummy"));
			} else {
				if (isInCrewNetwork())
					c_beam.register_update(regId, sharedPref.getString("pref_username", "dummy"));
				Log.i("GCM", "Already registered");
			}
		}

	}

	public boolean isInCrewNetwork() {
		WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

		if (Formatter.formatIpAddress(wifiManager.getDhcpInfo().ipAddress).startsWith("10.0.") && wifiManager.isWifiEnabled()) {
			return true;
		} else {
			// TODO: Display message 
			Log.i("c-beam", "not in crew network");
			return false;
		}

	}

	public void onStart() {
		super.onStart();
		startProgress();
		updateLists();
	}

	public void login() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		c_beam.force_login(sharedPref.getString("pref_username", "bernd"));
	}
	public void logout() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		c_beam.force_logout(sharedPref.getString("pref_username", "bernd"));
	}
	public void c_out() {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle("c_out-durchsage eingeben");
		final EditText input = new EditText(this);
		b.setView(input);
		b.setPositiveButton("OK", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int whichButton)
			{
				String result = input.getText().toString();
				c_beam.tts(result);
				Log.i("c_out", result);
			}
		});
		b.setNegativeButton("CANCEL", null);
		b.create().show();
	}

	public void updateLists() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

		//ArrayList<User> users = c_beam.getUsers();

		UserListFragment online = (UserListFragment) mSectionsPagerAdapter.getItem(0);
		online.setNextActivity(UserActivity.class);
		//UserListFragment eta = (UserListFragment) mSectionsPagerAdapter.getItem(1);
		EventListFragment events = (EventListFragment) mSectionsPagerAdapter.getItem(3);
		MissionListFragment missions = (MissionListFragment) mSectionsPagerAdapter.getItem(4);

		ArrayList<User> onlineList = c_beam.getOnlineList();
		//ArrayList<User> offlineList = c_beam.getOfflineList();
		ArrayList<User> etaList = c_beam.getEtaList();

		ToggleButton button = (ToggleButton) findViewById(R.id.toggleLogin);
		boolean found = false;
		for (User user: c_beam.getUsers()) {
			if(user.getUsername().equals(sharedPref.getString("pref_username", "bernd"))) {
				if (button != null) {
					button.setChecked(user.getStatus().equals("online"));
					button.setEnabled(true);
					found = true;
				}
			}
			if (!found) {
				if (button !=null)
					button.setEnabled(false);
			}

		}
		try {
			if (online.isAdded()) {
				online.clear();
				for(int i=0; i<onlineList.size();i++)
					online.addItem(onlineList.get(i));
				for(int i=0; i<etaList.size();i++)
					online.addItem(etaList.get(i));
			}
			if (events.isAdded()){
				JSONArray eventsresult = c_beam.getEvents();
				events.clear();
				if (eventsresult != null) { 
					for(int i=0; i<eventsresult.length();i++)
						events.addItem(new Event(eventsresult.get(i).toString()));
				}
			}

			if (missions.isAdded()){
				ArrayList<Mission> missionList = new ArrayList<Mission>();
				if (isInCrewNetwork())
					missionList = c_beam.getMissions();
				missions.clear();
				for(int i=0; i<missionList.size();i++)
					missions.addItem(missionList.get(i));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void startProgress() {
		// Do something long
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i >= 0; i++) {
					final int value = i;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					handler.post(new Runnable() {
						@Override
						public void run() {
							updateLists();
						}
					});
				}
			}
		};
		new Thread(runnable).start();
	}
	protected void onResume () {
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		if (item.getItemId() == R.id.menu_settings) {
			Intent myIntent = new Intent(this, SettingsActivity.class);
			startActivityForResult(myIntent, 0);
		} else if (item.getItemId() == R.id.menu_login) {
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			c_beam.login(sharedPref.getString("pref_username", "bernd"));
		} else if (item.getItemId() == R.id.menu_logout) {
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			c_beam.logout(sharedPref.getString("pref_username", "bernd"));
		} else if (item.getItemId() == R.id.menu_c_out) {
			Intent myIntent = new Intent(this, C_outActivity.class);
			startActivityForResult(myIntent, 0);
		}
		return super.onOptionsItemSelected(item);
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
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
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
			//Fragment fragment = new DummySectionFragment();
			Fragment fragment;
			if (pages[position] == null) {
				if(position == 0) {
					fragment = new UserListFragment();
				} else if(position == 5) {
					fragment = new C_ontrolFragment();
				} else if(position == 3) {
					fragment = new EventListFragment();
				} else if(position == 4) {
					fragment = new MissionListFragment();
				} else {
					fragment = new ArrayListFragment();
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
			return 6;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return getString(R.string.title_section0).toUpperCase();
			case 1:
				return getString(R.string.title_section2).toUpperCase();
			case 2:
				return getString(R.string.title_section3).toUpperCase();
			case 3:
				return getString(R.string.title_section4).toUpperCase();
			case 4:
				return getString(R.string.title_section5).toUpperCase();
			case 5:
				return getString(R.string.title_section6).toUpperCase();
			}
			return null;
		}
	}	

}

