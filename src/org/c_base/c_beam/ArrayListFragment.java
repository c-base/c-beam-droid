package org.c_base.c_beam;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class ArrayListFragment extends ListFragment {
	List<String> items = new ArrayList<String>();
	ListAdapter adapter;
	Class nextActivity = UserActivity.class;
	
	public ArrayListFragment() {
		
	}
	
	public void clear() {
		items.clear();
	}
	public void addItem(String item) {
		items.add(item);
		
		 //((ArrayAdapter)getListView().getAdapter()).notifyDataSetChanged(); 
	}
	
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, items);
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.i("FragmentList", "Item clicked: " + id);
        Intent myIntent = new Intent(v.getContext(), nextActivity);
        myIntent.putExtra("username", items.get((int) id));
        startActivityForResult(myIntent, 0);
    }
    
    public void setNextActivity(Class cls) {
    	nextActivity = cls;
    }
}	