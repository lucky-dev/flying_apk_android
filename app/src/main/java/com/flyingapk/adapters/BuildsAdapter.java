package com.flyingapk.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.flyingapk.R;
import com.flyingapk.models.Build;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BuildsAdapter extends BaseAdapter {

    private List<Build> mData;
    private LayoutInflater mInflater;
    private BuildsAdapterListener mBuildsAdapterListener;

    public BuildsAdapter(LayoutInflater inflater) {
        mInflater = inflater;

        mData = new ArrayList<Build>();
    }

    public void addItem(Build item) {
        mData.add(item);
    }

    public void setListener(BuildsAdapterListener buildsAdapterListener) {
        mBuildsAdapterListener = buildsAdapterListener;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Build getItem(int position) {
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
        Build build = mData.get(position);

        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();

            convertView = mInflater.inflate(R.layout.item_build, null);

            holder.tvNumberBuild = (TextView) convertView.findViewById(R.id.tv_number_build);
            holder.tvDateBuild = (TextView) convertView.findViewById(R.id.tv_date_build);
            holder.btnDownloadBuild = (Button) convertView.findViewById(R.id.btn_download_build);
            holder.btnDownloadBuild.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mBuildsAdapterListener != null) {
                        int pos = (Integer) v.getTag();
                        mBuildsAdapterListener.onDownloadBuild(pos);
                    }
                }
            });

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvNumberBuild.setText(build.getName());
        holder.tvDateBuild.setText(showDateWithFormat(build.getCreatedTime(), "yyyy-MM-dd HH:mm:ss"));
        holder.btnDownloadBuild.setTag(position);

        return convertView;
    }

    private String showDateWithFormat(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

    public class ViewHolder {
        public TextView tvNumberBuild;
        public TextView tvDateBuild;
        public TextView btnDownloadBuild;
    }

    public interface BuildsAdapterListener {
        public void onDownloadBuild(int position);
    }

}
