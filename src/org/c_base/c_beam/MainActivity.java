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
	}
	
	public void onStart() {
		super.onStart();
		startProgress();
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
	            	JSONRPCClient client = JSONRPCClient.create("http://10.0.1.27:4254/rpc/");
	            	client.setConnectionTimeout(5000);
	            	client.setSoTimeout(5000);
	            	
	            	ArrayListFragment online = (ArrayListFragment) mSectionsPagerAdapter.getItem(0);
	            	ArrayListFragment eta = (ArrayListFragment) mSectionsPagerAdapter.getItem(1);
	            	ArrayListFragment events = (ArrayListFragment) mSectionsPagerAdapter.getItem(2);
	            	ArrayListFragment missions = (ArrayListFragment) mSectionsPagerAdapter.getItem(3);

        			Log.i("handler",""+online.isAdded());
        			Log.i("handler",""+eta.isAdded());
        			Log.i("handler",""+events.isAdded());
        			Log.i("handler",""+missions.isAdded());
	            	try {
	            		JSONObject who = client.callJSONObject("who");
	        			if (online.isAdded()) {
		        			
		        			JSONArray available = who.getJSONArray("available");
		        			online.clear();
		        			for(int i=0; i<available.length();i++)
		        				online.addItem(available.get(i).toString());
	        			}
	        			
	        			if (eta.isAdded()) {
		        			JSONObject etaresult = who.getJSONObject("eta");
		        			eta.clear();
		        			Iterator etakeys = etaresult.keys();
		        			while(etakeys.hasNext()){
		        				String cur = etakeys.next().toString();
		        				eta.addItem(cur.toString() + " (" + etaresult.get(cur) + ")");
		        					
		        			}
	        			}
	        		
	        			if (events.isAdded()){
		        			JSONArray eventsresult = client.callJSONArray("events");
		        			events.clear();
	        				for(int i=0; i<eventsresult.length();i++)
	        					events.addItem(eventsresult.get(i).toString());
	        			}
	        			
	        			if (missions.isAdded()){
		        			JSONArray missionsresult = client.callJSONArray("missions");
		        			missions.clear();
	        				for(int i=0; i<missionsresult.length();i++)
	        					missions.addItem(missionsresult.get(i).toString());
	        			}
	            	} catch (JSONRPCException e) {
	        			// TODO Auto-generated catch block
	        			e.printStackTrace();
	        		} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            	
	            }
	          });
	        }
	      }
	    };
	    new Thread(runnable).start();
	  }
	protected void onResume () {
		super.onResume();
		
		Log.i("FragmentList", "foo");
		
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
				fragment = new ArrayListFragment();
				fragment.setArguments(new Bundle());
				pages[position] = fragment;
				Log.i("SectionsPagerAdapter","not found");
			} else {
				Log.i("SectionsPagerAdapter","found");
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
	class ArrayListFragment extends ListFragment {
		List<String> items = new ArrayList<String>();
		ListAdapter adapter;
		
		public void clear() {
			items.clear();
		}
		public void addItem(String item) {
			items.add(item);
			 ((ArrayAdapter)getListView().getAdapter()).notifyDataSetChanged(); 
		}
		
	    @Override
	    public void onActivityCreated(Bundle savedInstanceState) {
	        super.onActivityCreated(savedInstanceState);
	        adapter = new ArrayAdapter<String>(getActivity(),
	                android.R.layout.simple_list_item_1, items);
	        setListAdapter(adapter);
	    }

	    @Override
	    public void onListItemClick(ListView l, View v, int position, long id) {
	        Log.i("FragmentList", "Item clicked: " + id);
	    }
	}		
}

