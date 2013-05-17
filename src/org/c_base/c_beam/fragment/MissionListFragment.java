package org.c_base.c_beam.fragment;

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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import org.c_base.c_beam.util.Helper;
import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.activity.MissionDetailActivity;
import org.c_base.c_beam.domain.Mission;

public class MissionListFragment extends ListFragment {
	ArrayList<Mission> items = new ArrayList<Mission>();
	ListAdapter adapter;
	Class nextActivity = MissionDetailActivity.class;
	SharedPreferences sharedPref;

	public void clear() {
		items.clear();
	}
	public void addItem(Mission item) {
		items.add(item);
		((ArrayAdapter)getListView().getAdapter()).notifyDataSetChanged();
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
		sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		adapter = new MissionAdapter(getActivity(),
				android.R.layout.simple_list_item_1, items);
		setListAdapter(adapter);
		if (sharedPref.getBoolean(Settings.C_THEME, true)) getListView().setDividerHeight(0);
		getListView().setHapticFeedbackEnabled(true);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent myIntent = new Intent(v.getContext(), nextActivity);
		myIntent.putExtra("id", items.get((int) id).getId());
		startActivityForResult(myIntent, 0);
	}

	@SuppressWarnings("rawtypes")
	public class MissionAdapter extends ArrayAdapter {
		private static final String TAG = "MissionAdapter";
		private ArrayList<Mission> items;
		private Context context;

		@SuppressWarnings("unchecked")
		public MissionAdapter(Context context, int textViewResourceId, ArrayList<Mission> items) {
			super(context, textViewResourceId, items);
			this.context = context;
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView view = (TextView) super.getView(position, convertView, parent);
			Mission u = items.get(position);
			Helper.setListItemStyle(view);
			view.setPadding(25, 30, 25, 30);
			Helper.setFont(getActivity(), view);
			return view;
		}
	}


}
