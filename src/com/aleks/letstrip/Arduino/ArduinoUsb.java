package com.aleks.letstrip.Arduino;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.*;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;


public class ArduinoUsb {

    private static final int ARDUINO_USB_VENDOR_ID = 0x2341;
    private static final int ARDUINO_UNO_R3_USB_PRODUCT_ID = 0x43;
    private static final String TAG = "com.aleks.letstrip";

    private Activity mActivity;

    private volatile UsbDevice mUsbDevice = null;
    private volatile UsbDeviceConnection mUsbConnection = null;
    private volatile UsbEndpoint mInUsbEndpoint = null;
    private volatile UsbEndpoint mOutUsbEndpoint = null;

    private ConnectionListener mListener;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mUsbDevice = null;
            mListener.onDisconnected();
        }
    };;


    public ArduinoUsb(Activity activity, ConnectionListener listener) {
        mActivity = activity;
        mListener = listener;

        mActivity.registerReceiver(mReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));
        findDevice();
    }

    //Need to be called on the NewIntent of the activity
    public void onNewIntent(Intent intent) {
        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.contains(intent.getAction())) {
            if(findDevice(intent)) {
                mListener.onConnected();
            }
        }
    }

    //Need to be called on the onDestroy function of the activity
    public void onDestroy() {
        if(mReceiver != null) {
            mActivity.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    public void send(int r, int g, int b) throws DisconnectedException {
        final byte[] data = new byte[3];
        data[0] = (byte) ((byte)r & 0xFF);
        data[1] = (byte) ((byte)g & 0xFF);
        data[2] = (byte) ((byte)b & 0xFF);

        if(mUsbDevice == null) {
            throw new DisconnectedException();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                mUsbConnection.bulkTransfer(mOutUsbEndpoint, data, data.length, 0);
            }
        }).start();
    }

    private boolean findDevice() {

        UsbManager usbManager = (UsbManager) mActivity.getSystemService(Context.USB_SERVICE);
        UsbDevice usbDevice = null;
        HashMap<String, UsbDevice> usbDeviceList = usbManager.getDeviceList();

        Iterator<UsbDevice> deviceIterator = usbDeviceList.values().iterator();
        if (deviceIterator.hasNext()) {
            usbDevice = deviceIterator.next();


            if (usbDevice.getVendorId() == ARDUINO_USB_VENDOR_ID &&
                    usbDevice.getProductId() == ARDUINO_UNO_R3_USB_PRODUCT_ID) {

                Log.i(TAG, "Arduino uno found!");
                mUsbDevice = usbDevice;
                return initDevice();

            } else {
                Log.e(TAG,"No Arduino uno found!");
                return false;
            }
        } else {
            Log.e(TAG,"No Arduino uno found!");
            return false;
        }
    }

    private boolean findDevice(Intent intent) {
        mUsbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if(mUsbDevice == null) {
            return false;
        } else {
            return initDevice();
        }

    }

    private byte[] getLineEncoding(int baudRate) {
        final byte[] lineEncodingRequest = { (byte) 0x80, 0x25, 0x00, 0x00, 0x00, 0x00, 0x08 };
        switch (baudRate) {
            case 14400:
                lineEncodingRequest[0] = 0x40;
                lineEncodingRequest[1] = 0x38;
                break;

            case 19200:
                lineEncodingRequest[0] = 0x00;
                lineEncodingRequest[1] = 0x4B;
                break;
        }

        return lineEncodingRequest;
    }

    private boolean initDevice() {
        UsbManager usbManager = (UsbManager) mActivity.getSystemService(Context.USB_SERVICE);
        mUsbConnection = usbManager.openDevice(mUsbDevice);
        if (mUsbConnection == null) {
            return false;
        }
        UsbInterface usbInterface = mUsbDevice.getInterface(1);
        if (!mUsbConnection.claimInterface(usbInterface, true)) {
            return false;
        }

        // Arduino USB serial converter setup
        // Set control line state
        mUsbConnection.controlTransfer(0x21, 0x22, 0, 0, null, 0, 0);
        // Set line encoding.
        mUsbConnection.controlTransfer(0x21, 0x20, 0, 0, getLineEncoding(9600), 7, 0);

        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
            if (usbInterface.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (usbInterface.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN) {
                    mInUsbEndpoint = usbInterface.getEndpoint(i);
                } else if (usbInterface.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_OUT) {
                    mOutUsbEndpoint = usbInterface.getEndpoint(i);
                }
            }
        }

        if (mInUsbEndpoint == null) {
            mUsbConnection.close();
            Log.e(TAG, "Error while initializing IN connection");
            return false;
        }
        if (mOutUsbEndpoint == null) {
            mUsbConnection.close();
            Log.e(TAG, "Error while initializing OUT connection");
            return false;
        }
        //Now we're connected
        mListener.onConnected();
        return true;
    }
}
