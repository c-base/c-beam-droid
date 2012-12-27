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

public class UserListFragment extends ArrayListFragment {
	List<User> items = new ArrayList<User>();
	ListAdapter adapter;
	Class nextActivity = UserActivity.class;
	
	public void clear() {
		items.clear();
	}
	public void addItem(User item) {
		items.add(item);
		 ((ArrayAdapter)getListView().getAdapter()).notifyDataSetChanged();
	}
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new ArrayAdapter<User>(getActivity(),
                android.R.layout.simple_list_item_1, items);
        setListAdapter(adapter);
        getListView().setPadding(5, 5, 5, 5);
        //getListView().setDividerHeight(0);
    }

	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent myIntent = new Intent(v.getContext(), nextActivity);
        myIntent.putExtra("id", items.get((int) id).getId());
        myIntent.putExtra("username", items.get((int) id).getUsername());
        startActivityForResult(myIntent, 0);
    }

}
