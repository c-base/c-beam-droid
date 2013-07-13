package org.c_base.c_beam.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.domain.C_beam;
import org.c_base.c_beam.util.Helper;

import java.util.ArrayList;

public class C_outListFragment extends ListFragment {
    ArrayList<String> items = new ArrayList<String>();
    ArrayAdapter adapter;
    C_beam c_beam;
    SharedPreferences sharedPref;

    public C_outListFragment() {
        c_beam = C_beam.getInstance();
        c_beam.setActivity(this.getActivity());
    }

    public void clear() {
        items.clear();
    }

    public void addItem(String item) {
        items.add(item);
        adapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_view, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        adapter = new C_outAdapter(getActivity(),
                android.R.layout.simple_list_item_1, items);
        setListAdapter(adapter);
        if (sharedPref.getBoolean(Settings.C_THEME, true)) getListView().setDividerHeight(0);
        getListView().setHapticFeedbackEnabled(true);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        c_beam.play(items.get((int) id));
        //Toast.makeText(v.getContext(), R.string.c_out_sound_played, Toast.LENGTH_LONG).show();
    }

    @SuppressWarnings("rawtypes")
    public class C_outAdapter extends ArrayAdapter {
        private static final String TAG = "C_outAdapter";
        private ArrayList<String> items;
        private Context context;

        @SuppressWarnings("unchecked")
        public C_outAdapter(Context context, int textViewResourceId, ArrayList<String> items) {
            super(context, textViewResourceId, items);
            this.context = context;
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) super.getView(position, convertView, parent);
            String u = items.get(position);
            Helper.setListItemStyle(view);
            view.setPadding(25, 30, 25, 30);
            Helper.setFont(getActivity(), view);
            return view;
        }
    }
}
