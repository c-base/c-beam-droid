package org.c_base.c_beam.fragment;

import java.text.NumberFormat;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import org.c_base.c_beam.domain.Artefact;
import org.c_base.c_beam.activity.ArtefactActivity;
import org.c_base.c_beam.util.Helper;
import org.c_base.c_beam.R;

public class ArtefactListFragment extends ArrayListFragment {
	String TAG = "ArtefactListFragment";
	ArrayList<Artefact> items = new ArrayList<Artefact>();
	ListAdapter adapter;
	Class nextActivity = ArtefactActivity.class;
	SharedPreferences sharedPref;

	public void clear() {
		Log.i("ArtefactListFragment", "clear");
		items.clear();
	}
	public void addItem(Artefact item) {
		//Log.d("ArtefactListFragment", "add: "+item);
		items.add(item);
		//((ArrayAdapter)getListView().getAdapter()).notifyDataSetChanged();
	}
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        adapter = new ArtefactAdapter(getActivity(),
                android.R.layout.simple_list_item_1, items);
        setListAdapter(adapter);
        if (sharedPref.getBoolean("pref_c_theme", true)) getListView().setDividerHeight(0);
    }

	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent myIntent = new Intent(v.getContext(), nextActivity);
        myIntent.putExtra("slug", items.get((int) id).getSlug());
        Log.i(TAG, "slug: "+ items.get((int) id).getSlug());
        startActivityForResult(myIntent, 0);
    }
	public int size() {
		return items.size();
	}
	public void setArrayList(ArrayList<Artefact> artefactList) {
		items = artefactList;
		//((ArrayAdapter)getListView().getAdapter()).notifyDataSetChanged();
	}

	
	@SuppressWarnings("rawtypes")
	public class ArtefactAdapter extends ArrayAdapter {
		private static final String TAG = "UserAdapter";
		private ArrayList<Artefact> items;
		private Context context;

		@SuppressWarnings("unchecked")
		public ArtefactAdapter(Context context, int textViewResourceId, ArrayList<Artefact> items) {
			super(context, textViewResourceId, items);
			this.context = context;
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			NumberFormat defaultFormat = NumberFormat.getPercentInstance();
			defaultFormat.setMinimumFractionDigits(1);
			TextView view = (TextView) super.getView(position, convertView, parent);
			Artefact a = items.get(position);
			if (sharedPref.getBoolean("pref_c_theme", true)) view.setBackgroundResource(R.drawable.listitembg);
//			view.setPadding(25, 5, 25, 5);
//			setFont(view);
			Helper.setFont(getActivity(), view);
			view.setText(a.getName());
			return view;
		}
		
	}
}
