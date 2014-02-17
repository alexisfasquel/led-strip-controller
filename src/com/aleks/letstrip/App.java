package com.aleks.letstrip;

import android.app.Application;
import com.aleks.letstrip.Arduino.ArduinoUsb;
import com.aleks.letstrip.Arduino.BluetoothService;

public class App extends Application {

    public BluetoothService mBlutoothService;
    public ArduinoUsb mUsbService;

}
