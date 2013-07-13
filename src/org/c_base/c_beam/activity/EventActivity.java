package org.c_base.c_beam.activity;

import android.os.Bundle;

import com.actionbarsherlock.view.Menu;

import org.c_base.c_beam.R;

public class EventActivity extends C_beamActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event);
		// Show the Up button in the action bar.
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSherlock().getMenuInflater().inflate(R.menu.activity_event, menu);
		return true;
	}

}
