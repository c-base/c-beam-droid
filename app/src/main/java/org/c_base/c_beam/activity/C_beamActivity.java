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
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;
import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.domain.C_beam;
import org.c_base.c_beam.fragment.AboutDialogFragment;
import org.c_base.c_beam.util.Helper;
import org.c_base.c_beam.voice.HoldToTalkController;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

public class C_beamActivity extends AppCompatActivity {
    ActionBar actionBar;
    C_beam c_beam = C_beam.getInstance();
    /** The hold-to-talk panel below the content; null until setContentView ran. */
    private HoldToTalkController holdToTalk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        c_beam.reloadConfiguration();
        actionBar = getSupportActionBar();
    }

    @Override
    public void setContentView(int layoutResID) {
        setContentView(getLayoutInflater().inflate(layoutResID, null));
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(withVoicePanel(view));
        setupActionBar();
        applyEdgeToEdge();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(withVoicePanel(view), params);
        setupActionBar();
        applyEdgeToEdge();
    }

    /**
     * Lays the hold-to-talk panel over the bottom edge of the screen's own content, so
     * the microphone is reachable from every screen in the same place. Where the screen
     * has a bottom button bar (see {@link #voicePanelAnchor}) the panel sits above it.
     * The panel only shows in the crew network (see onResume).
     */
    private View withVoicePanel(View content) {
        FrameLayout frame = new FrameLayout(this);
        frame.addView(content, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        final View panel = getLayoutInflater().inflate(R.layout.view_voice_panel, frame, false);
        final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);
        frame.addView(panel, lp);
        holdToTalk = new HoldToTalkController(this, panel);
        // the bar's height is only known after layout, and it may come and go
        frame.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            View anchor = voicePanelAnchor();
            int bottom = anchor != null && anchor.isShown() ? anchor.getHeight() : 0;
            if (lp.bottomMargin != bottom) {
                lp.bottomMargin = bottom;
                panel.setLayoutParams(lp);
            }
        });
        return frame;
    }

    /**
     * The bottom bar the voice panel should float above, or null to rest on the bottom
     * edge: the main button row of the ring screens, or the c-corder's c-ontrols.
     */
    protected View voicePanelAnchor() {
        View bar = findViewById(R.id.fragment);
        return bar != null ? bar : findViewById(R.id.hidecontainer);
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

    private boolean isActionBarSetup = false;

    protected void setupActionBar() {
        if (isActionBarSetup) return;

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setTitle(null);
        }

        actionBar = getSupportActionBar();
        if (actionBar == null) return;

        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setTitle(null);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        LayoutInflater inflator = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.view_actionbar, findViewById(android.R.id.content), false);
        TextView titleView = v.findViewById(R.id.title);
        titleView.setText(this.getTitle());
        titleView.setTypeface(Typeface.createFromAsset(getAssets(), "CEVA-CM.TTF"));
        titleView.setTextSize(30);
        titleView.setPadding(10, 20, 10, 20);
        actionBar.setCustomView(v);

        isActionBarSetup = true;
    }

    public final void setAppFont(ViewGroup mContainer) {
        if (mContainer == null) return;
        final int mCount = mContainer.getChildCount();
        for (int i = 0; i < mCount; ++i) {
            final View mChild = mContainer.getChildAt(i);
            if (mChild instanceof TextView) {
                Helper.setFont(this, (TextView) mChild);
            } else if (mChild instanceof ViewGroup) {
                setAppFont((ViewGroup) mChild);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_settings) {
            startActivity(SettingsActivity.class);
        } else if (id == R.id.menu_login) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            c_beam.force_login(sharedPref.getString(Settings.USERNAME, "bernd"));
        } else if (id == R.id.menu_logout) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            c_beam.force_logout(sharedPref.getString(Settings.USERNAME, "bernd"));
        } else if (id == R.id.menu_c_out) {
            startActivity(C_outActivity.class);
        } else if (id == R.id.menu_map) {
            startActivity(MapActivity.class);
        } else if (id == R.id.menu_c_mission) {
            startActivity(MissionActivity.class);
        } else if (id == R.id.menu_c_corder) {
            startActivity(CcorderActivity.class);
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
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean mIsOnline = c_beam.isInCrewNetwork();
        try {
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                menu.findItem(R.id.menu_c_corder).setVisible(false);
            }
            menu.findItem(R.id.menu_login).setVisible(mIsOnline);
            menu.findItem(R.id.menu_logout).setVisible(mIsOnline);
            menu.findItem(R.id.menu_map).setVisible(mIsOnline);
            menu.findItem(R.id.menu_c_out).setVisible(mIsOnline);
            menu.findItem(R.id.menu_c_mission).setVisible(mIsOnline);
        } catch (Exception ignored) {}
        return true;
    }

    protected void onResume() {
        super.onResume();
        c_beam.reloadConfiguration();
        if (holdToTalk != null) {
            holdToTalk.setAvailable(c_beam.isInCrewNetwork());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (holdToTalk != null) {
            holdToTalk.abort();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (holdToTalk != null) {
            holdToTalk.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (holdToTalk != null) {
            holdToTalk.onPermissionResult(requestCode, grantResults);
        }
    }

    protected void startActivity(Class<?> activityClass) {
        Intent myIntent = new Intent(this, activityClass);
        startActivity(myIntent);
    }

}
