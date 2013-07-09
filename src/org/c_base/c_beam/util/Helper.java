package org.c_base.c_beam.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;
import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;

public class Helper {
	private static final String PREFS_NAME = "org.c_base.c_beam.AppWidgetProvider";
	private static final String PREF_PREFIX_KEY = "prefix_";

	public static void setFont(Activity activity, TextView view) {
		Context context = view.getContext();

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String defaultFont = context.getString(R.string.pref_font_default);
		String font = sharedPref.getString(Settings.FONT, defaultFont);

		int fontsize = Integer.parseInt(sharedPref.getString(Settings.FONT_SIZE, "20"));
		if (font.equals("X-Scale")) {
			view.setPadding(25, 10, 25, 25);
			Typeface myTypeface = Typeface.createFromAsset(context.getAssets(), "X-SCALE.TTF");
			view.setTypeface(myTypeface);
			view.setTextSize(fontsize);
		} else if (font.equals("Ceva")) {
			view.setPadding(25, 25, 25, 25);
			Typeface myTypeface = Typeface.createFromAsset(context.getAssets(), "CEVA-CM.TTF");
			view.setTypeface(myTypeface);
			view.setTextSize(fontsize);
		} else {
			view.setPadding(25, 25, 25, 25);
			view.setTextSize(fontsize);
		}

//		view.setGravity(Gravity.CENTER_VERTICAL);
	}

	// Write the prefix to the SharedPreferences object for this widget
	public static void saveTitlePref(Context context, int appWidgetId, String text) {
		SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
		prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
		prefs.commit();
	}

	// Read the prefix from the SharedPreferences object for this widget.
	// If there is no preference saved, get the default from a resource
	public static String loadTitlePref(Context context, int appWidgetId) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		String prefix = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
		if (prefix != null) {
			return prefix;
		} else {
			return context.getString(R.string.appwidget_prefix_default);
		}
	}

	public static void deleteTitlePref(Context context, int appWidgetId) {
	}

	public static void loadAllTitlePrefs(Context context,
			ArrayList<Integer> appWidgetIds, ArrayList<String> texts) {
	}
	
	public static final void setButtonStyle(ViewGroup mContainer)
	{
		if (mContainer == null) return;
		final int mCount = mContainer.getChildCount();
		// Loop through all of the children.
		for (int i = 0; i < mCount; ++i)
		{
			final View mChild = mContainer.getChildAt(i);
			if (mChild instanceof Button) {
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContainer.getContext());
				if (sharedPref.getBoolean(Settings.C_THEME, true)) {
					mChild.setBackgroundResource(R.drawable.button);
				}
				//				((TextView) mChild).setGravity(TextView.TEXT);
			}
			else if (mChild instanceof ViewGroup)
			{
				// Recursively attempt another ViewGroup.
				setButtonStyle((ViewGroup) mChild);
			}
		}
	}

	public static void setListItemStyle(View view) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(view.getContext());
		if (sharedPref.getBoolean(Settings.C_THEME, true)) {
			view.setBackgroundResource(R.drawable.listitembg);
//			view.setBackgroundResource(R.drawable.button);
		}
	}

    public static void setFont(TitlePageIndicator view) {
        Context context = view.getContext();

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String defaultFont = context.getString(R.string.pref_font_default);
		String font = sharedPref.getString(Settings.FONT, defaultFont);

		int fontsize = Integer.parseInt(sharedPref.getString(Settings.FONT_SIZE, "20"));
        fontsize += 10; // required for whatever reason :/
		if (font.equals("X-Scale")) {
			view.setPadding(25, 10, 25, 25);
			Typeface myTypeface = Typeface.createFromAsset(context.getAssets(), "X-SCALE.TTF");
			view.setTypeface(myTypeface);
			view.setTextSize(fontsize);
		} else if (font.equals("Ceva")) {
			view.setPadding(25, 25, 25, 25);
			Typeface myTypeface = Typeface.createFromAsset(context.getAssets(), "CEVA-CM.TTF");
			view.setTypeface(myTypeface);
			view.setTextSize(fontsize);
		} else {
			view.setPadding(25, 25, 25, 25);
			view.setTextSize(fontsize);
		}
    }
}
