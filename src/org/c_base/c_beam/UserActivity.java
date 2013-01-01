package org.c_base.c_beam;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

public class UserActivity extends Activity {
	C_beam c_beam;
	TableLayout tl;
	TableRow tr;
	TextView labelTV,valueTV;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		c_beam = new C_beam(this);

		setContentView(R.layout.activity_user);
		// Show the Up button in the action bar.
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			User u = c_beam.getUser(extras.getInt("id"));
			//addHeaders();
			tl = (TableLayout) findViewById(R.id.TableLayout1);
			if (u!=null) {
				addData(u);
				WebView w = (WebView) findViewById(R.id.userWebView);
				w.loadUrl("http://"+u.getUsername()+".crew.c-base.org");
			} else {
				Log.e("UserActivity", "user not forund: "+extras.getInt("id"));
			}
		}
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_user, menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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