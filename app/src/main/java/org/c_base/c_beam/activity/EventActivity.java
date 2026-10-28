package org.c_base.c_beam.activity;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

import org.c_base.c_beam.R;
import org.c_base.c_beam.domain.Event;

/**
 * Details of one entry of today's station calendar. The list passes the entry's
 * fields along, since the calendar feed gives events no id to look them up by.
 */
public class EventActivity extends C_beamActivity {
	public static final String EXTRA_TITLE = "title";
	public static final String EXTRA_START = "start";
	public static final String EXTRA_END = "end";
	public static final String EXTRA_DESCRIPTION = "description";
	public static final String EXTRA_ALL_DAY = "all_day";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event);
		// Show the Up button in the action bar.
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return;
		}
		String title = extras.getString(EXTRA_TITLE, "");
		setTitle(title);
		((TextView) findViewById(R.id.event_title)).setText(title);

		TextView time = findViewById(R.id.event_time);
		String start = extras.getString(EXTRA_START, "");
		String end = extras.getString(EXTRA_END, "");
		if (extras.getBoolean(EXTRA_ALL_DAY, false)) {
			time.setText(R.string.event_all_day);
		} else if (!start.isEmpty() || !end.isEmpty()) {
			time.setText(getString(R.string.event_time, Event.clock(start), Event.clock(end)));
		} else {
			time.setVisibility(ViewGroup.GONE);
		}

		TextView description = findViewById(R.id.event_description);
		String text = extras.getString(EXTRA_DESCRIPTION, "").trim();
		if (text.isEmpty()) {
			description.setText(R.string.event_no_description);
		} else {
			// the calendar feed may carry html; plain text passes through unchanged
			description.setText(HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT));
			description.setMovementMethod(LinkMovementMethod.getInstance());
		}

		setAppFont((ViewGroup) findViewById(android.R.id.content).getRootView());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_event, menu);
		return true;
	}

}
