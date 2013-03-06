package org.c_base.c_beam.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import java.util.ArrayList;
import org.c_base.c_beam.R;
import org.c_base.c_beam.domain.C_beam;
import org.c_base.c_beam.fragment.C_outListFragment;

public class C_outActivity extends SherlockFragmentActivity {
	C_beam c_beam;
	EditText et;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_c_out);
		// Show the Up button in the action bar.
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);

		LayoutInflater inflator = (LayoutInflater)this.getSystemService(getApplicationContext().LAYOUT_INFLATER_SERVICE);
		View v = inflator.inflate(R.layout.view_actionbar, null);

		((TextView)v.findViewById(R.id.title)).setText(this.getTitle());
		((TextView)v.findViewById(R.id.title)).setTypeface(Typeface.createFromAsset(getAssets(), "CEVA-CM.TTF"));
		((TextView)v.findViewById(R.id.title)).setTextSize(30);
		((TextView)v.findViewById(R.id.title)).setPadding(10, 20, 10, 20);
		actionBar.setCustomView(v);

		C_outListFragment f = (C_outListFragment) this.getSupportFragmentManager().findFragmentById(R.id.fragment1);

		c_beam = new C_beam(this);
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
				if (!text.isEmpty()) {
					c_beam.announce(text);
				}
			}
		});

		b = (Button) findViewById(R.id.button_r2d2);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = et.getText().toString();
				if (!text.isEmpty()) {
					c_beam.r2d2(text);
				}
			}
		});

		b = (Button) findViewById(R.id.button_tts);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = et.getText().toString();
				if (!text.isEmpty()) {
					c_beam.tts(text);
				}
			}
		});
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
