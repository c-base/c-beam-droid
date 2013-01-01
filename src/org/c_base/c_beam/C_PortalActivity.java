package org.c_base.c_beam;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

public class C_PortalActivity extends FragmentActivity implements ActionBar.TabListener {

	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	private int currentArticle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		setContentView(R.layout.activity_c_portal);
		
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.c_portal_pager);
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

		Bundle extras = getIntent().getExtras();
		
		if (extras != null) {
			C_portalWebViewFragment f = (C_portalWebViewFragment) mSectionsPagerAdapter.getItem(0);
			currentArticle = extras.getInt("id");
			if (f != null) {
				f.setUrl("https://c-portal.c-base.org/articles/" + extras.getInt("id") + "/");
			}
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_c_portal, menu);
		return true;
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {

	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		if (item.getItemId() == R.id.menu_settings) {
			Intent myIntent = new Intent(this, SettingsActivity.class);
			startActivityForResult(myIntent, 0);
		} else if (item.getItemId() == R.id.menu_c_out) {
			Intent myIntent = new Intent(this, C_outActivity.class);
			startActivityForResult(myIntent, 0);
		}
		return super.onOptionsItemSelected(item);
	}

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
					fragment = new C_portalWebViewFragment();
				} else if(position == 23) {
					fragment = null;
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
			return 5;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return getString(R.string.title_c_portal_section0).toUpperCase();
			case 1:
				return getString(R.string.title_c_portal_section1).toUpperCase();
			case 2:
				return getString(R.string.title_c_portal_section2).toUpperCase();
			case 3:
				return getString(R.string.title_c_portal_section3).toUpperCase();
			case 4:
				return getString(R.string.title_c_portal_section4).toUpperCase();
			}
			return null;
		}
	}	

}
