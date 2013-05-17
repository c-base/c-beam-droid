package org.c_base.c_beam.fragment;

import java.text.NumberFormat;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import org.c_base.c_beam.util.Helper;
import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.activity.UserActivity;
import org.c_base.c_beam.domain.User;

public class UserListFragment extends ListFragment {
	ArrayList<User> items = new ArrayList<User>();
	UserAdapter adapter;
	Class nextActivity = UserActivity.class;
	SharedPreferences sharedPref;

	public void clear() {
		items.clear();
	}

	public void addItem(User item) {
		items.add(item);
		adapter.notifyDataSetChanged();
	}

	// Override onCreateView() so we can use a custom empty view
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list_view, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		adapter = new UserAdapter(getActivity(), android.R.layout.simple_list_item_1, items);
		setListAdapter(adapter);

		sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		if (sharedPref.getBoolean(Settings.C_THEME, true)) {
			getListView().setDividerHeight(0);
		}
		getListView().setHapticFeedbackEnabled(true);
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
			boolean fadeUsers = sharedPref.getBoolean(Settings.FADE_USERS, true);

			NumberFormat defaultFormat = NumberFormat.getPercentInstance();
			defaultFormat.setMinimumFractionDigits(1);
			TextView view = (TextView) super.getView(position, convertView, parent);
			User u = items.get(position);
			float alpha = u.getAutologout_in()/(float) u.getAutologout();

//			if (fadeUsers) view.setAlpha(alpha);
			Helper.setListItemStyle(view);
			Helper.setFont(getActivity(), view);

			if (u.getStatus().equals("online"))
				view.setText(u.getUsername()+" ("+defaultFormat.format(alpha)+")");
			return view;
		}

	}
}
