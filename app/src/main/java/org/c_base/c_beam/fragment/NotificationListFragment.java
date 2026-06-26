package org.c_base.c_beam.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.c_base.c_beam.domain.Notification;
import org.c_base.c_beam.util.Helper;

import java.util.ArrayList;

public class NotificationListFragment extends ListFragment {
	private ArrayList<Notification> items = new ArrayList<>();

    public void clear() {
        items.clear();
    }

    public void setItems(ArrayList<Notification> items) {
        this.items = items;
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListAdapter(new NotificationAdapter(getActivity(),
				android.R.layout.simple_list_item_1, items));
	}

	private static class NotificationAdapter extends ArrayAdapter<Notification> {
		private final ArrayList<Notification> items;

		public NotificationAdapter(Context context, int itemLayout, ArrayList<Notification> items) {
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
			tv.setText(items.get(position).getNotification());
			return v;
		}

	}

}
