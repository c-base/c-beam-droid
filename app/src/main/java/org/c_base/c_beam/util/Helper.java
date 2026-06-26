package org.c_base.c_beam.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.viewpagerindicator.TitlePageIndicator;

import org.c_base.c_beam.Settings;

public class Helper {
	public static final String PREFS_NAME = "org.c_base.c_beam.AppWidgetProvider";
	public static final String PREF_PREFIX_KEY = "prefix_";

	public static void setFont(Context context, TextView view) {
		if (context == null || view == null) return;
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String font = sharedPref.getString(Settings.FONT, "Ceva");
		int fontsize = Integer.parseInt(sharedPref.getString(Settings.FONT_SIZE, "20"));

        view.setPadding(25, 25, 25, 25);
        view.setTextSize(fontsize);

        switch (font) {
            case "X-Scale": {
                Typeface myTypeface = Typeface.createFromAsset(context.getAssets(), "X-SCALE.TTF");
                view.setTypeface(myTypeface);
                break;
            }
            case "Ceva": {
                Typeface myTypeface = Typeface.createFromAsset(context.getAssets(), "CEVA-CM.TTF");
                view.setTypeface(myTypeface);
                break;
            }
        }
	}

	public static void saveTitlePref(Context context, int appWidgetId, String text) {
		SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
		prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
		prefs.apply();
	}

	public static String loadTitlePref(Context context, int appWidgetId) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		String prefix = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
		if (prefix != null) {
			return prefix;
		} else {
			return "c-beam";
		}
	}

	public static void deleteTitlePref(Context context, int appWidgetId) {
		SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
		prefs.remove(PREF_PREFIX_KEY + appWidgetId);
		prefs.apply();
	}

	public static void setButtonStyle(ViewGroup mContainer) {
		if (mContainer == null) return;
		final int mCount = mContainer.getChildCount();
		for (int i = 0; i < mCount; ++i) {
			final View mChild = mContainer.getChildAt(i);
			if (mChild instanceof ViewGroup) {
				setButtonStyle((ViewGroup) mChild);
			}
		}
	}

	public static void setListItemStyle(View view) {
		// view.setBackgroundResource(R.drawable.listitembg);
	}

	public static void setFont(TitlePageIndicator view) {
		if (view == null) return;
		Context context = view.getContext();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String font = sharedPref.getString(Settings.FONT, "Ceva");
		int fontsize = Integer.parseInt(sharedPref.getString(Settings.FONT_SIZE, "20"));
		fontsize += 10; // required for whatever reason :/

        view.setTextSize(fontsize);
        if (font.equals("X-Scale")) {
            Typeface myTypeface = Typeface.createFromAsset(context.getAssets(), "X-SCALE.TTF");
            view.setTypeface(myTypeface);
        } else if (font.equals("Ceva")) {
            Typeface myTypeface = Typeface.createFromAsset(context.getAssets(), "CEVA-CM.TTF");
            view.setTypeface(myTypeface);
        }
	}
}
