package org.c_base.c_beam;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.webkit.WebView;

public class ArtefactActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.activity_artefact);
		// Show the Up button in the action bar.
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			WebView w = (WebView) findViewById(R.id.artefactWebView);
			w.loadUrl("http://cbag3.c-base.org"+extras.getString("slug"));
		}
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_artefact, menu);
		return true;
	}

}
