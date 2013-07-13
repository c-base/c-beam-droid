package org.c_base.c_beam.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.domain.Mission;
import org.c_base.c_beam.util.Helper;

public class MissionDetailActivity extends C_beamActivity implements OnClickListener {
    protected static final String TAG = "MissionActivity";
    TableLayout tl;
    TableRow tr;
    TextView labelTV, valueTV;
    ToggleButton toggleMissionButton;
    Mission m = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mission_detail);
        // Show the Up button in the action bar.
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            m = c_beam.getMission(extras.getInt("id"));
            tl = (TableLayout) findViewById(R.id.TableLayout1);
            //tl.setShrinkAllColumns(true);
            tl.setColumnShrinkable(1, true);

            // addHeaders();
            if (m != null) {
                addData(m);
            } else {
            }

        }

        toggleMissionButton = (ToggleButton) findViewById(R.id.toggleMissionButton);
        toggleMissionButton.setOnClickListener(this);
        boolean isUserAssigned = false;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String username = sharedPref.getString(Settings.USERNAME, "bernd");

        if (m != null) {
            for (String user : m.getAssigned_to()) {
                if (user.equals(username))
                    isUserAssigned = true;
            }
            toggleMissionButton.setChecked(m.getStatus().equals("assigned") && isUserAssigned);
        }

//		Helper.setButtonStyle((ViewGroup) findViewById(android.R.id.content));
    }

    /**
     * This function adds the data to the table *
     */
    public void addData(Mission m) {
        addRow("Mission:", m.getShort_description());
        addRow("Status:", m.getStatus());
        addRow("AP:", "" + m.getAp());
        addRow("Aufgabe:", m.getDescription());
    }

    public void addRow(String label, String value) {
        /** Create a TableRow dynamically **/
        tr = new TableRow(this);
        tr.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT));
        tr.setBackgroundResource(R.drawable.listitembg);

        /** Creating a TextView to add to the row **/
        labelTV = new TextView(this);
        labelTV.setText(label);
        labelTV.setPadding(15, 15, 15, 15);
        Helper.setFont(this, labelTV);
        labelTV.setTextColor(Color.WHITE);
//		labelTV.setBackgroundResource(R.drawable.listitembg);
        tr.addView(labelTV); // Adding textView to tablerow.

        /** Creating another textview **/
        valueTV = new TextView(this);
        valueTV.setText(value);
        valueTV.setPadding(15, 15, 15, 15);
        Helper.setFont(this, valueTV);
        valueTV.setTextColor(Color.WHITE);
//		valueTV.setBackgroundResource(R.drawable.listitembg);
        tr.addView(valueTV); // Adding textView to tablerow.

        // Add the TableRow to the TableLayout
        tl.addView(tr, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toggleMissionButton: {
                ToggleButton b = (ToggleButton) view;
                if (b.isChecked()) {
                    // Ask if the user really wants to start the mission
                    showStartMissionDialog();
                } else {
                    // Display dialog asking whether the mission is complete or if it should be canceled
                    showEndMissionDialog();
                }
                break;
            }
        }
    }

    private void showStartMissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_mission_start);
        builder.setPositiveButton(R.string.button_mission_start, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                startMission();
            }
        });
        builder.setNegativeButton(R.string.button_cancel, null);
        builder.create().show();
    }

    private void showEndMissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Mission beenden");
        builder.setItems(R.array.mission_result_array, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                switch (whichButton) {
                    case 0: // mission complete
                        completeMission();
                        break;
                    case 1: // mission cancelled
                        cancelMission();
                        break;
                    case 2: // oops
                        toggleMissionButton.setChecked(true);
                }
            }
        });
        builder.create().show();

    }

    private void startMission() {
        c_beam.assignMission(getIntent().getExtras().getInt("id"));
    }

    private void completeMission() {
        c_beam.completeMission(getIntent().getExtras().getInt("id"));
    }

    private void cancelMission() {
        c_beam.cancelMission(getIntent().getExtras().getInt("id"));
    }
}
