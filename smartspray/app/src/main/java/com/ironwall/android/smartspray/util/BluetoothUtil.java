package com.ironwall.android.smartspray.util;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;

import com.ironwall.android.smartspray.global.GlobalVariable;

/**
 * Created by KimJS on 2016-07-28.
 */
public class BluetoothUtil {

    private static BluetoothAdapter mBluetoothAdapter;

    public BluetoothUtil(Context context) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static int getConnectivityStatus(Context context) {
        if(mBluetoothAdapter == null) {
            new BluetoothUtil(context);
        }

        int result = 0;

        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enable
            }
            else
            {
                // Bluetooth is  enable
                result += GlobalVariable.TYPE_BLUETOOTH;
            }
        }

        return result;
    }
}
