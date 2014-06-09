package org.c_base.c_beam.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.c_base.c_beam.R;

public class RinginfoFragment extends Fragment {
    private String ring = "core";
    public RinginfoFragment(String ring) {
        this.ring = ring;

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_ringinfo, container, false);

        TextView ringInfoTextView = (TextView) v.findViewById(R.id.textView);
        TextView ringTitleTextView = (TextView) v.findViewById(R.id.textView2);
        ImageView ringInfoImageView = (ImageView) v.findViewById(R.id.imageView);
        ringTitleTextView.setText(ring);
        if (ring == "clamp") {
            ringInfoTextView.setText(getString(R.string.info_clamp));
            ringInfoImageView.setImageResource(R.drawable.ring_clamp);
        } else if(ring.equals("carbon")) {
            ringInfoTextView.setText(getString(R.string.info_carbon));
            ringInfoImageView.setImageResource(R.drawable.ring_carbon);
        } else if(ring.equals("cience")) {
            ringInfoTextView.setText(getString(R.string.info_cience));
            ringInfoImageView.setImageResource(R.drawable.ring_cience);
        } else if(ring.equals("creactiv")) {
            ringInfoTextView.setText(getString(R.string.info_creactiv));
            ringInfoImageView.setImageResource(R.drawable.ring_creactiv);
        } else if(ring.equals("culture")) {
            ringInfoTextView.setText(getString(R.string.info_culture));
            ringInfoImageView.setImageResource(R.drawable.ring_culture);
        } else if(ring.equals("com")) {
            ringInfoTextView.setText(getString(R.string.info_com));
            ringInfoImageView.setImageResource(R.drawable.ring_com);
        } else if(ring.equals("core")) {
            ringInfoTextView.setText(getString(R.string.info_core));
            ringInfoImageView.setImageResource(R.drawable.ring_core);
        }
        return v;
    }
}
