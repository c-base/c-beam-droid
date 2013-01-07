package org.c_base.c_beam;

import java.text.NumberFormat;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.TextView;

public class UserListFragment extends ArrayListFragment {
	ArrayList<User> items = new ArrayList<User>();
	ListAdapter adapter;
	Class nextActivity = UserActivity.class;
	SharedPreferences sharedPref;

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
		sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		adapter = new UserAdapter(getActivity(),
				android.R.layout.simple_list_item_1, items);
		setListAdapter(adapter);
		//getListView().setPadding(5, 5, 5, 5);
		if (sharedPref.getBoolean("pref_c_theme", true)) getListView().setDividerHeight(0);
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
			boolean fadeUsers = sharedPref.getBoolean("pref_fade_users", true);

			NumberFormat defaultFormat = NumberFormat.getPercentInstance();
			defaultFormat.setMinimumFractionDigits(1);
			TextView view = (TextView) super.getView(position, convertView, parent);
			User u = items.get(position);
			float alpha = u.getAutologout_in()/(float) u.getAutologout();

			if (fadeUsers) view.setAlpha(alpha);
			if (sharedPref.getBoolean("pref_c_theme", true)) view.setBackgroundResource(R.drawable.listitembg);

			Helper.setFont(getActivity(), view);

			if (u.getStatus().equals("online"))
				view.setText(u.getUsername()+" ("+defaultFormat.format(alpha)+")");
			return view;
		}

	}
}
