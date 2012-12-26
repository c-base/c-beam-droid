package org.c_base.c_beam;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alexd.jsonrpc.JSONRPCClient;
import org.alexd.jsonrpc.JSONRPCException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {

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



		// add extras here..
		handler = new Handler();

		ArrayListFragment online = (ArrayListFragment) mSectionsPagerAdapter.getItem(0);
		online.setNextActivity(UserActivity.class);
		//     	 ArrayListFragment eta = (ArrayListFragment) mSectionsPagerAdapter.getItem(1);
		//     	 ArrayListFragment events = (ArrayListFragment) mSectionsPagerAdapter.getItem(2);
		ArrayListFragment missions = (ArrayListFragment) mSectionsPagerAdapter.getItem(3);
		missions.setNextActivity(MissionActivity.class);
	}

	public void onStart() {
		super.onStart();
		startProgress();
		updateLists();
	}

	public void updateLists() {
		C_beam c_beam = new C_beam();
		ArrayList<User> users = c_beam.getUsers();

		UserListFragment online = (UserListFragment) mSectionsPagerAdapter.getItem(0);
		online.setNextActivity(UserActivity.class);
		ArrayListFragment eta = (ArrayListFragment) mSectionsPagerAdapter.getItem(1);
		ArrayListFragment events = (ArrayListFragment) mSectionsPagerAdapter.getItem(2);
		MissionListFragment missions = (MissionListFragment) mSectionsPagerAdapter.getItem(3);

		//		Log.i("handler",""+online.isAdded());
		//		Log.i("handler",""+eta.isAdded());
		//		Log.i("handler",""+events.isAdded());
		//		Log.i("handler",""+missions.isAdded());
		//Log.i("Users", users.toString());

		ArrayList<User> onlineList = new ArrayList<User>();
		ArrayList<User> offlineList = new ArrayList<User>();
		ArrayList<User> etaList = new ArrayList<User>();

		for (User user: users) {
			if (user.getStatus().equals("online")) {
				onlineList.add(user);
			}
			if (user.getStatus().equals("eta")) {
				etaList.add(user);
			}
			if (user.getStatus().equals("offline")) {
				offlineList.add(user);
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
					eta.addItem(etaList.get(i).getUsername() + " (" + etaList.get(i).getEta() + ")");
			}

			if (events.isAdded()){
				JSONArray eventsresult = c_beam.getEvents();
				events.clear();
				for(int i=0; i<eventsresult.length();i++)
					events.addItem(eventsresult.get(i).toString());
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
						Thread.sleep(10000);
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
				} else if(position == 3) {
					fragment = new MissionListFragment();

				} else {
					fragment = new ArrayListFragment();
				}
				fragment.setArguments(new Bundle());
				pages[position] = fragment;
				//Log.i("SectionsPagerAdapter","not found");
			} else {
				//Log.i("SectionsPagerAdapter","found");
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

