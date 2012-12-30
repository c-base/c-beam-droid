package org.c_base.c_beam;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class C_outListFragment extends ArrayListFragment {
	List<String> items = new ArrayList<String>();
	ListAdapter adapter;
	C_beam c_beam;
	
	
	public C_outListFragment() {
		c_beam = new C_beam();
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
        Log.i("FragmentList", "Item clicked: " + items.get((int) id));
        c_beam.play(items.get((int) id));
        Toast.makeText(v.getContext(), R.string.c_out_sound_played, Toast.LENGTH_LONG).show();
    }
    
   
}	
