package org.c_base.c_beam.fragment;

import org.c_base.c_beam.R;
import org.c_base.c_beam.domain.C_beam;
import org.c_base.c_beam.util.Helper;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ActivitylogFragment extends Fragment {
	C_beam c_beam;
	Fragment c_leuseFragment;

	public ActivitylogFragment() {
		this.c_beam = null;
	}

//	public ActivitylogFragment(C_beam c_beam) {
//		this.c_beam = c_beam;
//	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}

		View v = inflater.inflate(R.layout.fragment_activitylog, container, false);

		return v;
	}

}
