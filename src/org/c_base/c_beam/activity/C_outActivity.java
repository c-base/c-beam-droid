package org.c_base.c_beam.activity;

import java.util.ArrayList;

import org.c_base.c_beam.R;
import org.c_base.c_beam.domain.C_beam;
import org.c_base.c_beam.fragment.C_outListFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class C_outActivity extends C_beamActivity {
	C_beam c_beam = C_beam.getInstance();
	EditText et;
	
	ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_c_out);

		C_outListFragment f = (C_outListFragment) this.getSupportFragmentManager().findFragmentById(R.id.fragment1);

		ArrayList<String> sounds = c_beam.getSounds();
		for(int i=0; i<sounds.size();i++)
			f.addItem(sounds.get(i));
		//v.addView(f.getView());

		et = (EditText) findViewById(R.id.c_outEditText);

		Button b = (Button) findViewById(R.id.button_announce);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = et.getText().toString();
				if (text.length() != 0) {
					c_beam.announce(text);
				}
			}
		});

		b = (Button) findViewById(R.id.button_r2d2);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = et.getText().toString();
				if (text.length() != 0) {
					c_beam.r2d2(text);
				}
			}
		});

		b = (Button) findViewById(R.id.button_tts);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = et.getText().toString();
				if (text.length() != 0) {
					c_beam.tts(text);
				}
			}
		});
		
		final ViewGroup mContainer = (ViewGroup) findViewById(
				android.R.id.content).getRootView();
		setAppFont(mContainer);
		setupActionBar();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSherlock().getMenuInflater().inflate(R.menu.activity_c_out, menu);
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
