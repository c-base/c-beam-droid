package org.c_base.c_beam;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class UserListFragment extends ArrayListFragment {
	ArrayList<User> items = new ArrayList<User>();
	ListAdapter adapter;
	Class nextActivity = UserActivity.class;

	public void clear() {
		items.clear();
	}

	public void addItem(User item) {
		items.add(item);
		((ArrayAdapter)getListView().getAdapter()).notifyDataSetChanged();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		adapter = new UserAdapter(getActivity(),
				android.R.layout.simple_list_item_1, items);
		setListAdapter(adapter);
		//getListView().setPadding(5, 5, 5, 5);
		getListView().setDividerHeight(0);
		getListView().setHapticFeedbackEnabled(true);
		//getListView().setDividerHeight(0);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent myIntent = new Intent(v.getContext(), nextActivity);
		myIntent.putExtra("id", items.get((int) id).getId());
		myIntent.putExtra("username", items.get((int) id).getUsername());
		startActivityForResult(myIntent, 0);
	}

	@SuppressWarnings("rawtypes")
	public class UserAdapter extends ArrayAdapter {
		private static final String TAG = "UserAdapter";
		private ArrayList<User> items;
		private Context context;

		@SuppressWarnings("unchecked")
		public UserAdapter(Context context, int textViewResourceId, ArrayList<User> items) {
			super(context, textViewResourceId, items);
			this.context = context;
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			NumberFormat defaultFormat = NumberFormat.getPercentInstance();
			defaultFormat.setMinimumFractionDigits(1);
			TextView view = (TextView) super.getView(position, convertView, parent);
			User u = items.get(position);
			float alpha = u.getAutologout_in()/(float) u.getAutologout();
			view.setAlpha(alpha);
			view.setBackgroundResource(R.drawable.listitembg);
			setFont(view);

			if (u.getStatus().equals("online"))
				view.setText(u.getUsername()+" ("+defaultFormat.format(alpha)+")");
			return view;
		}

		private void setFont(TextView view) {
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(view.getContext());
			String font = sharedPref.getString("pref_font", "Android Default");

			if (font.equals("X-Scale")) {
				view.setPadding(25, 10, 25, 25);
				Typeface myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "X-SCALE.TTF");
				view.setTypeface(myTypeface);
				view.setTextSize(20);
			} else if (font.equals("Ceva")) {
				view.setPadding(25, 25, 25, 25);
				Typeface myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "CEVA-CM.TTF");
				view.setTypeface(myTypeface);
				view.setTextSize(20);
			} else {
				view.setPadding(25, 25, 25, 25);
			}

			view.setGravity(Gravity.CENTER_VERTICAL);		
		}
	}
}
