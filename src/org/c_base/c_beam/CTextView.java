package org.c_base.c_beam;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TextView;

public class CTextView extends TextView {

	public CTextView(Context context) {
		super(context);
		init();
	}

	public CTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init();
	}

	public CTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	public void init() {
//		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
//		String font = sharedPref.getString("pref_font", "Android Default");
//		if (font.equals("Default Android")) {
//			
//		} else if (font.equals("X-Scale")) {
//			Typeface myTypeface = Typeface.createFromAsset(getContext().getAssets(), "X-SCALE.TTF");
//			setTypeface(myTypeface);
//		} else if (font.equals("Ceva")) {	
//			Typeface myTypeface = Typeface.createFromAsset(getContext().getAssets(), "CEVA-CM.TTF");
//			setTypeface(myTypeface);
//		}
	}

}
