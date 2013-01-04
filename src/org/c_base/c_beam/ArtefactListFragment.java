package org.c_base.c_beam;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ArtefactListFragment extends ArrayListFragment {
	String TAG = "ArtefactListFragment";
	ArrayList<Artefact> items = new ArrayList<Artefact>();
	ListAdapter adapter;
	Class nextActivity = ArtefactActivity.class;
	
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
        adapter = new ArtefactAdapter(getActivity(),
                android.R.layout.simple_list_item_1, items);
        setListAdapter(adapter);
        getListView().setDividerHeight(0);
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
			view.setBackgroundResource(R.drawable.listitembg);
			view.setPadding(25, 5, 25, 5);
			setFont(view);
			view.setText(a.getName());
			return view;
		}
		
		private void setFont(TextView view) {
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(view.getContext());
			String font = sharedPref.getString("pref_font", "Android Default");

			if (font.equals("X-Scale")) {
				view.setPadding(25, 10, 25, 25);
				Typeface myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "X-SCALE.TTF");
				view.setTypeface(myTypeface);
				view.setTextSize(20);
			} else if (font.equals("Ceva")) {
				view.setPadding(25, 25, 25, 25);
				Typeface myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "CEVA-CM.TTF");
				view.setTypeface(myTypeface);
				view.setTextSize(20);
			} else {
				view.setPadding(25, 25, 25, 25);
			}

			view.setGravity(Gravity.CENTER_VERTICAL);		
		}
	}
}
