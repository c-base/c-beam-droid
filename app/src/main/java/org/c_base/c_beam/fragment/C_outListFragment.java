package org.c_base.c_beam.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.c_base.c_beam.R;
import org.c_base.c_beam.domain.C_beam;
import org.c_base.c_beam.util.Helper;

import java.util.ArrayList;

public class C_outListFragment extends ListFragment {
	private final ArrayList<String> items = new ArrayList<>();

    public void clear() {
        items.clear();
    }

    public void addItem(String item) {
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
		setListAdapter(new C_outAdapter(getActivity(),
				android.R.layout.simple_list_item_1, items));
	}

	@Override
	public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
		C_beam.getInstance().play(items.get(position));
	}

	private static class C_outAdapter extends ArrayAdapter<String> {
		private final ArrayList<String> items;

		public C_outAdapter(Context context, int itemLayout, ArrayList<String> items) {
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
			tv.setText(items.get(position));
			return v;
		}

	}

}
