package com.ironwall.android.smartspray.service;

import android.annotation.TargetApi;
import android.app.Notification;
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
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.ironwall.android.smartspray.activity.MainActivity;
import com.ironwall.android.smartspray.database.DBManager;
import com.ironwall.android.smartspray.dto.LogSms;
import com.ironwall.android.smartspray.dto.SosNumber;
import com.ironwall.android.smartspray.global.GlobalVariable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

/**
 * Created by KimJS on 2016-08-11.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class SprayService extends Service {

    private static final int STATE_DISCONNECT = 10;
    private static final int STATE_CONNECT = 20;

    private static final int NOTFOUND_MSG = 1;
    private static final int CONNECTED_MSG = 2;
    private static final int DISCONNECT_MSG = 3;
    private static final int BLUETOOTH_NOT_ON = 4;
    private static final int BLE_NOT_SUPPORT = 5;

    private int state;
    public static final String LOG_TAG = "SprayService##";
    private Context mContext;

    /* ----- 블루투스 제어용 변수들 ----- */
    private BluetoothLeScanner mLEScanner;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothManager mBluetoothManager;
    private BluetoothGattService mBluetoothGattService;
    private String devName;

    public final static UUID UUID_SERVICE = BluetoothHelper.sixteenBitUuid(0x2220);
    public final static UUID UUID_RECEIVE = BluetoothHelper.sixteenBitUuid(0x2221);
    public final static UUID UUID_CLIENT_CONFIGURATION = BluetoothHelper.sixteenBitUuid(0x2902);

    /* ----- 10초내로 철벽이를 잡지 못하면 -> 기기를 찾을 수 없습니다 ----- */
    private Handler toastHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case NOTFOUND_MSG :
                    Log.d(LOG_TAG, "NO Device Found");
                    Toast.makeText(SprayService.this, "No device found",
                            Toast.LENGTH_SHORT).show();

                    if (Build.VERSION.SDK_INT < 21) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        Log.d(LOG_TAG, "Stop Scanning");
                    } else {
                        mLEScanner.stopScan(mScanCallback);
                        Log.d(LOG_TAG, "Stop Scanning");
                    }

                    Log.d(LOG_TAG, "[in handler]thread interrupt");
                    connectThread.interrupt();
                    break;
                case CONNECTED_MSG :
                    Toast.makeText(SprayService.this, "connected to " + mBluetoothDevice.getName(),
                            Toast.LENGTH_SHORT).show();
                    break;
                case DISCONNECT_MSG :
                    Toast.makeText(SprayService.this, "device disconnected ",
                            Toast.LENGTH_SHORT).show();
                    break;
                case BLUETOOTH_NOT_ON :
                    Toast.makeText(SprayService.this, "Blutooth not enabled, Please Reconnect after turn on Bluetooth",
                            Toast.LENGTH_SHORT).show();
                    break;
                case BLE_NOT_SUPPORT :
                    Toast.makeText(SprayService.this, "BLE Not Supported",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private final Thread connectThread = new Thread(new Runnable() {
        @Override
        public void run() {
            Log.d(LOG_TAG, "[in thread]thread start");
            try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
            if(!connectThread.isInterrupted()) {
                if(state == STATE_DISCONNECT) {
                    toastHandler.sendEmptyMessage(NOTFOUND_MSG);
                }
                else {
                    Log.d(LOG_TAG, "[in handler]thread interrupt");
                    connectThread.interrupt();
                }
            }
        }
    });

    @Override
    public void onCreate() {

        setState(STATE_DISCONNECT);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            toastHandler.sendEmptyMessage(BLE_NOT_SUPPORT);
            stopSelf();
        }

        if (initialize()) {
            Log.i(LOG_TAG, "Initialize succeed");
            mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        } else {
            stopSelf();
        }

        //Receiver
        mContext = this;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startForeground(1, new Notification());
        Log.d(LOG_TAG, "Service Start");
        scanLeDevice();
        return super.onStartCommand(intent, flags, startId);
        //return START_STICKY;
    }

    @Override
    public void onDestroy() {

        Intent ringtonePlayingService = new Intent(mContext, RingtonePlayingService.class);
        stopService(ringtonePlayingService);
        disconnect();
        close();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //서비스가 시작할 때 스캔을 시작하는 부분
    public void scanLeDevice() {
        Log.d(LOG_TAG, "**scanLeDevice()");
        if(connectThread.isInterrupted()) {
            connectThread.start();
        }

        if (Build.VERSION.SDK_INT < 21) {
            mBluetoothAdapter.startLeScan(
                    new UUID[]{UUID_SERVICE},
                    mLeScanCallback);
        } else {
            mLEScanner.startScan(mScanCallback);
        }
    }

    // SDK >= 21 에서 작동하는 ScanCallback
    private ScanCallback mScanCallback = new ScanCallback()
    {
        @Override
        public void onScanResult(int callbackType, ScanResult result)
        {
            Log.d(LOG_TAG, "**onScanResult()");
            if(result == null) {
                Log.d(LOG_TAG, "no device found");
            } else {

                if (result.getDevice().getName().contains("IRONWALL")) {

                    mBluetoothDevice = result.getDevice();
                    devName = mBluetoothDevice.getName();

                    Log.i("mBluetoothAdapter 결과", mBluetoothAdapter.enable() ? "Enabling bluetooth..." : "Enable failed!");
                    Log.i("mBlueToothDevice 결과", mBluetoothDevice.getAddress());
                    if (connect(mBluetoothDevice.getAddress())) {
                        Log.d(LOG_TAG, "장비이름 : " + devName);
                    }

                    mLEScanner.stopScan(this);
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results)
        {
            Log.d(LOG_TAG, "**onBatchScanResults()");
            if(results == null) {
                Log.d(LOG_TAG, "no device found");
            } else {
                for (ScanResult sr : results)
                {
                    if(sr.getDevice().getName().contains("IRONWALL")) {

                        mBluetoothDevice = sr.getDevice();
                        devName = mBluetoothDevice.getName();

                        Log.d(LOG_TAG, "mBluetoothAdapter 결과: " + (mBluetoothAdapter.enable() ? "Enabling bluetooth..." : "Enable failed!"));
                        Log.d(LOG_TAG, "mBlueToothDevice 결과: " + mBluetoothDevice.getAddress());
                        if (connect(mBluetoothDevice.getAddress())) {
                            Log.d(LOG_TAG, "장비이름 : " + devName);
                        }

                        mLEScanner.stopScan(this);
                    }
                }
            }
        }
    };

    // SDK < 21 에서 작동하는 ScanCallback
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    Log.d(LOG_TAG, "**onLeScan()");
                    mBluetoothAdapter.stopLeScan(this);
                    mBluetoothDevice = device;
                    devName = device.getName();

                    Log.d(LOG_TAG, "mBluetoothAdapter 결과: " + (mBluetoothAdapter.enable() ? "Enabling bluetooth..." : "Enable failed!"));
                    Log.d(LOG_TAG, "mBlueToothDevice 결과: " + mBluetoothDevice.getAddress());
                    if (connect(mBluetoothDevice.getAddress())) {
                        Log.d(LOG_TAG, "장비이름 : " + devName);
                    }
                }
            };

    //블루투스 GattCallback의 선언 연결상태가 변하거나, 스프레이가 작동했을 때 값을 받아 처리하는 부분
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(LOG_TAG, "**onConnectionStateChange()");
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(LOG_TAG, "Connected to IRWONWALL");
                Log.d(LOG_TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

                toastHandler.sendEmptyMessage(CONNECTED_MSG);
                //TODO shared preference 로 바꾸기
                setState(STATE_CONNECT);

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(LOG_TAG, "Disconnected from IRWONWALL");
                setState(STATE_DISCONNECT);
                scanLeDevice();         // 연결이 끊겼을 경우 다시 연결을 시도한다
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.e(LOG_TAG, "---------------------------------------------- **onCharacteristicChanged ----------------------------------------------");
            //Log.d(LOG_TAG, "**onCharacteristicChanged");
            if (UUID_RECEIVE.equals(characteristic.getUuid())) {
                int getdata = ByteBuffer.wrap(characteristic.getValue()).order(ByteOrder.LITTLE_ENDIAN).getInt();
                Log.d(LOG_TAG, "data received in onCharacteristicChanged " + getdata);

                Intent broadcastsender = new Intent(GlobalVariable.BROADCASTER);

                switch (getdata) {
                    case GlobalVariable.IN_EMERGENCY:

                        broadcastsender.putExtra(GlobalVariable.emergency, getdata);
                        Intent intent = new Intent (SprayService.this, MainActivity.class);
                        //FLAG_ACTIVITY_NEW_TASK : 새로운 스택에 액티비티를 생성하나, 동일한 affinity 의 스택이 있는 경우 그곳에 액티비티 생성
                        //FLAG_ACTIVITY_SINGLE_TOP : 스택의 최상위 액티비티와 같은경우 생성하지 않는다.
                        //FLAG_ACTIVITY_CLEAR_TOP : 스택에서 해당 액티비티가 존재하는 경우 상위의 모든 액티비티를 pop(삭제) 하고 top 으로 만들어 준다.
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                        //Intent ringtonePlayingService = new Intent(mContext, RingtonePlayingService.class);
                        //startService(ringtonePlayingService);
                        break;
                    case GlobalVariable.IN_LOWBATTERY:
                        //broadcastsender.putExtra(GlobalVariable.lowbattery, getdata);
                        //Toast.makeText(mContext, "result recieved " + getdata, Toast.LENGTH_SHORT).show();
                        break;
                }
                Log.d(LOG_TAG, "sendBroadcast");
                //sendBroadcast(broadcastsender);
                onReceived();

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mBluetoothGattService = gatt.getService(UUID_SERVICE);
                if (mBluetoothGattService == null) {
                    Log.d(LOG_TAG, "IRWONWALL GATT service not found!");
                    return;
                }

                BluetoothGattCharacteristic receiveCharacteristic =
                        mBluetoothGattService.getCharacteristic(UUID_RECEIVE);
                if (receiveCharacteristic != null) {
                    BluetoothGattDescriptor receiveConfigDescriptor =
                            receiveCharacteristic.getDescriptor(UUID_CLIENT_CONFIGURATION);
                    Log.d(LOG_TAG, "BLE GATT Descriptor recieved");
                    if (receiveConfigDescriptor != null) {
                        gatt.setCharacteristicNotification(receiveCharacteristic, true);

                        receiveConfigDescriptor.setValue(
                                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(receiveConfigDescriptor);
                        Log.d(LOG_TAG, "BLE GATT Descriptor set");
                    } else {
                        Log.d(LOG_TAG, "IRWONWALL receive config descriptor not found");
                    }

                } else {
                    Log.d(LOG_TAG, "IRWONWALL receive characteristic not found");
                }
            } else {
                Log.d(LOG_TAG, "onServicesDiscovered received: " + status);
            }
        }
    };

    private void onReceived() {
        if(GlobalVariable.IS_DEBUG_MODE) {
            Log.d(LOG_TAG, "in onReceived");
        }
        Intent ringtonePlayingService = new Intent(mContext, RingtonePlayingService.class);
        startService(ringtonePlayingService);

        //Telephone
        ArrayList<SosNumber> telnos = DBManager.getManager(mContext).getAllSosNumber();

        double lat = GlobalVariable.nowloc.getLatitude();
        double lng = GlobalVariable.nowloc.getLongitude();
        String uri = "https://www.google.co.kr/maps/place/"+lat +","+lng;
        String loc = "위치: " + Uri.parse(uri).toString();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String name = sharedPref.getString("pref_my_name", "");
        String txt = name + " 님이 위급상황에 처했습니다.";

        Calendar calendar = Calendar.getInstance();
        String time = calendar.getTime().toString();

        try {
            for(SosNumber sn : telnos) {
                LogSms ls = new LogSms();
                ls.group_key = time;
                ls.name = sn.name;
                ls.number = sn.number;
                ls.latitude = GlobalVariable.nowloc.getLatitudeE6();
                ls.longitude = GlobalVariable.nowloc.getLongitudeE6();
                ls.message = uri;
                ls.result = "NO";
                SmsManager smsManager = SmsManager.getDefault();

                smsManager.sendTextMessage(sn.number, null, loc, null, null);
                smsManager.sendTextMessage(sn.number, null, txt, null, null);

                if(GlobalVariable.IS_DEBUG_MODE) {
                    Log.d(LOG_TAG, txt);
                }
                DBManager.setLogSms(ls);
            }
        } catch (Exception e) {

            e.printStackTrace();
        }


    }

    //처음 서비스가 만들어질 때 BLE에서 사용해야 하는 것들을 초기화 시켜준다.
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.d(LOG_TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.d(LOG_TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        if(!BluetoothAdapter.getDefaultAdapter().enable()) {
            toastHandler.sendEmptyMessage(BLUETOOTH_NOT_ON);
            return false;
        }

        return true;
    }

    //찾은 IRONWALL device에 GATT을 연결하는 과정
    public boolean connect(String address) {

        if (mBluetoothAdapter == null || address == null) {
            Log.d(LOG_TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(LOG_TAG, "Trying to use an existing mBluetoothGatt for connection.");
            return mBluetoothGatt.connect();
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, true, mGattCallback);
        Log.d(LOG_TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        return true;
    }

    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.d(LOG_TAG, "BluetoothAdapter not initialized");
            toastHandler.sendEmptyMessage(DISCONNECT_MSG);
            return;
        }
        mBluetoothGatt.disconnect();
        toastHandler.sendEmptyMessage(DISCONNECT_MSG);
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    private void setState(int newstate) {
        if(newstate == STATE_CONNECT ||  newstate == STATE_DISCONNECT) {
            state = newstate;
        }
    }


    //TODO test
    public Intent getRingtoneService() {
        return new Intent(mContext, RingtonePlayingService.class);
    }
}