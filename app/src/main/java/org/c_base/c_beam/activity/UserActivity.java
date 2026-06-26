package org.c_base.c_beam.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.c_base.c_beam.R;
import org.c_base.c_beam.domain.User;

public class UserActivity extends C_beamActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user);
		setupActionBar();
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			User u = c_beam.getUser(extras.getInt("id"));
			if (u != null) {
				updateUser(u);
			}
		}
	}

	private void updateUser(User u) {
		TextView tv = findViewById(R.id.textView1);
		if (tv != null) {
			tv.setText(u.getUsername());
		}

		ToggleButton toggleButton1 = findViewById(R.id.toggleButton1);
		toggleButton1.setTextOff(u.getUsername());
		toggleButton1.setTextOn(u.getUsername());

		if (u.getStatus().equals("online")) {
			toggleButton1.setChecked(true);
			toggleButton1.setBackgroundColor(Color.rgb(0, 255, 0));
		} else {
			toggleButton1.setChecked(false);
			toggleButton1.setBackgroundColor(Color.rgb(255, 0, 0));
		}

		WebView w = findViewById(R.id.userWebView);
		w.loadUrl("https://portal.c-base.org/users/" + u.getUsername());

		TableLayout tl = findViewById(R.id.TableLayout1);

		tl.addView(createRow("Status", u.getStatus()));
		tl.addView(createRow("Abheben", u.getAutologout() + " min"));
		tl.addView(createRow("AP", "" + u.getAp()));
	}

	private TableRow createRow(String label, String value) {
		TableRow tr = new TableRow(this);
		tr.setLayoutParams(new TableRow.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));

		TextView labelTV = new TextView(this);
		labelTV.setText(label);
		labelTV.setPadding(15, 5, 15, 5);
		tr.addView(labelTV);

		TextView valueTV = new TextView(this);
		valueTV.setText(value);
		valueTV.setPadding(15, 5, 15, 5);
		tr.addView(valueTV);

		return tr;
	}

}
