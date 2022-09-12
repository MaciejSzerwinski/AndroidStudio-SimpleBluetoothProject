package com.example.bluetooth_app;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    //declaration UI components
    BluetoothAdapter bluetoothAdapter;      //Declaration bluetoothAdapter which is required for any all Bluetooth activity. BluetoothAdapter represents the device's own Bluetooth adapter
    ListView pairedDevicesList;
    ListView scanDevicesList;
    Switch turnBluetooth;
    Button showPairedListBtn;       //Show list last paired devices
    Button scanDevicesBtn;      //Button turn on scan mode
    TextView availableDevicesTxt;
    TextView deviceNameTxt;
    TextView MACAddressTxt;
    TextView pairedDeviceTxt;

    //variables
    ArrayList<Device> dataModels;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();    //Hide action bar
        actionBar.hide();

        // function destined to setup App
        setupUI();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        //unregisterReceiver(bluetoothStatus);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void setupUI() {
        //describe UI components
        pairedDevicesList = (ListView) findViewById(R.id.pairedDevicesList);
        scanDevicesList = (ListView) findViewById(R.id.availableDevices);
        turnBluetooth = (Switch) findViewById(R.id.turnBlth);
        showPairedListBtn = (Button) findViewById(R.id.showBtn);
        scanDevicesBtn = (Button) findViewById(R.id.refreshBtn);
        availableDevicesTxt = (TextView) findViewById(R.id.availableDevicesTxt);
        deviceNameTxt = (TextView) findViewById(R.id.deviceNameTxt);
        MACAddressTxt = (TextView) findViewById(R.id.MACAddressTxt);
        pairedDeviceTxt = (TextView) findViewById(R.id.pairedDeviceTxt);

        //declaration default bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        dataModels = new ArrayList<>();

        checkConnectPermission();

        //checking while app is running whether being noticed bluetooth changed status
        IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothStatus, BTIntent);

        //check bluetooth status after run app
        setBluetoothState();

        turnBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void setBluetoothState() {
        if(bluetoothAdapter.isEnabled()) {
            turnBluetooth.setChecked(true);

            availableDevicesTxt.setVisibility(VISIBLE);
            deviceNameTxt.setVisibility(VISIBLE);
            MACAddressTxt.setVisibility(VISIBLE);
            pairedDeviceTxt.setVisibility(VISIBLE);

            scanDevicesBtn.setVisibility(VISIBLE);
            scanDevicesBtn.setEnabled(false);

            startSearchingDevices();
            showPairedDevices();
        }
        else {
            deviceNameTxt.setVisibility(GONE);
            MACAddressTxt.setVisibility(GONE);
            pairedDeviceTxt.setVisibility(GONE);
            availableDevicesTxt.setVisibility(GONE);

            scanDevicesBtn.setVisibility(GONE);
            turnBluetooth.setChecked(false);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void startSearchingDevices() {
        checkConnectPermission();
        scanDevicesList.setAdapter(null);
        dataModels.clear();

        bluetoothAdapter.startDiscovery();
        registerReceiver(bluetoothStatus, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(bluetoothStatus, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(bluetoothStatus, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void showPairedDevices() {
        checkConnectPermission();
        pairedDevicesList.setVisibility(VISIBLE);
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        String[] strings = new String[pairedDevices.size()];
        int index = 0;
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                strings[index] = device.getName();
                index++;
            }
            PairedDevicesList customBaseAdapter = new PairedDevicesList(getApplicationContext(), strings);
            pairedDevicesList.setVisibility(VISIBLE);
            pairedDevicesList.setAdapter(customBaseAdapter);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void enableDisableBT() {
        checkConnectPermission();
        if (bluetoothAdapter == null) {
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
        }
        if (!bluetoothAdapter.isEnabled()) {
            Log.i("MESS", "JEST GIT");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(bluetoothStatus, BTIntent);
        }
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();

            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(bluetoothStatus, filter);
        }
    }

    private final BroadcastReceiver bluetoothStatus = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.S)
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();         //String variable, which store actual intent status
            Log.i("INFO", action);

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, bluetoothAdapter.ERROR); //Retrieve extended data from the intent or received
                // error value. BluetoothAdapter.EXTRA_STATE, get state bluetoothAdapter

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");

                        //Set bluetooth switch in off position
                        turnBluetooth.setChecked(false);
                        scanDevicesBtn.setVisibility(GONE);

                        availableDevicesTxt.setVisibility(GONE);
                        deviceNameTxt.setVisibility(GONE);
                        MACAddressTxt.setVisibility(GONE);
                        pairedDeviceTxt.setVisibility(GONE);

                        //reset scanDevices adapter and delete all devices save on list
                        scanDevicesList.setAdapter(null);
                        dataModels.clear();
                        scanDevicesList.setVisibility(GONE);    //Gone list of scanDevices

                        pairedDevicesList.setVisibility(GONE);  //Gone list of pairedDevices
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");

                        //Set bluetooth switch in on position
                        turnBluetooth.setChecked(true);
                        scanDevicesBtn.setVisibility(VISIBLE);

                        availableDevicesTxt.setVisibility(VISIBLE);
                        deviceNameTxt.setVisibility(VISIBLE);
                        MACAddressTxt.setVisibility(VISIBLE);
                        pairedDeviceTxt.setVisibility(VISIBLE);

                        //reset scanDevices adapter and delete all devices save on list
                        startSearchingDevices();
                        scanDevicesList.setVisibility(VISIBLE);    //Visible list of scanDevices
                        scanDevicesBtn.setEnabled(false);

                        showPairedDevices();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:

                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                int modeValue = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (modeValue) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "onReceive: The device is not in discoverable mode but can still receive connection");
                        break;

                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "onReceive: The device is in discoverable mode");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "onReceive: The device is not in discoverable mode and can not receive connection");
                        break;
                    default:
                        Log.d(TAG, "onReceive: Error");
                }
            }

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device != null) {
                    checkConnectPermission();
                }

                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
                dataModels.add(new Device(deviceName, deviceHardwareAddress));

                ScannedDevicesList adapter = new ScannedDevicesList(dataModels, getApplicationContext());
                scanDevicesList.setAdapter(adapter);
                scanDevicesList.setVisibility(VISIBLE);     //Set visible mode for scanDevicesList element to show near accesses devices

                Log.i("WIADOMOSC", device.getName() + "\n" + device.getAddress());

                scanDevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Device dataModel= dataModels.get(position);

                            Snackbar.make(view, dataModel.getName()+"\n"+dataModel.getAddressMAC(), Snackbar.LENGTH_LONG)
                                .setAction("No action", null).show();
                            }
                });
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                scanDevicesBtn.setEnabled(true);
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void checkConnectPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
            Log.i("MES", "SUCCESS");
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.BLUETOOTH_CONNECT} ,2);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            Log.i("MES", "SUCCESS");
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION} ,2);

        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED) {
            Log.i("MES", "SUCCESS");
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.BLUETOOTH_SCAN} ,2);
        }
    }
}