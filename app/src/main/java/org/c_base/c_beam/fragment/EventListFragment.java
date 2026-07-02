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
import androidx.fragment.app.ListFragment;
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
	private String emptyText = null;

	public void clear() {
		items.clear();
		if (adapter != null) {
			((ArrayAdapter) adapter).notifyDataSetChanged();
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

	public void addItem(Event item) {
		items.add(item);
		if (adapter != null) {
			((ArrayAdapter) adapter).notifyDataSetChanged();
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
		sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		adapter = new EventAdapter(getActivity(),
				android.R.layout.simple_list_item_1, items);
		setListAdapter(adapter);
		if (sharedPref.getBoolean(Settings.C_THEME, true)) getListView().setDividerHeight(0);
		getListView().setHapticFeedbackEnabled(true);
		updateEmptyView();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
	    Intent myIntent = new Intent(v.getContext(), nextActivity);
		myIntent.putExtra("id", items.get((int) id).getId());
		startActivityForResult(myIntent, 0);
	}

	@SuppressWarnings("rawtypes")
	public class EventAdapter extends ArrayAdapter {
		private final ArrayList<Event> items;
		private final Context context;

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
