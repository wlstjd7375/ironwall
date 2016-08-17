package com.ironwall.android.smartspray.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ironwall.android.smartspray.global.GlobalVariable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

/**
 * Created by KimJS on 2016-08-11.
 */
public class SprayService extends Service implements BluetoothAdapter.LeScanCallback{

    final private static int STATE_BLUETOOTH_OFF = 10;
    final private static int STATE_DISCONNECTED = 20;
    final private static int STATE_INITIALIZED = 30;
    final private static int STATE_CONNECTING = 40;
    final private static int STATE_CONNECTED = 50;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothManager mBluetoothManager;
    private BluetoothGattService mBluetoothGattService;
    private String devName;

    public static final String TAG = "SprayService##";

    public final static UUID UUID_SERVICE = BluetoothHelper.sixteenBitUuid(0x2220);
    public final static UUID UUID_RECEIVE = BluetoothHelper.sixteenBitUuid(0x2221);
    public final static UUID UUID_CLIENT_CONFIGURATION = BluetoothHelper.sixteenBitUuid(0x2902);

    private int flag;
    private int state;

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.d(TAG, "onLeScan");
        mBluetoothAdapter.stopLeScan(this);
        mBluetoothDevice = device;
        devName = device.getName();
        Log.d("mBluetoothAdapter 결과",mBluetoothAdapter.enable() ? "Enabling bluetooth..." : "Enable failed!");
        Log.d("mBlueToothDevice 결과",mBluetoothDevice.getAddress());
        if(connect(mBluetoothDevice.getAddress())) {
            Log.i(TAG,"장비이름 : "+ devName);
        }
    }

    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        if(initialize()) {
            Log.i(TAG, "초기화 성공");
            mBluetoothAdapter.startLeScan(
                    new UUID[]{UUID_SERVICE},
                    SprayService.this);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        close();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to IRWONWALL");
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                state = newState;

                try {Thread.sleep(3000);} catch (InterruptedException e) {e.printStackTrace();}

                mBluetoothAdapter.startLeScan(
                        new UUID[]{UUID_SERVICE},
                        SprayService.this);
                Log.i(TAG, "Disconnected from IRWONWALL");
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.i(TAG, "Gatt readed");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (UUID_RECEIVE.equals(characteristic.getUuid())) {
                    int getdata = ByteBuffer.wrap(characteristic.getValue()).order(ByteOrder.LITTLE_ENDIAN).getInt();
                    Log.i(TAG, "data received" + getdata);

                    Intent broadcastsender = new Intent(GlobalVariable.BROADCASTER);
                    switch(getdata) {
                        case GlobalVariable.IN_EMERGENCY :
                            broadcastsender.putExtra(GlobalVariable.emergency, getdata);
                            break;
                        case GlobalVariable.IN_LOWBATTERY :
                            broadcastsender.putExtra(GlobalVariable.lowbattery, getdata);
                            break;
                    }
                    sendBroadcast(broadcastsender);

                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.i(TAG, "Gatt changed");
            if (UUID_RECEIVE.equals(characteristic.getUuid())) {
                int getdata = ByteBuffer.wrap(characteristic.getValue()).order(ByteOrder.LITTLE_ENDIAN).getInt();
                Log.i(TAG, "data received" + getdata);

                Intent broadcastsender = new Intent(GlobalVariable.BROADCASTER);
                switch(getdata) {
                    case GlobalVariable.IN_EMERGENCY :
                        broadcastsender.putExtra(GlobalVariable.emergency, getdata);
                        break;
                    case GlobalVariable.IN_LOWBATTERY :
                        broadcastsender.putExtra(GlobalVariable.lowbattery, getdata);
                        break;
                }
                sendBroadcast(broadcastsender);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mBluetoothGattService = gatt.getService(UUID_SERVICE);
                if (mBluetoothGattService == null) {
                    Log.e(TAG, "IRWONWALL GATT service not found!");
                    return;
                }

                BluetoothGattCharacteristic receiveCharacteristic =
                        mBluetoothGattService.getCharacteristic(UUID_RECEIVE);
                if (receiveCharacteristic != null) {
                    BluetoothGattDescriptor receiveConfigDescriptor =
                            receiveCharacteristic.getDescriptor(UUID_CLIENT_CONFIGURATION);
                    if (receiveConfigDescriptor != null) {
                        gatt.setCharacteristicNotification(receiveCharacteristic, true);

                        receiveConfigDescriptor.setValue(
                                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(receiveConfigDescriptor);
                    } else {
                        Log.e(TAG, "IRWONWALL receive config descriptor not found");
                    }

                } else {
                    Log.e(TAG, "IRWONWALL receive characteristic not found");
                }

                //broadcastUpdate(ACTION_CONNECTED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }
    };

    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    public boolean connect(String address) {

        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            return mBluetoothGatt.connect();
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;

        return true;
    }

    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }
}
