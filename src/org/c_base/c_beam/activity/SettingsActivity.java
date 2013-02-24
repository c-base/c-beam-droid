package org.c_base.c_beam.activity;

import android.os.Bundle;
import com.actionbarsherlock.app.SherlockActivity;
import org.c_base.c_beam.fragment.SettingsFragment;

public class SettingsActivity extends SherlockActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
    
    
    
    
}