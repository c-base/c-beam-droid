package org.c_base.c_beam.activity;

import java.util.ArrayList;

import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.domain.NotificationDetails;
import org.c_base.c_beam.fragment.ArrayListFragment;
import org.c_base.c_beam.util.Helper;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
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
public class NotificationActivity extends C_beamActivity implements
ActionBar.TabListener, OnClickListener {
	private static final int MISSION_FRAGMENT = 2;
	private static final int BOARDING_FRAGMENT = 0;
	private static final int ETA_FRAGMENT = 1;

	private static final String NO_MESSAGES = "keine nachrichten";

	private static final String TAG = "NotificationActivity";

	private static ArrayList<String> missionList = new ArrayList<String>();
	private static ArrayList<String> boardingList = new ArrayList<String>();
	private static ArrayList<String> etaList = new ArrayList<String>();


	SectionsPagerAdapter mSectionsPagerAdapter;

	ViewPager mViewPager;
	EditText text;

	private View mNotificationArea;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (missionList.size() == 0) {
			missionList.add(NO_MESSAGES);
		}
		if (boardingList.size() == 0) {
			boardingList.add(NO_MESSAGES);
		}
		if (etaList.size() == 0) {
			etaList.add(NO_MESSAGES);
		}

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		setContentView(R.layout.activity_notification);

		mNotificationArea = findViewById(R.id.notification_area);

		setupActionBar();
		setupViewPager();
		
		Button buttonDeleteNotifications = (Button) findViewById(R.id.button_delete_notifications);
		buttonDeleteNotifications.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showConfirmation();
			}
		});

	}

	public void onStart() {
		Log.i(TAG, "onStart()");
		super.onStart();
	}

	public void showConfirmation() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.confirm_delete_notifications);
		builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				clear();
//				Intent myIntent = new Intent(getBaseContext(), MainActivity.class);
//				startActivityForResult(myIntent, 0);
				onBackPressed();
				mSectionsPagerAdapter.notifyDataSetChanged();
			}
		});
		builder.setNegativeButton(R.string.button_cancel, null);
		builder.create().show();
	}

	@Override
	protected void onPause() {
		Log.i(TAG, "onPause()");
		super.onPause();
	}

	@Override
	protected void onResume () {
		Log.i(TAG, "onResume()");
		super.onResume();

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		mNotificationArea.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// Hide some menu items when not connected to the crew network
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
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

	public static void addMissionNotification(String notification) {
		if (missionList.size() == 0 || missionList.get(0).contentEquals(NO_MESSAGES)) {
			missionList.clear();
		}
		missionList.add(notification);
	}

	public static void addBoardingNotification(String notification) {
		if (boardingList.size() == 0 || boardingList.get(0).contentEquals(NO_MESSAGES)) {
			boardingList.clear();
		}
		boardingList.add(notification);
	}

	public static void addETANotification(String notification) {
		if (etaList.size() == 0 || etaList.get(0).contentEquals(NO_MESSAGES)) {
			etaList.clear();
		}
		etaList.add(notification);
	}

	public static void clear() {
		missionList.clear();
		boardingList.clear();
		etaList.clear();
		missionList.add(NO_MESSAGES);
		etaList.add(NO_MESSAGES);
		boardingList.add(NO_MESSAGES);
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
			Fragment fragment;
			if (pages[position] == null) {

				switch (position) {
				case MISSION_FRAGMENT:
					fragment = new ArrayListFragment(missionList);
					break;
				case BOARDING_FRAGMENT:
					fragment = new ArrayListFragment(boardingList);
					break;
				case ETA_FRAGMENT:
					fragment = new ArrayListFragment(etaList);
					break;
				default:
					fragment = new ArrayListFragment();
					break;
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
			case MISSION_FRAGMENT:
				return getString(R.string.title_notification_mission).toUpperCase();
			case BOARDING_FRAGMENT:
				return getString(R.string.title_notification_boarding).toUpperCase();
			case ETA_FRAGMENT:
				return getString(R.string.title_notification_eta).toUpperCase();
			}
			return null;
		}
	}

	private void setupViewPager() {
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

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
	public void onClick(View v) {
		// TODO Auto-generated method stub
	}
}
