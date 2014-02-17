package com.aleks.letstrip.Arduino;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.aleks.letstrip.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class BluetoothService {

    private static final String DEVICE_NAME = "Napiz";
    private static final String TAG = "com.aleks.letstrip";
    private final static int REQUEST_ENABLE_BT = 1;

    private MainActivity mActivity;

    private ConnectionListener mListener;
    private BroadcastReceiver mReceiver;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBluetoothSocket;

    private BluetoothDevice mBluetoothDevice;
    private OutputStream mOutStream;

    Map<String, BluetoothDevice> mDevices = new HashMap<String, BluetoothDevice>();


    public BluetoothService(Activity activity, ConnectionListener listener) {

        mActivity = (MainActivity) activity;
        mListener = listener;

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.i(TAG, "No bluetooth support...");
        }
        //If bluetooth inactive, then attempting to start it
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private String[] getPairedDevices() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device :  pairedDevices) {
                String name = device.getName();
                if(name != null) {
                    mDevices.put(name, device);
                }
                Log.i(TAG, "\n" + device.getName() + "\n" + device.getAddress() + "\n " + device.getType());
            }
        }
        return null;
    }

    public boolean startDiscovery(final DiscoveryListener listener) {
        if(mReceiver != null) {
            return false;
        }
        // Create a BroadcastReceiver for ACTION_FOUND
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String name = device.getName();
                    if(name != null) {
                        if(mBluetoothDevice == null || !name.equals(mBluetoothDevice.getName())) {
                            listener.onNewDeviceDiscovered(device.getName());
                            mDevices.put(name, device);
                        }
                    }
                    Log.i(TAG, "\n" + device.getName() + "\n" + device.getAddress() + "\n " + device.getType());
                }
            }
        };
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mActivity.registerReceiver(mReceiver, filter);
        mBluetoothAdapter.startDiscovery();
        return true;
    }

    public void stopDiscovery() {
        mBluetoothAdapter.cancelDiscovery();
        mActivity.unregisterReceiver(mReceiver);
        mReceiver = null;
    }

    public boolean connect(String name) {
        if(mBluetoothDevice != null) {
            return false;
        }
        mBluetoothDevice = mDevices.get(name);
        try {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            mBluetoothSocket.connect();

            mOutStream = mBluetoothSocket.getOutputStream();
            final InputStream inputStream = mBluetoothSocket.getInputStream();
            if(mOutStream != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while (true) {
                                if(inputStream.available() > 0 );
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {}
                            }
                        } catch (IOException e) {
                            mListener.onDisconnected();
                        }
                    }
                });
                return true;
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return false;
    }

    public String getConnectedDeviceName() {
        return mBluetoothDevice.getName();
    }

    public void disconnect() {
        try {
            mBluetoothDevice = null;
            mBluetoothSocket.close();
        } catch (Exception e) {}
    }

    public void send (int r, int g, int b) throws DisconnectedException {
        final byte[] data = new byte[3];
        data[0] = (byte) ((byte)r & 0xFF);
        data[1] = (byte) ((byte)g & 0xFF);
        data[2] = (byte) ((byte)b & 0xFF);

        if(mBluetoothDevice == null) {
            throw new DisconnectedException();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mOutStream.write(data);
                } catch (IOException e) {
                    disconnect();
                    mListener.onDisconnected();
                }
            }
        }).start();
    }
}
