package org.c_base.c_beam.activity;

import org.c_base.c_beam.R;
import org.c_base.c_beam.domain.C_beam;
import org.c_base.c_beam.domain.User;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class UserActivity extends C_beamActivity {
	C_beam c_beam = C_beam.getInstance();;
	TableLayout tl;
	TableRow tr;
	TextView labelTV,valueTV;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		setContentView(R.layout.activity_user);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			User u = c_beam.getUser(extras.getInt("id"));
			tl = (TableLayout) findViewById(R.id.TableLayout1);
			if (u!=null) {
				this.setTitle(u.getUsername());
				addData(u);
				WebView w = (WebView) findViewById(R.id.userWebView);
				w.getSettings().setJavaScriptEnabled(true);
				w.loadUrl("http://"+u.getUsername()+".crew.c-base.org");

			} else {
				this.setTitle("c-beam user view");
			}
		}
		
		final ViewGroup mContainer = (ViewGroup) findViewById(
				android.R.id.content).getRootView();
		setAppFont(mContainer);
		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSherlock().getMenuInflater().inflate(R.menu.activity_user, menu);

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

	/** This function add the data to the table **/
	public void addData(User u){

		addRow("Username:", u.getUsername());
		addRow("Status:", u.getStatus());
		addRow("ETA:", u.getEta());
		addRow("Reminder:", u.getReminder());
		addRow("Logintime:", u.getLogintime());
	}
	public void addRow(String label, String value) {
		/** Create a TableRow dynamically **/
		tr = new TableRow(this);
		tr.setLayoutParams(new LayoutParams(
				//					LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));

		/** Creating a TextView to add to the row **/
		labelTV = new TextView(this);
		labelTV.setText(label);
		//		companyTV.setTextColor(Color.RED);
		//		companyTV.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		//		companyTV.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		labelTV.setPadding(15, 15, 15, 15);
		tr.addView(labelTV);  // Adding textView to tablerow.

		/** Creating another textview **/
		valueTV = new TextView(this);
		valueTV.setText(value);
		//		valueTV.setTextColor(Color.GREEN);
		//		valueTV.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		valueTV.setPadding(15, 15, 15, 15);
		//		valueTV.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		tr.addView(valueTV); // Adding textView to tablerow.

		// Add the TableRow to the TableLayout
		tl.addView(tr, new TableLayout.LayoutParams(
				LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));

	}
}
