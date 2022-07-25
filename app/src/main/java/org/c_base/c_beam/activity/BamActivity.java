package org.c_base.c_beam.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.core.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

import org.c_base.c_beam.R;


public class BamActivity extends C_beamActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MediaPlayer mp=MediaPlayer.create(this, R.raw.microwave_ding);
		mp.start();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bam);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_bam, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent myIntent;
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.menu_settings:
			 myIntent = new Intent(this, SettingsActivity.class);
			startActivityForResult(myIntent, 0);
			return true;
		case R.id.menu_c_out:
			myIntent = new Intent(this, C_outActivity.class);
			startActivityForResult(myIntent, 0);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
