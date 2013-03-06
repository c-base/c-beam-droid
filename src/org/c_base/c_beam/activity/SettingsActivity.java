package org.c_base.c_beam.activity;

import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

import org.c_base.c_beam.GCMManager;
import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;

public class SettingsActivity extends SherlockPreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		final Context context = getApplicationContext();

		CheckBoxPreference push = (CheckBoxPreference) findPreference(Settings.PUSH);
		push.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (((Boolean) newValue).booleanValue()) {
					GCMManager.register(context);
				} else {
					GCMManager.unregister(context);
				}

				return true;
			}
		});
	}
}
