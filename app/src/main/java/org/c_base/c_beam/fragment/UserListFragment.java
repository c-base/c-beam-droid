package org.c_base.c_beam.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.activity.UserActivity;
import org.c_base.c_beam.domain.User;
import org.c_base.c_beam.util.Helper;

import java.text.NumberFormat;
import java.util.ArrayList;

public class UserListFragment extends ListFragment {
	ArrayList<User> items = new ArrayList<User>();
	UserAdapter adapter;
	Class nextActivity = UserActivity.class;
	SharedPreferences sharedPref;
	private String emptyText = null;

	public void clear() {
		items.clear();
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
	}

	public void setEmptyText(String text) {
		this.emptyText = text;
		updateEmptyView();
	}

	private void updateEmptyView() {
		if (getView() != null && emptyText != null) {
			TextView tv = getView().findViewById(R.id.no_users_online);
			if (tv != null) {
				tv.setText(emptyText);
				tv.setVisibility(View.VISIBLE);
			}
			View pb = getView().findViewById(R.id.progress_bar);
			if (pb != null) {
				pb.setVisibility(View.GONE);
			}
		}
	}

	public void addItem(User item) {
		items.add(item);
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
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
		updateEmptyView();
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
		private final ArrayList<User> items;
		private final Context context;

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
			double alpha = u.getAutologout_in() / (float) u.getAutologout();

//			if (fadeUsers) view.setAlpha(alpha);
			Helper.setListItemStyle(view);
			Helper.setFont(getActivity(), view);

			if (u.getStatus().equals("online")) {
				view.setText(u.getUsername() + " (" + defaultFormat.format(alpha) + ")");
			} else {
				view.setText(u.toString());
			}
			return view;
		}

	}
}
