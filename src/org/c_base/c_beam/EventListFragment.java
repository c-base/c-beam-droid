package org.c_base.c_beam;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class EventListFragment extends ArrayListFragment {
	ArrayList<Event> items = new ArrayList<Event>();
	ListAdapter adapter;
	Class nextActivity = EventActivity.class;

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
		adapter = new EventAdapter(getActivity(),
				android.R.layout.simple_list_item_1, items);
		setListAdapter(adapter);
        getListView().setDividerHeight(0);
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
			view.setBackgroundResource(R.drawable.listitembg);
			view.setPadding(25, 30, 25, 30);
			return view;
		}
	}
}
