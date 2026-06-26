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
		int id = item.getItemId();
		if (id == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		} else if (id == R.id.menu_settings) {
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		} else if (id == R.id.menu_c_out) {
			startActivity(new Intent(this, C_outActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
