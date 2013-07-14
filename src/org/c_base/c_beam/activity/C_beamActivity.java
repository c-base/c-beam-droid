package org.c_base.c_beam.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.domain.C_beam;
import org.c_base.c_beam.fragment.AboutDialogFragment;
import org.c_base.c_beam.util.Helper;

public class C_beamActivity extends SherlockFragmentActivity {
    ActionBar actionBar;
    C_beam c_beam = C_beam.getInstance();
    private String TAG = "C_beamActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        c_beam.setActivity(this);
        actionBar = getSupportActionBar();

        //setupActionBar();
    }

    protected void setupActionBar() {
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        LayoutInflater inflator = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        View v = inflator.inflate(R.layout.view_actionbar, null);
        TextView titleView = (TextView) v.findViewById(R.id.title);
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
            //			if (mChild instanceof Button) {
            //				mChild.setBackgroundResource(R.drawable.button);
            //				return;
            //			}
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
        // Handle item selection
        if (item.getItemId() == R.id.menu_settings) {
            Intent myIntent = new Intent(this, SettingsActivity.class);
            startActivityForResult(myIntent, 0);
        } else if (item.getItemId() == R.id.menu_login) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            c_beam.force_login(sharedPref.getString(Settings.USERNAME, "bernd"));
        } else if (item.getItemId() == R.id.menu_logout) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            c_beam.force_logout(sharedPref.getString(Settings.USERNAME, "bernd"));
        } else if (item.getItemId() == R.id.menu_c_out) {
            Intent myIntent = new Intent(this, C_outActivity.class);
            startActivityForResult(myIntent, 0);
        } else if (item.getItemId() == R.id.menu_map) {
            Intent myIntent = new Intent(this, MapActivity.class);
            startActivityForResult(myIntent, 0);
        } else if (item.getItemId() == R.id.menu_c_mission) {
            Intent myIntent = new Intent(this, MissionActivity.class);
            startActivityForResult(myIntent, 0);
        } else if (item.getItemId() == R.id.menu_c_corder) {
            Intent myIntent = new Intent(this, CcorderActivity.class);
            startActivityForResult(myIntent, 0);
        } else if (item.getItemId() == R.id.menu_ab_out) {
            new AboutDialogFragment().show(getSupportFragmentManager(), "about");
        } else if (item.getItemId() == android.R.id.home) {
            Intent myIntent = new Intent(this, MainActivity.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(myIntent, 0);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSherlock().getMenuInflater().inflate(R.menu.activity_main, menu);
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

    protected void startActivity(Class activityClass) {
        Intent myIntent = new Intent(this, activityClass);
        startActivityForResult(myIntent, 0);
    }

    protected class C_beamTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if (params.length == 2) {
                return c_beam.call(params[0], params[1]);
            } else if (params.length == 3) {
                return c_beam.call(params[0], params[1], params[2]);
            }
            return "failure";
        }

        @Override
        protected void onPostExecute(String result) {
//            if (result.contentEquals("eta_set")) {
//                result = getText(R.string.eta_set).toString();
//            } else if (result.contentEquals("eta_removed")) {
//                result = getText(R.string.eta_removed).toString();
//            } else {
//                result = getText(R.string.eta_failure).toString();
//            }
            //Toast.makeText(activity.getApplicationContext(), result, Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

}
