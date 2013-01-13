package org.c_base.c_beam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MainPagerAdapter extends FragmentPagerAdapter
{
	private static final int USER_FRAGMENT = 0;
	private static final int C_PORTAL_FRAGMENT = 1;
	private static final int ARTEFACTS_FRAGMENT = 2;
	private static final int EVENTS_FRAGMENT = 3;
	private static final int C_ONTROL_FRAGMENT = 4;
	private static final int MISSION_FRAGMENT = 5;
	private static final String TAG = "MainPagerAdapter";

	private ArrayList<User> userList;
	private ArrayList<Article> articleList;
	private ArrayList<Artefact> artefactList;
	private ArrayList<Event> eventList;
	private ArrayList<Mission> missionList;

	private static String[] titles = new String[] { "An Bord", "C-Portal",
		"Artefacts", "Events", "C-ontrol", "Missionen" };

	private final Context context;
	private int[] scrollPosition = new int[titles.length];

	Fragment[] pages;

	UserListFragment userlist;
	EventListFragment events;
	MissionListFragment missions;
	C_portalListFragment c_portal;
	ArtefactListFragment artefacts;


	public MainPagerAdapter( FragmentManager fm, Context context )
	{
		super(fm);
		this.context = context;
		for ( int i = 0; i < titles.length; i++ )
		{
			scrollPosition[i] = 0;
		}
		pages = new Fragment[getCount()];

		userlist = (UserListFragment) getItem(USER_FRAGMENT);
		events = (EventListFragment) getItem(EVENTS_FRAGMENT);
		missions = (MissionListFragment) getItem(MISSION_FRAGMENT);
		c_portal = (C_portalListFragment) getItem(C_PORTAL_FRAGMENT);
		artefacts = (ArtefactListFragment) getItem(ARTEFACTS_FRAGMENT);
	}

	@Override
	public String getPageTitle( int position )
	{
		return titles[position].toUpperCase();
	}

	@Override
	public int getCount()
	{
		return titles.length;
	}

	@Override
	public Object instantiateItem( View pager, final int position )
	{
		Log.i(TAG, "instantiateItem");
		ListView v = new ListView( context );
		String[] from = new String[] { "str" };
		int[] to = new int[] { android.R.id.text1 };
		List<Map<String, String>> items = new ArrayList<Map<String, String>>();
		for ( int i = 0; i < 20; i++ )
		{
			Map<String, String> map = new HashMap<String, String>();
			map.put( "str", String.format( "Item %d", i + 1 ) );
			items.add( map );
		}
		SimpleAdapter adapter = new SimpleAdapter( context, items,
				android.R.layout.simple_list_item_1, from, to );
		v.setAdapter( adapter );
		((ViewPager)pager ).addView( v, 0 );
		v.setSelection( scrollPosition[ position ] );
		v.setOnScrollListener( new OnScrollListener()
		{

			@Override
			public void onScrollStateChanged( AbsListView view, int scrollState )
			{
			}

			@Override
			public void onScroll( AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount )
			{
				scrollPosition[ position ] = firstVisibleItem;
			}
		} );
		return v;
	}

	@Override
	public void destroyItem( View pager, int position, Object view )
	{
		( (ViewPager) pager ).removeView( (ListView) view );
	}

	@Override
	public boolean isViewFromObject( View view, Object object )
	{
		return view.equals( object );
	}

	@Override
	public void finishUpdate( View view )
	{
		Log.i(TAG, "finishUpdate");
	}

	@Override
	public void restoreState( Parcelable p, ClassLoader c )
	{
		Log.i(TAG, "restoreState");
		if ( p instanceof ScrollState )
		{
			scrollPosition = ( (ScrollState) p ).getScrollPos();
		}
	}

	@Override
	public Parcelable saveState()
	{
		return new ScrollState( scrollPosition );
	}



	@Override
	public void startUpdate( View view )
	{
		Log.i(TAG, "startUpdate");
		userlist.addItem(new User("bernd"));
	}

	@Override
	public Fragment getItem(int position) {
		Log.i(TAG, "getItem("+position+")");
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
				//} else if(position == C_ONTROL_FRAGMENT) {
				//fragment = new C_ontrolFragment(c_beam);	
			} else if(position == MISSION_FRAGMENT) {
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

	public UserListFragment getUserlist() {
		return userlist;
	}

	public void setUserlist(UserListFragment userlist) {
		this.userlist = userlist;
	}

	public EventListFragment getEvents() {
		return events;
	}

	public void setEvents(EventListFragment events) {
		this.events = events;
	}

	public MissionListFragment getMissions() {
		return missions;
	}

	public void setMissions(MissionListFragment missions) {
		this.missions = missions;
	}

	public C_portalListFragment getC_portal() {
		return c_portal;
	}

	public void setC_portal(C_portalListFragment c_portal) {
		this.c_portal = c_portal;
	}

	public ArtefactListFragment getArtefacts() {
		return artefacts;
	}

	public void setArtefacts(ArtefactListFragment artefacts) {
		this.artefacts = artefacts;
	}



}
