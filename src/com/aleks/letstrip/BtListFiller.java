package com.aleks.letstrip;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.ListView;
import com.aleks.letstrip.Arduino.BluetoothService;
import com.aleks.letstrip.Arduino.DiscoveryListener;

/**
 * Created with IntelliJ IDEA.
 * User: Aleks
 * Date: 16/02/14
 * Time: 20:46
 * To change this template use File | Settings | File Templates.
 */
public class BtListFiller extends AsyncTask<Void, String, Void>{

    private BtListAdapter mAdapter;

    private Activity mActivity;

    private BluetoothService mService;

    public BtListFiller(Activity activity, ListView listView, BluetoothService service) {
        mActivity = activity;
        mService = service;
        mAdapter = new BtListAdapter(activity);

        listView.setAdapter(mAdapter);
    }

    @Override
    protected Void doInBackground(Void... voids ) {
       mService.startDiscovery(new DiscoveryListener() {
           @Override
           public void onNewDeviceDiscovered(String deviceName) {
               publishProgress(deviceName);
           }
       });
       return null;
    }

    @Override
    protected void onProgressUpdate(final String... values) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.add(values[0]);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onCancelled() {
        mService.stopDiscovery();
    }
}
