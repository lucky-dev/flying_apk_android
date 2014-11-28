package com.flyingapk.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.flyingapk.R;
import com.flyingapk.models.AndroidApp;

import java.util.ArrayList;
import java.util.List;

public class AppsAdapter extends BaseAdapter {

    private List<AndroidApp> mData;
    private LayoutInflater mInflater;

    public AppsAdapter(LayoutInflater inflater) {
        mInflater = inflater;

        mData = new ArrayList<AndroidApp>();
    }

    public void addItem(AndroidApp item) {
        mData.add(item);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public AndroidApp getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return mData.size();
    }

    public void clear() {
        mData.clear();
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AndroidApp androidApp = mData.get(position);

        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();

            convertView = mInflater.inflate(R.layout.item_app, null);

            holder.tvNameApp = (TextView) convertView.findViewById(R.id.tv_name_app);
            holder.tvDescriptionApp = (TextView) convertView.findViewById(R.id.tv_description_app);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvNameApp.setText(androidApp.getName());
        holder.tvDescriptionApp.setText(androidApp.getDescription());

        return convertView;
    }

    public class ViewHolder {
        public TextView tvNameApp;
        public TextView tvDescriptionApp;
    }

}
