package org.c_base.c_beam.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import org.c_base.c_beam.adapter.MainPagerAdapter;
import org.c_base.c_beam.R;

public class ViewPagerActivity extends FragmentActivity
{
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
	    super.onCreate( savedInstanceState );
	    setContentView( R.layout.activity_main );
	 
	    MainPagerAdapter adapter = new MainPagerAdapter( getSupportFragmentManager(), this );
	    ViewPager pager =
	        (ViewPager)findViewById( R.id.pager );
	    pager.setAdapter( adapter );
	    
	    
	}
}