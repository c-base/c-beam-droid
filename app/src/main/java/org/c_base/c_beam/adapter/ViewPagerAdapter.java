package org.c_base.c_beam.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Parcelable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import org.c_base.c_beam.util.ScrollState;

public class ViewPagerAdapter extends PagerAdapter
{
	private static String[] titles = new String[] { "Page 1", "Page 2",
			"Page 3" };
	private final Context context;
	private int[] scrollPosition = new int[titles.length];

	public ViewPagerAdapter( Context context )
	{
		this.context = context;
		for ( int i = 0; i < titles.length; i++ )
		{
			scrollPosition[i] = 0;
		}
	}

	@Override
	public String getPageTitle( int position )
	{
		return titles[position];
	}

	@Override
	public int getCount()
	{
		return titles.length;
	}

	@Override
	public Object instantiateItem( View pager, final int position )
	{
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
	}

	@Override
	public void restoreState( Parcelable p, ClassLoader c )
	{
		if ( p instanceof ScrollState)
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
	}

}
