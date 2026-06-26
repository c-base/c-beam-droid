package org.c_base.c_beam.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.c_base.c_beam.R;
import org.c_base.c_beam.activity.BamActivity;
import org.c_base.c_beam.domain.C_beam;

public class C_ontrolFragment extends Fragment {
	private final C_beam c_beam;

	public C_ontrolFragment(C_beam c_beam) {
		this.c_beam = c_beam;
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_c_ontrol, container, false);

		ToggleButton toggleButtonBluewall = v.findViewById(R.id.toggleButtonBluewall);
        if (toggleButtonBluewall != null) {
            toggleButtonBluewall.setOnClickListener(v1 -> {
                if (((ToggleButton) v1).isChecked()) {
                    c_beam.bluewall();
                } else {
                    c_beam.darkwall();
                }
            });
        }

		Button b = v.findViewById(R.id.button_softwareverbrennung);
        if (b != null) {
            b.setOnClickListener(v12 -> c_beam.hwstorage());
        }

		b = v.findViewById(R.id.button_self_destruct);
        if (b != null) {
            b.setOnClickListener(v14 -> {
                Toast.makeText(getActivity(), R.string.self_destruct_thx, Toast.LENGTH_LONG).show();
                new Thread(() -> {
                    try {
                        for (int progress = 0; progress < 100; progress++) {
                            Thread.sleep(5);
                        }
                    } catch (InterruptedException e) {
                        Log.e("C_ontrolFragment", "Self destruct interrupted", e);
                    }
                    Intent myIntent = new Intent(getActivity(), BamActivity.class);
                    startActivity(myIntent);
                }).start();
            });
        }

		Button bEmergency = v.findViewById(R.id.button_emergency_lights);
        if (bEmergency != null) {
            bEmergency.setOnClickListener(v15 -> c_beam.notbeleuchtung());
        }

        Button bPatternDefault = v.findViewById(R.id.button_pattern_default);
        if (bPatternDefault != null) {
            bPatternDefault.setOnClickListener(v16 -> c_beam.set_stripe_default());
        }

        setupLedPatternButtons(v);

		return v;
	}

    private void setupLedPatternButtons(View v) {
        int[] buttonIds = {R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6, R.id.button7};
        for (int i = 0; i < buttonIds.length; i++) {
            final int pattern = i;
            Button b = v.findViewById(buttonIds[i]);
            if (b != null) {
                b.setOnClickListener(v1 -> c_beam.set_stripe_pattern(pattern));
            }
        }
    }

}
