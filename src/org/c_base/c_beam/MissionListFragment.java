package org.c_base.c_beam;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class MissionListFragment extends ArrayListFragment {
	List<Mission> items = new ArrayList<Mission>();
	ListAdapter adapter;
	Class nextActivity = MissionActivity.class;
	
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
        adapter = new ArrayAdapter<Mission>(getActivity(),
                android.R.layout.simple_list_item_1, items);
        setListAdapter(adapter);
    }

	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent myIntent = new Intent(v.getContext(), nextActivity);
        myIntent.putExtra("id", items.get((int) id).getId());
        startActivityForResult(myIntent, 0);
    }
}
