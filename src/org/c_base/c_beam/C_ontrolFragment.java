package org.c_base.c_beam;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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



public class C_ontrolFragment extends Fragment {
	View thisView;
	ProgressBar input;
	C_ontrolFragment cf = this;
	AlertDialog pd;
	AlertDialog bam;
	int progress;
	C_beam c_beam;

	public C_ontrolFragment() {
		this.c_beam = null;
	}

	public C_ontrolFragment(C_beam c_beam) {
		this.c_beam = c_beam;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}

		View v = inflater.inflate(R.layout.fragment_c_ontrol, container, false);
		Button b = (Button) v.findViewById(R.id.button_self_destruct);

		b.setOnClickListener(new OnClickListener() {
			ProgressDialog p;
			@Override
			public void onClick(View v) {
				AlertDialog.Builder adb = new AlertDialog.Builder(v.getContext());
				adb.setTitle("BAM!");
				adb.setPositiveButton("Danke", null);
				bam = adb.create();

				AlertDialog.Builder b = new AlertDialog.Builder(v.getContext());
				b.setTitle("Erde wird abgesprengt, bitte warten...");
				input = new ProgressBar(v.getContext(), null, android.R.attr.progressBarStyleHorizontal);
				b.setView(input);
				b.setNegativeButton("CANCEL", null);

				pd = b.create();
				pd.show();

				Runnable r = new Runnable(){
					@Override
					public void run() {
						Looper.prepare();
						for (progress = 0; progress < 100; progress++) {
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							input.setProgress(progress);
						}
						Log.i("thread", "done");
						pd.dismiss();
						bam.show();
					}
					public int getProgress(){
						return progress;
					}
				};
				Thread thread = new Thread(r);
				thread.start();
			}			
		});

		b = (Button) v.findViewById(R.id.button2);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder b = new AlertDialog.Builder(v.getContext());
				b.setTitle("We need to lift off...");
				b.setMessage("There's no time to explain!");
				b.setPositiveButton("OK", null);
				b.show();
			}
		});	

		ToggleButton t = (ToggleButton) v.findViewById(R.id.toggleButtonBluewall);
		t.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ToggleButton t = (ToggleButton) v;
				if (c_beam != null) {
					if (t.isChecked()) {
						c_beam.bluewall();
					} else {
						c_beam.bluewall();
					}
				}
			}
		});	

		thisView = v;
		return v;
	}
	public void bam() {
		AlertDialog.Builder b = new AlertDialog.Builder(thisView.getContext());
		b.setTitle("BAM!");
		b.setPositiveButton("Danke", null);
		b.create().show();
	}
	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewStateRestored(savedInstanceState);
	}

}
