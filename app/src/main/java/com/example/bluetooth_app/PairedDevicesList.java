package com.example.bluetooth_app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PairedDevicesList extends BaseAdapter {

    Context context;
    String[] deviceList;
    LayoutInflater inflater;

    public PairedDevicesList(Context context, String [] deviceList) {
        this.context = context;
        this.deviceList = deviceList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return deviceList.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.paired_devices_list, null);
        TextView name = (TextView) convertView.findViewById(R.id.name);
        name.setText(deviceList[position]);
        return convertView;
    }
}
