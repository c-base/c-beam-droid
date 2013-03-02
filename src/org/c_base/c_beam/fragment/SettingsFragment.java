package org.c_base.c_beam.fragment;

import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;

import org.c_base.c_beam.GCMManager;
import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;

public class SettingsFragment extends PreferenceFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		final Context context = getActivity().getApplicationContext();

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
