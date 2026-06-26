package org.c_base.c_beam.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.c_base.c_beam.R;
import org.c_base.c_beam.activity.EventActivity;
import org.c_base.c_beam.domain.Event;
import org.c_base.c_beam.util.Helper;

import java.util.ArrayList;

public class EventListFragment extends ListFragment {
	private final ArrayList<Event> items = new ArrayList<>();
	Class<?> nextActivity = EventActivity.class;
	SharedPreferences sharedPref;

    public void clear() {
        items.clear();
    }

    public void addItem(Event item) {
        items.add(item);
        if (getListAdapter() != null) {
            ((ArrayAdapter<?>) getListAdapter()).notifyDataSetChanged();
        }
    }

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list_view, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListAdapter(new EventAdapter(getActivity(),
				android.R.layout.simple_list_item_1, items));
		if (getActivity() != null) {
            sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        }
	}

	@Override
	public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
		Intent myIntent = new Intent(v.getContext(), nextActivity);
		myIntent.putExtra("id", items.get(position).getId());
		startActivity(myIntent);
	}

	private static class EventAdapter extends ArrayAdapter<Event> {
		private final ArrayList<Event> items;

		public EventAdapter(Context context, int itemLayout, ArrayList<Event> items) {
			super(context, itemLayout, items);
			this.items = items;
		}

		@NonNull
        @Override
		public View getView(int position, View convertView, @NonNull ViewGroup parent) {
			View v = super.getView(position, convertView, parent);
			TextView tv = v.findViewById(android.R.id.text1);
            if (getContext() instanceof android.app.Activity) {
                Helper.setFont((android.app.Activity) getContext(), tv);
            }
			tv.setText(items.get(position).toString());
			return v;
		}

	}

}
