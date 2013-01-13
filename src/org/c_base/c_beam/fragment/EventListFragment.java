package org.c_base.c_beam.fragment;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import org.c_base.c_beam.domain.Event;
import org.c_base.c_beam.activity.EventActivity;
import org.c_base.c_beam.util.Helper;
import org.c_base.c_beam.R;

public class EventListFragment extends ArrayListFragment {
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
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		adapter = new EventAdapter(getActivity(),
				android.R.layout.simple_list_item_1, items);
		setListAdapter(adapter);
		if (sharedPref.getBoolean("pref_c_theme", true)) getListView().setDividerHeight(0);
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
		private static final String TAG = "EventAdapter";
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
			Event u = items.get(position);
			if (sharedPref.getBoolean("pref_c_theme", true)) view.setBackgroundResource(R.drawable.listitembg);
			Helper.setFont(getActivity(), view);
			return view;
		}
		
	}
}
