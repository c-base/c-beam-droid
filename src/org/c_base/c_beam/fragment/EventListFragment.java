package org.c_base.c_beam.fragment;

import java.util.ArrayList;

import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.activity.EventActivity;
import org.c_base.c_beam.domain.Event;
import org.c_base.c_beam.util.Helper;

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

public class EventListFragment extends ListFragment {
	ArrayList<Event> items = new ArrayList<Event>();
	ListAdapter adapter;
	Class nextActivity = EventActivity.class;
	SharedPreferences sharedPref;

	public void clear() {
		items.clear();
	}
	public void addItem(Event item) {
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
		adapter = new EventAdapter(getActivity(),
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
	public class EventAdapter extends ArrayAdapter {
		private ArrayList<Event> items;
		private Context context;

		@SuppressWarnings("unchecked")
		public EventAdapter(Context context, int textViewResourceId, ArrayList<Event> items) {
			super(context, textViewResourceId, items);
			this.context = context;
			this.items = items;
		}
 
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView view = (TextView) super.getView(position, convertView, parent);
//			Event event = items.get(position);
			Helper.setListItemStyle(view);
			Helper.setFont(getActivity(), view);
			return view;
		}

	}
}
