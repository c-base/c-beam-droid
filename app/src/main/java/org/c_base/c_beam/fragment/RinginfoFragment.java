package org.c_base.c_beam.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.c_base.c_beam.R;

public class RinginfoFragment extends Fragment {
	private String ring = "clamp";

	public void setRing(String ring) {
		this.ring = ring;
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_ringinfo, container, false);
		TextView tv = v.findViewById(R.id.textView);
		TextView tv2 = v.findViewById(R.id.textView2);
        ImageView iv = v.findViewById(R.id.imageView);

        tv2.setText(ring);
        switch (ring) {
            case "clamp":
                tv.setText(R.string.info_clamp);
                iv.setImageResource(R.drawable.ring_clamp);
                break;
            case "carbon":
                tv.setText(R.string.info_carbon);
                iv.setImageResource(R.drawable.ring_carbon);
                break;
            case "cience":
                tv.setText(R.string.info_cience);
                iv.setImageResource(R.drawable.ring_cience);
                break;
            case "creactiv":
                tv.setText(R.string.info_creactiv);
                iv.setImageResource(R.drawable.ring_creactiv);
                break;
            case "culture":
                tv.setText(R.string.info_culture);
                iv.setImageResource(R.drawable.ring_culture);
                break;
            case "com":
                tv.setText(R.string.info_com);
                iv.setImageResource(R.drawable.ring_com);
                break;
            case "core":
                tv.setText(R.string.info_core);
                iv.setImageResource(R.drawable.ring_core);
                break;
        }

		return v;
	}

}
