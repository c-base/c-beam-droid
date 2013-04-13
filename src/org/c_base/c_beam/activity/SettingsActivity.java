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
import org.c_base.c_beam.domain.C_beam;

public class SettingsActivity extends SherlockPreferenceActivity {
	
	C_beam c_beam = C_beam.getInstance(); //new C_beam(this);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		c_beam.setActivity(this);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		final Context context = getApplicationContext();
		
		CheckBoxPreference stats_enabled = (CheckBoxPreference) findPreference(Settings.STATS_ENABLED);
		if (c_beam.isInCrewNetwork()) {
			stats_enabled.setChecked(c_beam.isStats_enabled());
		} else {
			stats_enabled.setEnabled(false);
			
		}
		
		stats_enabled.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				c_beam.setStats_enabled((Boolean) newValue);
//				if (((Boolean) newValue).booleanValue()) {
//
//				} else {
//
//				}
				return true;
			}
		});
		
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
