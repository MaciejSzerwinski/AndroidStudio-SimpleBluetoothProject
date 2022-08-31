package com.example.bluetooth_app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int PERMISSION_CODE = 1000;
    private FragmentActivity frs;

    //declaration UI components
    BluetoothAdapter bluetoothAdapter;      //Declaration bluetoothAdapter which is required for any all Bluetooth activity. BluetoothAdapter represents the device's own Bluetooth adapter
    ListView pairedDevicesList;
    ListView scanDevicesList;
    Button turnBluetooth;
    Button showPairedListBtn;       //Show list last paired devices
    Button scanDevicesBtn;      //Button turn on scan mode
    private static ScannedDevicesList adapter;

    //variables
    //ArrayList<String> availableDeviceList = new ArrayList<String>();
    ArrayList<Device> dataModels;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // function destined to setup App
        setupUI();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(bluetoothState);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setupUI() {
        //describe UI components
        pairedDevicesList = (ListView) findViewById(R.id.pairedDevicesList);
        scanDevicesList = (ListView) findViewById(R.id.availableDevices);
        turnBluetooth = (Button) findViewById(R.id.turnBtn);
        showPairedListBtn = (Button) findViewById(R.id.showBtn);
        scanDevicesBtn = (Button) findViewById(R.id.refreshBtn);

        //declaration default bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        dataModels = new ArrayList<>();

        showPairedListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPairedDevices();
            }
        });

        turnBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.BLUETOOTH_CONNECT} ,2);
                        return;
                    }
                }
                enableDisableBT();
            }
        });

        scanDevicesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSearchingDevices();
            }
        });
    }

    private void startSearchingDevices() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                Log.i("MES", "SUCCESS");
                ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION} ,2);

        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED) {
            Log.i("MES", "SUCCESS");
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.BLUETOOTH_SCAN} ,2);

        }
            dataModels.clear();
            bluetoothAdapter.startDiscovery();
        registerReceiver(bluetoothState, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(bluetoothState, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(bluetoothState, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

    }

    private void showPairedDevices() {
        if (pairedDevicesList.getVisibility() == View.VISIBLE) {
            pairedDevicesList.setVisibility(View.GONE);
        } else {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
                Log.i("MES", "SUCCESS");
                ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.BLUETOOTH_CONNECT} ,2);
            }
            pairedDevicesList.setVisibility(View.VISIBLE);
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            String[] strings = new String[pairedDevices.size()];
            int index = 0;
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    strings[index] = device.getName();
                    index++;
                }
                PairedDevicesList customBaseAdapter = new PairedDevicesList(getApplicationContext(), strings);
                pairedDevicesList.setAdapter(customBaseAdapter);
            }
        }
    }

    public void enableDisableBT() {
        if (bluetoothAdapter == null) {
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                startActivity(intent);

                IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(bluetoothState, filter);
            }
        }
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();

            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(bluetoothState, filter);
        }
    }

    private final BroadcastReceiver bluetoothState = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();         //String variable, which store actual intent status

//            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
//                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, bluetoothAdapter.ERROR); //Retrieve extended data from the intent or received
//                // error value. BluetoothAdapter.EXTRA_STATE, get state bluetoothAdapter
//
//                switch (state) {
//                    case BluetoothAdapter.STATE_OFF:
//                        Log.d(TAG, "onReceive: STATE OFF");
//                        break;
//                    case BluetoothAdapter.STATE_TURNING_OFF:
//                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
//                        break;
//                    case BluetoothAdapter.STATE_ON:
//                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
//                        break;
//                    case BluetoothAdapter.STATE_TURNING_ON:
//                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
//                        break;
//                }
//            }
//
//            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
//                int modeValue = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
//
//                switch (modeValue) {
//                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
//                        Log.d(TAG, "onReceive: The device is not in discoverable mode but can still receive connection");
//                        break;
//
//                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
//                        Log.d(TAG, "onReceive: The device is in discoverable mode");
//                        break;
//                    case BluetoothAdapter.SCAN_MODE_NONE:
//                        Log.d(TAG, "onReceive: The device is not in discoverable mode and can not receive connection");
//                        break;
//                    default:
//                        Log.d(TAG, "onReceive: Error");
//                }
//            }

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.i("WIADOMOSC", "GOWNOOOOOOOOO");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device != null) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
                        Log.i("MES", "SUCCESS");
                        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.BLUETOOTH_CONNECT} ,2);

                    }
                        String deviceName = device.getName();
                        String deviceHardwareAddress = device.getAddress();
                        dataModels.add(new Device(deviceName, deviceHardwareAddress));
                    adapter = new ScannedDevicesList(dataModels, getApplicationContext());
                    Log.i("WIADOMOSC", device.getName() + "\n" + device.getAddress());
                    scanDevicesList.setAdapter(adapter);
                    scanDevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            Device dataModel= dataModels.get(position);

                            Snackbar.make(view, dataModel.getName()+"\n"+dataModel.getAddressMAC(), Snackbar.LENGTH_LONG)
                                    .setAction("No action", null).show();
                        }
                    });
                }
            }
        }
    };
}