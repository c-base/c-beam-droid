package org.c_base.c_beam.fragment;

import android.content.Context;
import android.content.Intent;
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
import org.c_base.c_beam.activity.UserActivity;
import org.c_base.c_beam.domain.User;
import org.c_base.c_beam.util.Helper;

import java.text.NumberFormat;
import java.util.ArrayList;

public class UserListFragment extends ListFragment {
	private final ArrayList<User> items = new ArrayList<>();
	private final Class<?> nextActivity = UserActivity.class;

    public void clear() {
        items.clear();
    }

    public void addItem(User item) {
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
		setListAdapter(new UserAdapter(getActivity(),
				android.R.layout.simple_list_item_1, items));
	}

	@Override
	public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
		Intent myIntent = new Intent(v.getContext(), nextActivity);
		myIntent.putExtra("id", items.get(position).getId());
		startActivity(myIntent);
	}

	private static class UserAdapter extends ArrayAdapter<User> {
		private final ArrayList<User> items;

		public UserAdapter(Context context, int itemLayout, ArrayList<User> items) {
			super(context, itemLayout, items);
			this.items = items;
		}

		@NonNull
        @Override
		public View getView(int position, View convertView, @NonNull ViewGroup parent) {
			View v = super.getView(position, convertView, parent);
			TextView view = v.findViewById(android.R.id.text1);
            if (getContext() instanceof android.app.Activity) {
                Helper.setFont((android.app.Activity) getContext(), view);
            }
			User u = items.get(position);
			if (u.getStatus().equals("online")) {
				view.setText(u.getUsername());
			} else {
				double alpha = 0.5;
				try {
					alpha = Double.parseDouble(u.getEta().replace(",", "."));
				} catch (Exception ignored) {
				}
				NumberFormat defaultFormat = NumberFormat.getPercentInstance();
				view.setText(String.format("%s (%s)", u.getUsername(), defaultFormat.format(alpha)));
			}
			return v;
		}

	}

}
