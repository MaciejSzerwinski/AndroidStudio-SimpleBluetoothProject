package com.example.bluetooth_app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class ScannedDevicesList extends ArrayAdapter<Device> implements View.OnClickListener {

    private ArrayList<Device> deviceSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView deviceName;
        TextView deviceMACAddr;
    }

    public ScannedDevicesList(ArrayList<Device> data, Context context) {
        super(context, R.layout.scaned_devices_list, data);
        this.deviceSet = data;
        this.mContext=context;

    }

    @Override
    public void onClick(View v) {

        int position=(Integer) v.getTag();
        Object object= getItem(position);
        Device dataModel=(Device)object;

        switch (v.getId())
        {
            case R.id.device_name:
                Snackbar.make(v, "MAC " +dataModel.getAddressMAC(), Snackbar.LENGTH_LONG)
                        .setAction("No action", null).show();
                break;
        }
    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Device dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.scaned_devices_list, parent, false);
            viewHolder.deviceName = (TextView) convertView.findViewById(R.id.device_name);
            viewHolder.deviceMACAddr = (TextView) convertView.findViewById(R.id.device_MAC_addr);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        lastPosition = position;

        viewHolder.deviceName.setText(dataModel.getName());
        viewHolder.deviceMACAddr.setText(dataModel.getAddressMAC());
        // Return the completed view to render on screen
        return convertView;
    }
}
