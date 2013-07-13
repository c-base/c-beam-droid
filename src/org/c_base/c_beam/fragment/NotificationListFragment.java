package org.c_base.c_beam.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.c_base.c_beam.activity.UserActivity;
import org.c_base.c_beam.domain.Notification;
import org.c_base.c_beam.util.Helper;

import java.text.NumberFormat;
import java.util.ArrayList;

public class NotificationListFragment extends ListFragment {
    ArrayList<Notification> items = new ArrayList<Notification>();
    ListAdapter adapter;
    Class nextActivity = UserActivity.class;

    SharedPreferences sharedPref;

    public NotificationListFragment() {

    }

    public NotificationListFragment(ArrayList<Notification> items) {
        this.items = items;
    }

    public void clear() {
        items.clear();
    }

    public void addItem(Notification item) {
        items.add(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new C_beamAdapter(getActivity(),
                android.R.layout.simple_list_item_1, items);
        setListAdapter(adapter);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    }

    public void setNextActivity(Class cls) {
        nextActivity = cls;
    }

    public class C_beamAdapter<T> extends ArrayAdapter<Notification> {
        private static final String TAG = "UserAdapter";
        private ArrayList<Notification> items;
        private Context context;

        public C_beamAdapter(Context context, int textViewResourceId, ArrayList<Notification> items) {
            super(context, textViewResourceId, items);
            this.context = context;
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            NumberFormat defaultFormat = NumberFormat.getPercentInstance();
            defaultFormat.setMinimumFractionDigits(1);
            TextView view = (TextView) super.getView(position, convertView, parent);
            Notification s = items.get(position);

            Helper.setListItemStyle(view);
            Helper.setFont(getActivity(), view);

            view.setText(s.toString());
            return view;
        }

    }
}	