package org.c_base.c_beam.activity;

import org.c_base.c_beam.R;
import org.c_base.c_beam.domain.C_beam;
import org.c_base.c_beam.util.Helper;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
 
public class C_beamActivity extends SherlockFragmentActivity {
	ActionBar actionBar;
	C_beam c_beam = C_beam.getInstance();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		c_beam.setActivity(this);
		actionBar = getSupportActionBar();

		setupActionBar();
	}

	protected void setupActionBar() {
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);

		LayoutInflater inflator = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		View v = inflator.inflate(R.layout.view_actionbar, null);
		TextView titleView = (TextView) v.findViewById(R.id.title);
		titleView.setText(this.getTitle());
		titleView.setTypeface(Typeface.createFromAsset(getAssets(), "CEVA-CM.TTF"));
		titleView.setTextSize(30);
		titleView.setPadding(10, 20, 10, 20);
		actionBar.setCustomView(v);
	}

	public final void setAppFont(ViewGroup mContainer)
	{
		if (mContainer == null) return;
		final int mCount = mContainer.getChildCount();
		// Loop through all of the children.
		for (int i = 0; i < mCount; ++i)
		{
			final View mChild = mContainer.getChildAt(i);
			if (mChild instanceof TextView)
			{
				// Set the font if it is a TextView.
				Helper.setFont(this, ((TextView) mChild));
//				((TextView) mChild).setTypeface(Typeface.createFromAsset(getAssets(), "CEVA-CM.TTF"));
			}
			else if (mChild instanceof ViewGroup)
			{
				// Recursively attempt another ViewGroup.
				setAppFont((ViewGroup) mChild);
			}
		}
	}

}
