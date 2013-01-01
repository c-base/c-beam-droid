package org.c_base.c_beam;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class C_portalListFragment extends ArrayListFragment {
	List<Article> items = new ArrayList<Article>();
	ListAdapter adapter;
	Class nextActivity = C_PortalActivity.class;

	public void clear() {
		items.clear();
	}

	public void addItem(Article item) {
		items.add(item);
		((ArrayAdapter)getListView().getAdapter()).notifyDataSetChanged();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		adapter = new ArrayAdapter<Article>(getActivity(),
				android.R.layout.simple_list_item_1, items);
		setListAdapter(adapter);
		getListView().setPadding(5, 5, 5, 5);
		//getListView().setDividerHeight(0);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent myIntent = new Intent(v.getContext(), nextActivity);
		myIntent.putExtra("id", items.get((int) id).getId());
//		myIntent.putExtra("username", items.get((int) id).getUsername());
		startActivityForResult(myIntent, 0);
	}
}
