package org.c_base.c_beam.activity;

import java.util.ArrayList;

import org.c_base.c_beam.R;
import org.c_base.c_beam.domain.Artefact;
import org.c_base.c_beam.domain.Article;
import org.c_base.c_beam.domain.C_beam;
import org.c_base.c_beam.domain.Event;
import org.c_base.c_beam.domain.Mission;
import org.c_base.c_beam.domain.User;
import org.c_base.c_beam.fragment.ArtefactListFragment;
import org.c_base.c_beam.fragment.C_ontrolFragment;
import org.c_base.c_beam.fragment.C_portalListFragment;
import org.c_base.c_beam.fragment.EventListFragment;
import org.c_base.c_beam.fragment.MissionListFragment;
import org.c_base.c_beam.fragment.UserListFragment;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gcm.GCMRegistrar;

@SuppressLint("NewApi")
public class MainActivity extends SherlockFragmentActivity implements
ActionBar.TabListener {
	private static final int USER_FRAGMENT = 0;
	private static final int C_PORTAL_FRAGMENT = 1;
	private static final int ARTEFACTS_FRAGMENT = 2;
	private static final int EVENTS_FRAGMENT = 3;
	private static final int C_ONTROL_FRAGMENT = 4;
	private static final int MISSION_FRAGMENT = 5;

	private static final int threadDelay = 5000;
	private static final int firstThreadDelay = 1000;
	private static final String TAG = "MainActivity";
	
	Activity activity;

	ArrayList<Article> articleList;
	ArrayList<Event> eventList;

	//	SectionsPagerAdapter mSectionsPagerAdapter;
	SectionsPagerAdapter mSectionsPagerAdapter;

	ViewPager mViewPager;
	private Handler handler;
	EditText text;

	ActionBar actionBar;

	C_beam c_beam = new C_beam(this);

	protected Runnable fred;


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
		activity = this;

		//c_beam.startThread();

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		setContentView(R.layout.activity_main);

		// Set up the action bar.
		actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		//		BEGIN TEST Custom Font in Actionbar TEST BEGIN

		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);

		LayoutInflater inflator = (LayoutInflater)this.getSystemService(getApplicationContext().LAYOUT_INFLATER_SERVICE);
		View v = inflator.inflate(R.layout.view_actionbar, null);

		((TextView)v.findViewById(R.id.title)).setText(this.getTitle());
		((TextView)v.findViewById(R.id.title)).setTypeface(Typeface.createFromAsset(getAssets(), "CEVA-CM.TTF"));
		((TextView)v.findViewById(R.id.title)).setTextSize(30);
		((TextView)v.findViewById(R.id.title)).setPadding(10, 20, 10, 20);
		actionBar.setCustomView(v);
		
//		END TEST Custom Font in Actionbar TEST END

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		
		String defaultUsername = "bernd";getString(R.string.pref_username);
		String user = sharedPref.getString("pref_username", defaultUsername);
		Log.i(TAG, "username"+defaultUsername);
		if (user.equals(defaultUsername) || user.isEmpty()) {
			AlertDialog.Builder b = new AlertDialog.Builder(v.getContext());
			b.setTitle(R.string.set_username_title);
			b.setMessage(R.string.set_username_message);
			b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent myIntent = new Intent(activity, SettingsActivity.class);
					startActivityForResult(myIntent, 0);
				}
			});
			b.show();
		}
		
		//		ViewPagerAdapter adapter = new ViewPagerAdapter( this );
		//	    ViewPager pager =
		//	        (ViewPager)findViewById( R.id.pager );
		//	    pager.setAdapter( adapter );

		//		mSectionsPagerAdapter = new MainPagerAdapter(
		//				getSupportFragmentManager());
		//
		//		
		//		
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
			Tab tab = actionBar.newTab();
			TextView t = new TextView(getApplicationContext());
			t.setTypeface(Typeface.createFromAsset(getAssets(), "CEVA-CM.TTF"));
			tab.setText(mSectionsPagerAdapter.getPageTitle(i));
			//			tab.setCustomView(t);
			tab.setTabListener(this);
			actionBar.addTab(tab);

		}
		ToggleButton b = (ToggleButton) findViewById(R.id.toggleLogin);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ToggleButton b = (ToggleButton) v;
				AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

				if (b.isChecked()) {
					builder.setTitle(R.string.confirm_login);
					builder.setPositiveButton(R.string.button_login, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int whichButton)
						{
							//							Toast.makeText(v.getContext(), "Du wirst eingelogt, bitte warten", Toast.LENGTH_LONG).show();
							login();
						}
					});
					builder.setNegativeButton(R.string.button_cancel, null);
					builder.create().show();

				} else {
					builder.setTitle(R.string.confirm_logout);
					builder.setPositiveButton(R.string.button_logout, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int whichButton)
						{
							// Toast.makeText(v.getContext(), "Du wirst eingelogt, bitte warten", Toast.LENGTH_LONG).show();
							logout();
						}
					});
					builder.setNegativeButton(R.string.button_cancel, null);
					builder.create().show();
				}

			}

		});

		Button buttonC_out = (Button) findViewById(R.id.buttonC_out);
		buttonC_out.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(v.getContext(), C_outActivity.class);
				startActivityForResult(myIntent, 0);

			}

		});

		Button button_c_maps = (Button) findViewById(R.id.button_c_maps);
		button_c_maps.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(v.getContext(), MapActivity.class);
				startActivityForResult(myIntent, 0);
			}

		});
		// add extras here..
		handler = new Handler();

		//		ArrayListFragment online = (ArrayListFragment) mSectionsPagerAdapter.getItem(0);
		//		online.setNextActivity(UserActivity.class);
		//		ArrayListFragment missions = (ArrayListFragment) mSectionsPagerAdapter.getItem(MISSION_FRAGMENT);
		//		missions.setNextActivity(MissionActivity.class);

		if (sharedPref.getBoolean("pref_push", false)) {
			GCMRegistrar.checkDevice(this);
			GCMRegistrar.checkManifest(this);
			String regId = GCMRegistrar.getRegistrationId(this);
			if (regId.equals("")) {
				GCMRegistrar.register(this, "987966345562");
				regId = GCMRegistrar.getRegistrationId(this);
				Log.i("GCM", "registering " + regId); 
				c_beam.register(regId, sharedPref.getString("pref_username", "dummy"));
			} else {
				c_beam.register_update(regId, sharedPref.getString("pref_username", "dummy"));
				Log.i("GCM", "Already registered"); 
			}
		}
		//		String font = sharedPref.getString("pref_font", "Android Default");
		//		if (font.equals("Default Android")) {
		//			
		//		} else if (font.equals("X-Scale")) {
		//			Typeface myTypeface = Typeface.createFromAsset(getAssets(), "X-SCALE.TTF");
		//			final ViewGroup mContainer = (ViewGroup) findViewById(
		//					android.R.id.content).getRootView();
		//			setAppFont(mContainer, myTypeface);
		//		} else if (font.equals("Ceva")) {	
		//			Typeface myTypeface = Typeface.createFromAsset(getAssets(), "CEVA-CM.TTF");
		//			final ViewGroup mContainer = (ViewGroup) findViewById(
		//					android.R.id.content).getRootView();
		//			setAppFont(mContainer, myTypeface);
		//			
		//		}
	}

	public void onStart() {
		Log.i(TAG, "onStart()");
		super.onStart();
		startProgress();
		updateLists();
	}


	@Override
	protected void onPause() {
		Log.i(TAG, "onPause()");
		super.onPause();
		//		thread.interrupt();
		c_beam.stopThread();
	}

	@Override
	protected void onStop() {
		Log.i(TAG, "onStop()");
		super.onStop();
		//		thread.interrupt();
		c_beam.stopThread();
	}

	public void login() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		c_beam.force_login(sharedPref.getString("pref_username", "bernd"));
	}
	public void logout() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		c_beam.force_logout(sharedPref.getString("pref_username", "bernd"));
	}
	//	public void c_out() {
	//		AlertDialog.Builder b = new AlertDialog.Builder(this);
	//		b.setTitle("c_out-durchsage eingeben");
	//		final EditText input = new EditText(this);
	//		b.setView(input);
	//		b.setPositiveButton("OK", new DialogInterface.OnClickListener()
	//		{
	//			@Override
	//			public void onClick(DialogInterface dialog, int whichButton)
	//			{
	//				String result = input.getText().toString();
	//				c_beam.tts(result);
	//				Log.i("c_out", result);
	//			}
	//		});
	//		b.setNegativeButton("CANCEL", null);
	//		b.create().show();
	//	}

	public void updateLists() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		//		if (true)
		//			return;
		UserListFragment online = (UserListFragment) mSectionsPagerAdapter.getItem(USER_FRAGMENT);
		EventListFragment events = (EventListFragment) mSectionsPagerAdapter.getItem(EVENTS_FRAGMENT);
		MissionListFragment missions = (MissionListFragment) mSectionsPagerAdapter.getItem(MISSION_FRAGMENT);
		C_portalListFragment c_portal = (C_portalListFragment) mSectionsPagerAdapter.getItem(C_PORTAL_FRAGMENT);
		ArtefactListFragment artefacts = (ArtefactListFragment) mSectionsPagerAdapter.getItem(ARTEFACTS_FRAGMENT);

		ArrayList<User> onlineList = c_beam.getOnlineList();
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
			if (articleList == null || 1 == 1 || articleList.size() != tmpList.size()) {
				articleList = tmpList;
				c_portal.clear();
				for(int i=0; i<articleList.size();i++)
					c_portal.addItem(articleList.get(i));
			}
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
		//		thread.start();
		c_beam.startThread();
		updateLists();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSherlock().getMenuInflater().inflate(R.menu.activity_main, menu);
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
		} else if (item.getItemId() == R.id.menu_map) {
			Intent myIntent = new Intent(this, MapActivity.class);
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
			//			if (pages[position] == null || pages[position].isAdded() == false) {
			//				if (pages[position] != null)
			//					Log.i("foo",pages[position].isAdded()+"");
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
				} else {
					//fragment = new ArrayListFragment();
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
			return 6;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case USER_FRAGMENT:
				return getString(R.string.title_users).toUpperCase();
			case C_PORTAL_FRAGMENT:
				return getString(R.string.title_c_portal).toUpperCase();
			case ARTEFACTS_FRAGMENT:
				return getString(R.string.title_artefacts).toUpperCase();
			case EVENTS_FRAGMENT:
				return getString(R.string.title_events).toUpperCase();
			case C_ONTROL_FRAGMENT:
				return getString(R.string.title_c_ontrol).toUpperCase();
			case MISSION_FRAGMENT:
				return getString(R.string.title_missions).toUpperCase();
			}
			return null;
		}
	}	

	public static final void setAppFont(ViewGroup mContainer, Typeface mFont)
	{
		if (mContainer == null || mFont == null) return;

		final int mCount = mContainer.getChildCount();

		// Loop through all of the children.
		for (int i = 0; i < mCount; ++i)
		{
			final View mChild = mContainer.getChildAt(i);
			if (mChild instanceof TextView)
			{
				// Set the font if it is a TextView.
				((TextView) mChild).setTypeface(mFont);
			}
			else if (mChild instanceof ViewGroup)
			{
				// Recursively attempt another ViewGroup.
				setAppFont((ViewGroup) mChild, mFont);
			}
		}
		Log.i("MainActivity", "font set");

	}

}
