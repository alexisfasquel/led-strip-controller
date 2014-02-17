package com.aleks.letstrip;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.aleks.letstrip.Arduino.BluetoothService;

public class ScanActivity extends Activity {

    private ListView mClientList;
    private BtListFiller mFiller;

    private BluetoothService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mService = ((App)getApplication()).mBlutoothService;
        setContentView(R.layout.activity_scan);

        mClientList = (ListView) findViewById(R.id.bt_list_view);

        mClientList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final TextView toater = (TextView)findViewById(R.id.toaster_scan);
                toater.setText("Connecting");

                final TextView tv = (TextView)view.findViewById(R.id.device_name);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final ImageView loading = (ImageView) findViewById(R.id.loading_scan);

                        if(mService.connect(tv.getText().toString())) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Intent resultIntent = new Intent();
                                    setResult(Activity.RESULT_OK, resultIntent);
                                    finish();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loading.setImageDrawable(getResources().getDrawable(R.drawable.loading_4));
                                    toater.setText("Failed!");
                                    toater.setTextColor(R.color.red);
                                    mClientList.setEnabled(false);
                                }
                            });

                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {}

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loading.setImageDrawable(getResources().getDrawable(R.drawable.loading_animation));
                                    toater.setText("Scanning");
                                    toater.setTextColor(R.color.grey);
                                    mClientList.setEnabled(true);
                                }
                            });
                        }
                    }
                }).start();
            }
        });
        ImageButton back = (ImageButton) findViewById(R.id.back_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, resultIntent);
                finish();
            }
        });

        mFiller = new BtListFiller(this, mClientList, mService);
        mFiller.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mService.stopDiscovery();
    }
}
