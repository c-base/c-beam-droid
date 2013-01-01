package org.c_base.c_beam;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class ArtefactListFragment extends ArrayListFragment {
	String TAG = "ArtefactListFragment";
	List<Artefact> items = new ArrayList<Artefact>();
	ListAdapter adapter;
	Class nextActivity = ArtefactActivity.class;
	
	public void clear() {
		items.clear();
	}
	public void addItem(Artefact item) {
		items.add(item);
		 ((ArrayAdapter)getListView().getAdapter()).notifyDataSetChanged();
	}
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new ArrayAdapter<Artefact>(getActivity(),
                android.R.layout.simple_list_item_1, items);
        setListAdapter(adapter);
    }

	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent myIntent = new Intent(v.getContext(), nextActivity);
        myIntent.putExtra("slug", items.get((int) id).getSlug());
        Log.i(TAG, "slug: "+ items.get((int) id).getSlug());
        startActivityForResult(myIntent, 0);
    }
	public int size() {
		// TODO Auto-generated method stub
		return items.size();
	}
}
