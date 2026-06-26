package org.c_base.c_beam.activity;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ToggleButton;

import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.domain.Mission;

public class MissionDetailActivity extends C_beamActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission_detail);
        setupActionBar();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Mission m = c_beam.getMission(extras.getInt("id"));
            updateMission(m);
        }

        findViewById(R.id.toggleMissionButton).setOnClickListener(this::toggleMission);
    }

    private void updateMission(Mission m) {
        if (m != null) {
            ToggleButton toggleMissionButton = findViewById(R.id.toggleMissionButton);
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String username = sharedPref.getString(Settings.USERNAME, "bernd");
            boolean isUserAssigned = false;
            for (String user : m.getAssigned_to()) {
                if (user.equals(username)) {
                    isUserAssigned = true;
                    break;
                }
            }

            toggleMissionButton.setChecked(m.getStatus().equals("assigned") && isUserAssigned);
            toggleMissionButton.setEnabled(m.getStatus().equals("open") || (m.getStatus().equals("assigned") && isUserAssigned));
        }
    }

    public void toggleMission(View view) {
        if (view.getId() == R.id.toggleMissionButton) {
            ToggleButton toggleMissionButton = (ToggleButton) view;
            if (toggleMissionButton.isChecked()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.confirm_mission_start);
                builder.setPositiveButton(R.string.button_mission_start, (dialog, whichButton) -> {
                    if (getIntent().getExtras() != null) {
                        c_beam.assignMission(getIntent().getExtras().getInt("id"));
                    }
                });
                builder.setNegativeButton(R.string.button_cancel, (dialog, whichButton) -> toggleMissionButton.setChecked(false));
                builder.create().show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.button_mission_end);
                builder.setItems(R.array.mission_result_array, (dialog, whichButton) -> {
                    if (getIntent().getExtras() != null) {
                        switch (whichButton) {
                            case 0: // mission complete
                                c_beam.completeMission(getIntent().getExtras().getInt("id"));
                                break;
                            case 1: // mission canceled
                                c_beam.cancelMission(getIntent().getExtras().getInt("id"));
                                break;
                            case 2: // oops
                                toggleMissionButton.setChecked(true);
                                break;
                        }
                    }
                });
                builder.create().show();
            }
        }
    }

}
