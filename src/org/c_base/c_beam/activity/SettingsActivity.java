package org.c_base.c_beam.activity;

import android.app.Activity;
import android.os.Bundle;
import org.c_base.c_beam.fragment.SettingsFragment;

public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
    
    
    
    
}