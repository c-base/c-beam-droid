package org.c_base.c_beam.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.c_base.c_beam.activity.UserActivity;
import org.c_base.c_beam.util.Helper;

import java.util.ArrayList;

public class ArrayListFragment extends ListFragment {
	ArrayList<String> items = new ArrayList<>();
	Class<?> nextActivity = UserActivity.class;
	SharedPreferences sharedPref;

    public void clear() {
        items.clear();
    }

    public void addItem(String item) {
        items.add(item);
        if (getListAdapter() != null) {
            ((C_beamAdapter) getListAdapter()).notifyDataSetChanged();
        }
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		C_beamAdapter adapter = new C_beamAdapter(getActivity(),
				android.R.layout.simple_list_item_1, items);
		setListAdapter(adapter);
		if (getActivity() != null) {
            sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        }
	}

	@Override
	public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
		Intent intent = new Intent(getActivity(), nextActivity);
		intent.putExtra("id", position);
		startActivity(intent);
	}

    public void setNextActivity(Class<?> cls) {
        nextActivity = cls;
    }

	public static class C_beamAdapter extends ArrayAdapter<String> {
		private final ArrayList<String> items;

		public C_beamAdapter(Context context, int itemLayout, ArrayList<String> items) {
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
