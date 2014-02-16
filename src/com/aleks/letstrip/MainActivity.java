package com.aleks.letstrip;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final int RED_COLOR  = 0;
    private static final int GREEN_COLOR  = 1;
    private static final int BLUE_COLOR  = 2;

    private static final String TAG = "com.aleks.letstrip";

    private int[] colors = new int[]{0, 0, 0};

    private ArduinoUsb mAdrduinoUsb;
    private ArduinoBluetooth mAdrduinoBlutooth;

    private View mHeader;
    private TextView mToaster;
    private SeekBar[] mSeekBars;



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


    private void setColors() {
        //mHeader.setBackgroundColor(Color.rgb(colors[RED_COLOR], colors[GREEN_COLOR], colors[BLUE_COLOR]));
        //mAdrduinoUsb.send(colors[RED_COLOR], colors[GREEN_COLOR], colors[BLUE_COLOR]);
        //mAdrduinoBlutooth.send(colors[RED_COLOR], colors[GREEN_COLOR], colors[BLUE_COLOR]);
    }


    public void enableControls(boolean enable) {
        for (int i = RED_COLOR; i <= BLUE_COLOR; i ++) {
            mSeekBars[i].setEnabled(enable);
        }
    }


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mToaster = (TextView) findViewById(R.id.toaster);
        mHeader = findViewById(R.id.header);

        final SeekBar sk_red = (SeekBar) findViewById(R.id.red_slider);
        final SeekBar sk_green= (SeekBar) findViewById(R.id.green_slider);
        final SeekBar sk_blue = (SeekBar) findViewById(R.id.blue_slider);

        sk_red.setOnSeekBarChangeListener(new ColorListener(RED_COLOR));
        sk_green.setOnSeekBarChangeListener(new ColorListener(GREEN_COLOR));
        sk_blue.setOnSeekBarChangeListener(new ColorListener(BLUE_COLOR));

        mSeekBars = new SeekBar[]{sk_red, sk_green, sk_blue };
        enableControls(false);

        mAdrduinoUsb = new ArduinoUsb(this);
        mAdrduinoBlutooth = new ArduinoBluetooth(this);

        if(mAdrduinoUsb.findDevice() || mAdrduinoBlutooth.connect()) {
            mToaster.setText("ArduinoUsb Uno connected!");
            enableControls(true);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setColors();
                }
            }, 100);
        } else {
            mToaster.setText("ArduinoUsb Uno missing!");
        }
        registerReceiver(mReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));
        enableControls(true);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.contains(intent.getAction())) {
            if(mAdrduinoUsb.findDevice(intent)) {
                mToaster.setText("ArduinoUsb Uno connected!");
                enableControls(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setColors();
                    }
                }, 100);
            } else {
                mToaster.setText("ArduinoUsb Uno missing!");
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        mAdrduinoBlutooth.onDestroy();

    }


    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mToaster.setText("ArduinoUsb Uno missing!");
            enableControls(false);
        }
    };
}
