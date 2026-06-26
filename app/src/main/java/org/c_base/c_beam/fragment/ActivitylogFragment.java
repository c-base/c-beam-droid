package org.c_base.c_beam.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.c_base.c_beam.R;
import org.c_base.c_beam.domain.ActivityLog;
import org.c_base.c_beam.util.Helper;

import java.util.ArrayList;

public class ActivitylogFragment extends Fragment {

	private TextView tv;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_activitylog, container, false);
		tv = v.findViewById(R.id.textView_activitylog);
		Helper.setFont(getActivity(), tv);
		return v;
	}

	public void updateLog(ArrayList<ActivityLog> activityLogList) {
		if (activityLogList == null) return;
        StringBuilder tmp = new StringBuilder("user@c-beam> tail activitylog\n");

		for (ActivityLog activitylog : activityLogList) {
			tmp.append(activitylog.getStr()).append("\n");
		}

		if (tv != null) {
            String newText = tmp.toString();
			if (!tv.getText().toString().equals(newText)) {
				tv.setText(newText);
			}
		}
	}
}
