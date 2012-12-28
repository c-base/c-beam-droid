package org.c_base.c_beam;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
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
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	private Handler handler;

	C_beam c_beam = new C_beam();

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		c_beam.startThread();

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
				c_out();
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
				c_beam.register(regId, sharedPref.getString("pref_username", "dummy"));
			} else {
				c_beam.register_update(regId, sharedPref.getString("pref_username", "dummy"));
				Log.i("GCM", "Already registered");
			}
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
		Log.i("c_out", "got called");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("c_out durchsage");
		//.setTitle("Text:");
		LayoutInflater inflater = this.getLayoutInflater();

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(inflater.inflate(R.layout.dialog_c_out, null));
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Log.i("dialog",dialog.toString());
				//Log.i("c_out", "click: "+((EditText) findViewById(R.id.c_out_text)).getText().toString());
				//c_beam.tts(((EditText) findViewById(R.id.c_out_text)).getText().toString());
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User cancelled the dialog
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();		
	}
	public void updateLists() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

		//ArrayList<User> users = c_beam.getUsers();

		UserListFragment online = (UserListFragment) mSectionsPagerAdapter.getItem(0);
		online.setNextActivity(UserActivity.class);
		UserListFragment eta = (UserListFragment) mSectionsPagerAdapter.getItem(1);
		EventListFragment events = (EventListFragment) mSectionsPagerAdapter.getItem(2);
		MissionListFragment missions = (MissionListFragment) mSectionsPagerAdapter.getItem(3);

		ArrayList<User> onlineList = c_beam.getOnlineList();
		ArrayList<User> offlineList = c_beam.getOfflineList();
		ArrayList<User> etaList = c_beam.getEtaList();

		for (User user: c_beam.getUsers()) {
			if(user.getUsername().equals(sharedPref.getString("pref_username", "bernd"))) {
				ToggleButton button = (ToggleButton) findViewById(R.id.toggleLogin);
				if (button != null) {
					button.setChecked(user.getStatus().equals("online"));
				}
			}

		}
		try {
			if (online.isAdded()) {
				online.clear();
				for(int i=0; i<onlineList.size();i++)
					online.addItem(onlineList.get(i));
			}
			if (eta.isAdded()) {
				eta.clear();
				for(int i=0; i<etaList.size();i++)
					eta.addItem(etaList.get(i));
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
				ArrayList<Mission> missionList = c_beam.getMissions();
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
		ArrayListFragment[] pages;
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
			pages = new ArrayListFragment[getCount()];
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			//Fragment fragment = new DummySectionFragment();
			ArrayListFragment fragment;
			if (pages[position] == null) {
				if(position == 0) {
					fragment = new UserListFragment();
				} else if(position == 1) {
					fragment = new UserListFragment();
				} else if(position == 2) {
					fragment = new EventListFragment();
				} else if(position == 3) {
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
			return 4;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase();
			case 1:
				return getString(R.string.title_section2).toUpperCase();
			case 2:
				return getString(R.string.title_section3).toUpperCase();
			case 3:
				return getString(R.string.title_section4).toUpperCase();
			case 4:
				return getString(R.string.title_section5).toUpperCase();
			}
			return null;
		}
	}	

}

