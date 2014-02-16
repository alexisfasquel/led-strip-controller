package com.aleks.letstrip;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: Aleks
 * Date: 14/02/14
 * Time: 04:20
 * To change this template use File | Settings | File Templates.
 */


public class ArduinoBluetooth {

    private static final String DEVICE_NAME = "Napiz";
    private static final String TAG = "com.aleks.letstrip";
    private final static int REQUEST_ENABLE_BT = 1;

    private MainActivity mActivity;

    private BroadcastReceiver mReceiver;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBluetoothSocket;
    private BluetoothDevice mDevice = null;

    private OutputStream mOutStream;


    public ArduinoBluetooth(Activity activity) {

        mActivity = (MainActivity) activity;

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.i(TAG, "No bluetooth support...");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        //findOldOnes();
        findNewOnes();
    }


    public void onDestroy() {
        mActivity.unregisterReceiver(mReceiver);
        disconnect();
    }

    private void findNewOnes() {
        // Create a BroadcastReceiver for ACTION_FOUND
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.i(TAG, device.getName() + "\n" + device.getAddress());
                    if(device.getName() != null && device.getName().trim().equals(DEVICE_NAME)) {
                        mDevice = device;
                        connect();
                        mBluetoothAdapter.cancelDiscovery();
                        Log.i(TAG, "Closing discrovery");
                    }
                }
            }
        };

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mActivity.registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
        mBluetoothAdapter.startDiscovery();

    }


    private void findOldOnes() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                Log.i(TAG, device.getName() + "\n" + device.getAddress());

                if(device.getName().equals(DEVICE_NAME)) {
                    mDevice = device;
                }
            }
        }
    }

    public void disconnect() {
        try {
            mBluetoothSocket.close();
        } catch (IOException e) {}
    }

    public boolean connect() {
        if(mDevice == null) {
            return false;
        }
        try {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

            mBluetoothSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
            mBluetoothSocket.connect();

            Log.i(TAG, "Well well well");
            mOutStream = mBluetoothSocket.getOutputStream();
            if(mOutStream != null) {
                mActivity.enableControls(true);
                return true;
            }



        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return false;
    }

    public void send(int r, int g, int b) {
        final byte[] data = new byte[3];
        data[0] = (byte) ((byte)r & 0xFF);
        data[1] = (byte) ((byte)g & 0xFF);
        data[2] = (byte) ((byte)b & 0xFF);

        new Thread(new Runnable() {
            @Override
            public void run() {
                 System.out.println(data[0] + " " + data[1]);
                try {
                    mOutStream.write(data);
                } catch (IOException e) {}
            }
        }).start();
    }
}
