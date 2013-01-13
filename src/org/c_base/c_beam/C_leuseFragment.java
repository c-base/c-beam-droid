package org.c_base.c_beam;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ToggleButton;



public class C_leuseFragment extends Fragment {
	View thisView;
	ProgressBar input;
	C_leuseFragment cf = this;
	AlertDialog pd;
	AlertDialog bam;
	int progress;
	C_beam c_beam;

	public C_leuseFragment() {
		this.c_beam = null;
	}

	public C_leuseFragment(C_beam c_beam) {
		this.c_beam = c_beam;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}

		View v = inflater.inflate(R.layout.fragment_c_leuse, container, false);
		
		Button b = (Button) v.findViewById(R.id.button_pattern_off);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				c_beam.set_stripe_pattern(0);
			}
		});	

		b = (Button) v.findViewById(R.id.button_pattern_rainbow);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				c_beam.set_stripe_pattern(1);
			}
		});	

		b = (Button) v.findViewById(R.id.button_pattern_green);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				c_beam.set_stripe_pattern(2);
			}
		});	

		b = (Button) v.findViewById(R.id.button_pattern_bluegreen);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				c_beam.set_stripe_pattern(3);
			}
		});	

		b = (Button) v.findViewById(R.id.button_pattern_blue);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				c_beam.set_stripe_pattern(5);
			}
		});	

		b = (Button) v.findViewById(R.id.button_pattern_red);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				c_beam.set_stripe_pattern(4);
			}
		});	

		b = (Button) v.findViewById(R.id.button_pattern_white);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				c_beam.set_stripe_pattern(6);
			}
		});	
		
		b = (Button) v.findViewById(R.id.button_speed_plus_6);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				c_beam.set_stripe_speed(6);
			}
		});

		
		b = (Button) v.findViewById(R.id.button_speed_plus_3);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				c_beam.set_stripe_speed(3);
			}
		});
		
		b = (Button) v.findViewById(R.id.button_speed_plus_1);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				c_beam.set_stripe_speed(1);
			}
		});
		
		b = (Button) v.findViewById(R.id.button_speed_0);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				c_beam.set_stripe_speed(0);
			}
		});
		
		b = (Button) v.findViewById(R.id.button_speed_minus_1);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				c_beam.set_stripe_speed(-1);
			}
		});

		b = (Button) v.findViewById(R.id.button_speed_minus_3);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				c_beam.set_stripe_speed(-3);
			}
		});
		
		b = (Button) v.findViewById(R.id.button_speed_minus_6);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				c_beam.set_stripe_speed(-6);
			}
		});

		b = (Button) v.findViewById(R.id.button_emergency_lights);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				c_beam.notbeleuchtung();
			}
		});
		
		b = (Button) v.findViewById(R.id.button_small_blue);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				c_beam.set_stripe_pattern(7);
			}
		});
		
		b = (Button) v.findViewById(R.id.button_small_green);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				c_beam.set_stripe_pattern(8);
			}
		});

		b = (Button) v.findViewById(R.id.button_small_red);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				c_beam.set_stripe_pattern(9);
			}
		});
		
		b = (Button) v.findViewById(R.id.button_pattern_default);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				c_beam.set_stripe_default();
			}
		});

		thisView = v;
		return v;
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewStateRestored(savedInstanceState);
	}

}
