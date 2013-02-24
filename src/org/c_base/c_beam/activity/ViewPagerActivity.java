package org.c_base.c_beam.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import org.c_base.c_beam.R;
import org.c_base.c_beam.adapter.MainPagerAdapter;

public class ViewPagerActivity extends SherlockFragmentActivity
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