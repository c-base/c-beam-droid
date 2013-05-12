package org.c_base.c_beam.activity;

import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.domain.C_beam;
import org.c_base.c_beam.fragment.AboutDialogFragment;
import org.c_base.c_beam.util.Helper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
 
public class C_beamActivity extends SherlockFragmentActivity {
	ActionBar actionBar;
	C_beam c_beam = C_beam.getInstance();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		c_beam.setActivity(this);
		actionBar = getSupportActionBar();

		setupActionBar();
	}

	protected void setupActionBar() {
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);

		LayoutInflater inflator = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		View v = inflator.inflate(R.layout.view_actionbar, null);
		TextView titleView = (TextView) v.findViewById(R.id.title);
		titleView.setText(this.getTitle());
		titleView.setTypeface(Typeface.createFromAsset(getAssets(), "CEVA-CM.TTF"));
		titleView.setTextSize(30);
		titleView.setPadding(10, 20, 10, 20);
		actionBar.setCustomView(v);
	}

	public final void setAppFont(ViewGroup mContainer)
	{
		if (mContainer == null) return;
		final int mCount = mContainer.getChildCount();
		// Loop through all of the children.
		for (int i = 0; i < mCount; ++i)
		{
			final View mChild = mContainer.getChildAt(i);
			if (mChild instanceof TextView)
			{
				Helper.setFont(this, ((TextView) mChild));
			}
			if (mChild instanceof Button) {
				mChild.setBackgroundResource(R.drawable.button);
//				((TextView) mChild).setGravity(TextView.TEXT);
			}
			else if (mChild instanceof ViewGroup)
			{
				// Recursively attempt another ViewGroup.
				setAppFont((ViewGroup) mChild);
			}
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		if (item.getItemId() == R.id.menu_settings) {
			Intent myIntent = new Intent(this, SettingsActivity.class);
			startActivityForResult(myIntent, 0);
		} else if (item.getItemId() == R.id.menu_login) {
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			c_beam.login(sharedPref.getString(Settings.USERNAME, "bernd"));
		} else if (item.getItemId() == R.id.menu_logout) {
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			c_beam.logout(sharedPref.getString(Settings.USERNAME, "bernd"));
		} else if (item.getItemId() == R.id.menu_c_out) {
			Intent myIntent = new Intent(this, C_outActivity.class);
			startActivityForResult(myIntent, 0);
		} else if (item.getItemId() == R.id.menu_map) {
			Intent myIntent = new Intent(this, MapActivity.class);
			startActivityForResult(myIntent, 0);
		} else if (item.getItemId() == R.id.menu_c_mission) {
			Intent myIntent = new Intent(this, MissionActivity.class);
			startActivityForResult(myIntent, 0);
		} else if (item.getItemId() == R.id.menu_c_corder) {
			Intent myIntent = new Intent(this, CcorderActivity.class);
			startActivityForResult(myIntent, 0);
		} else if (item.getItemId() == R.id.menu_ab_out) {
			new AboutDialogFragment().show(getSupportFragmentManager(), "about");
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSherlock().getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
