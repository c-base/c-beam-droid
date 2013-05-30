package org.c_base.c_beam.fragment;

import java.util.ArrayList;

import org.c_base.c_beam.R;
import org.c_base.c_beam.domain.ActivityLog;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

public class ActivitylogFragment extends Fragment {

    public ActivitylogFragment() {
        super();
	}

	public void updateLog(ArrayList<ActivityLog> activitylogList) {
		View v = getView();
		if (v == null) {
			return;
		}
		TextView tv = (TextView) getView().findViewById(R.id.textView_activitylog);
		String tmp = "user@c-beam> tail activitlog\n";
		if (activitylogList != null) {
			for(ActivityLog activitylog: activitylogList) {
				tmp += activitylog.getStr() + "\n";
			}
		}
		tmp += "user@c-beam>";

		if (!tv.getText().equals(tmp)) {
            String logtail = tmp;
			tv.setTypeface(Typeface.MONOSPACE);
			tv.setTextSize(10);
			tv.setBackgroundColor(Color.BLACK);
			tv.setTextColor(Color.rgb(58, 182, 228));
			tv.setText(logtail);
			ScrollView sv = (ScrollView) v.findViewById(R.id.scrollView1);
			sv.fullScroll(View.FOCUS_DOWN);
		}
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}

		View view = inflater.inflate(R.layout.fragment_activitylog, container, false);
		TextView tv = (TextView) view.findViewById(R.id.textView_activitylog);
		String logtail = "user@c-beam>\n";
		tv.setTypeface(Typeface.MONOSPACE);
		tv.setTextSize(10);
		tv.setBackgroundColor(Color.BLACK);
		tv.setTextColor(Color.rgb(58, 182, 228));
		tv.setText(logtail);
		ScrollView sv = (ScrollView) view.findViewById(R.id.scrollView1);
		sv.fullScroll(View.FOCUS_DOWN);
		view.setBackgroundColor(Color.BLACK);
		return view;
	}

}
