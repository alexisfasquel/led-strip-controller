package com.aleks.letstrip;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import com.aleks.letstrip.Arduino.ArduinoUsb;
import com.aleks.letstrip.Arduino.BluetoothService;
import com.aleks.letstrip.Arduino.ConnectionListener;
import com.aleks.letstrip.Arduino.DisconnectedException;

public class MainActivity extends Activity {

    private static final int REQUEST_CODE = 1;

    private static final int RED_COLOR  = 0;
    private static final int GREEN_COLOR  = 1;
    private static final int BLUE_COLOR  = 2;

    private int[] colors = new int[] {127, 127, 127};

    private ArduinoUsb mAdrduinoUsb;
    private BluetoothService mAdrduinoBlutooth;

    private boolean mIsUsbConnected = false;
    private boolean mIsBtConnected = false;

    private TextView mToaster;
    private SeekBar[] mSeekBars;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToaster = (TextView) findViewById(R.id.toaster_home);

        final SeekBar sk_red = (SeekBar) findViewById(R.id.red_slider);
        final SeekBar sk_green= (SeekBar) findViewById(R.id.green_slider);
        final SeekBar sk_blue = (SeekBar) findViewById(R.id.blue_slider);

        sk_red.setOnSeekBarChangeListener(new ColorListener(RED_COLOR));
        sk_green.setOnSeekBarChangeListener(new ColorListener(GREEN_COLOR));
        sk_blue.setOnSeekBarChangeListener(new ColorListener(BLUE_COLOR));

        mSeekBars = new SeekBar[]{sk_red, sk_green, sk_blue };
        setConnected(false, false);

        final Activity activity = this;
        findViewById(R.id.main_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mIsBtConnected) {



                    ///YOURE THE ONE WHO"S FUCKED UP

                    //mAdrduinoBlutooth.disconnect();
                    setConnected(false, true);
                } else {
                    Intent intent = new Intent(activity, ScanActivity.class);
                    startActivityForResult(intent, REQUEST_CODE);
                }
            }
        });


        mAdrduinoBlutooth = new BluetoothService(this, new ConnectionListener() {
            @Override
            public void onConnected() {}    //Connection handled by the result of ScanActivity

            @Override
            public void onDisconnected() {
                setConnected(false, true);
            }
        });
        mAdrduinoUsb = new ArduinoUsb(this, new ConnectionListener() {
            @Override
            public void onConnected() {
                setConnected(true, false);
            }

            @Override
            public void onDisconnected() {
                setConnected(false, false);
            }
        });

        App app = (App)getApplication();
        app.mBlutoothService = mAdrduinoBlutooth;
        app.mUsbService = mAdrduinoUsb;

    }

    private void setConnected(boolean connected, boolean bluetooth) {
        if(bluetooth) {
            mIsBtConnected = connected;
        } else {
            mIsUsbConnected = connected;
        }
        if(mIsBtConnected) {
            //findViewById(R.id.main_button).setBackground(getResources().getDrawable(R.drawable.button_disconnect_normal));
            mToaster.setText("Connected!");
            setColors();
            connected = true;
        } else if(mIsUsbConnected) {
            mToaster.setText("Connected!");
            setColors();
            connected = true;
        } else {
            //findViewById(R.id.main_button).setBackground(getResources().getDrawable(R.drawable.button_scan));
            mToaster.setText("Disconnected!");
            connected = false;
        }
        for (int i = RED_COLOR; i <= BLUE_COLOR; i ++) {
            mSeekBars[i].setEnabled(connected);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (REQUEST_CODE) : {
                if (resultCode == Activity.RESULT_OK) {
                    setConnected(true, true);
                }
                break;
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        mAdrduinoUsb.onNewIntent(intent);
        super.onNewIntent(intent);
    }


    @Override
    protected void onPause() {
        mAdrduinoBlutooth.disconnect();
        setConnected(false, true);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mAdrduinoUsb.onDestroy();
        super.onDestroy();

    }

    private void setColors() {
        try {
            //mAdrduinoUsb.send(colors[RED_COLOR], colors[GREEN_COLOR], colors[BLUE_COLOR]);
            mAdrduinoBlutooth.send(colors[RED_COLOR], colors[GREEN_COLOR], colors[BLUE_COLOR]);
        } catch (DisconnectedException e) {
            //We should not have miss something riot ?
            e.printStackTrace();
        }
    }

    private class ColorListener implements SeekBar.OnSeekBarChangeListener {

        private int mWhichColor;
        ColorListener(int whichColor) {
            mWhichColor = whichColor;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            colors[mWhichColor] = (int)(progress * 2.55f);
            setColors();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    }

}
