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
import org.c_base.c_beam.util.Helper;
import org.c_base.c_beam.R;
import org.c_base.c_beam.activity.MissionActivity;
import org.c_base.c_beam.domain.Mission;

public class MissionListFragment extends ArrayListFragment {
	ArrayList<Mission> items = new ArrayList<Mission>();
	ListAdapter adapter;
	Class nextActivity = MissionActivity.class;
	SharedPreferences sharedPref;

	public void clear() {
		items.clear();
	}
	public void addItem(Mission item) {
		items.add(item);
		 ((ArrayAdapter)getListView().getAdapter()).notifyDataSetChanged();
	}
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        adapter = new MissionAdapter(getActivity(),
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
			if (sharedPref.getBoolean("pref_c_theme", true)) view.setBackgroundResource(R.drawable.listitembg);
			view.setPadding(25, 30, 25, 30);
			Helper.setFont(getActivity(), view);
			return view;
		}		
	}
}
