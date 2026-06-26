package org.c_base.c_beam.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.domain.C_beam;
import org.c_base.c_beam.fragment.AboutDialogFragment;
import org.c_base.c_beam.util.Helper;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

public class C_beamActivity extends AppCompatActivity {
    ActionBar actionBar;
    C_beam c_beam = C_beam.getInstance();
    private final String TAG = "C_beamActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        c_beam.setActivity(this);
        actionBar = getSupportActionBar();

        //setupActionBar();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setupActionBar();
        applyEdgeToEdge();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        setupActionBar();
        applyEdgeToEdge();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        setupActionBar();
        applyEdgeToEdge();
    }

    private void applyEdgeToEdge() {
        View root = findViewById(android.R.id.content);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                View toolbar = findViewById(R.id.toolbar);
                if (toolbar != null) {
                    toolbar.setPadding(toolbar.getPaddingLeft(), systemBars.top, toolbar.getPaddingRight(), toolbar.getPaddingBottom());
                    v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
                } else {
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                }

                View drawer = findViewById(R.id.left_drawer);
                if (drawer != null) {
                    drawer.setPadding(drawer.getPaddingLeft(), systemBars.top, drawer.getPaddingRight(), drawer.getPaddingBottom());
                }

                return insets;
            });
        }
    }

    protected void setupActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        actionBar = getSupportActionBar();
        if (actionBar == null) return;
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        LayoutInflater inflator = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        View v = inflator.inflate(R.layout.view_actionbar, findViewById(android.R.id.content), false);
        TextView titleView = v.findViewById(R.id.title);
        titleView.setText(this.getTitle());
        titleView.setTypeface(Typeface.createFromAsset(getAssets(), "CEVA-CM.TTF"));
        titleView.setTextSize(30);
        titleView.setPadding(10, 20, 10, 20);
        actionBar.setCustomView(v);
    }

    public final void setAppFont(ViewGroup mContainer) {
        if (mContainer == null) return;
        final int mCount = mContainer.getChildCount();
        // Loop through all of the children.
        for (int i = 0; i < mCount; ++i) {
            final View mChild = mContainer.getChildAt(i);
            if (mChild instanceof TextView) {
                Helper.setFont(this, ((TextView) mChild));
            } else if (mChild instanceof ViewGroup) {
                // Recursively attempt another ViewGroup.
                setAppFont((ViewGroup) mChild);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.menu_login) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            c_beam.force_login(sharedPref.getString(Settings.USERNAME, "bernd"));
        } else if (id == R.id.menu_logout) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            c_beam.force_logout(sharedPref.getString(Settings.USERNAME, "bernd"));
        } else if (id == R.id.menu_c_out) {
            startActivity(new Intent(this, C_outActivity.class));
        } else if (id == R.id.menu_map) {
            startActivity(new Intent(this, MapActivity.class));
        } else if (id == R.id.menu_c_mission) {
            startActivity(new Intent(this, MissionActivity.class));
        } else if (id == R.id.menu_c_corder) {
            startActivity(new Intent(this, CcorderActivity.class));
        } else if (id == R.id.menu_ab_out) {
            new AboutDialogFragment().show(getSupportFragmentManager(), "about");
        } else if (id == android.R.id.home) {
            Intent myIntent = new Intent(this, MainActivity.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(myIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean mIsOnline = c_beam.isInCrewNetwork();
        // Hide some menu items when not connected to the crew network
        try {
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                menu.findItem(R.id.menu_c_corder).setVisible(false);
            }
            menu.findItem(R.id.menu_login).setVisible(mIsOnline);
            menu.findItem(R.id.menu_logout).setVisible(mIsOnline);
            menu.findItem(R.id.menu_map).setVisible(mIsOnline);
            menu.findItem(R.id.menu_c_out).setVisible(mIsOnline);
            menu.findItem(R.id.menu_c_mission).setVisible(mIsOnline);
        } catch (Exception e) {
            // some menu item is missing
        }
        return true;
    }

    protected void onResume() {
        super.onResume();
        c_beam.setActivity(this);
    }

    protected void startActivity(Class<?> activityClass) {
        Intent myIntent = new Intent(this, activityClass);
        startActivity(myIntent);
    }

}
