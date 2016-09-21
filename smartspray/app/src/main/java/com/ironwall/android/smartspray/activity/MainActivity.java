package com.ironwall.android.smartspray.activity;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ironwall.android.smartspray.R;
import com.ironwall.android.smartspray.database.DBManager;
import com.ironwall.android.smartspray.global.GlobalVariable;
import com.ironwall.android.smartspray.service.SprayService;
import com.ironwall.android.smartspray.service.SpraySignalReceiver;
import com.ironwall.android.smartspray.util.BluetoothUtil;
import com.ironwall.android.smartspray.util.GpsUtil;
import com.ironwall.android.smartspray.util.NetworkUtil;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String LOG_TAG = "MainActivity##";
    private Context mContext;

    private LinearLayout llSos;
    private LinearLayout llSafenow;
    private LinearLayout llMap;
    private LinearLayout llSettings;

    //## Connection Status Check
    private LinearLayout llTotalStatus;
    private ImageView ivInternetStatus;
    private ImageView ivGPSStatus;
    private ImageView ivBluetoothStatus;
    private ImageView ivWIFIStatus;
    private ArrayList<ImageView> statusList;

    //## Refresh
    private ImageView ivRefresh;

    private TextView tvSosNumberCount;

    //## nav_header_main
    private NavigationView navigationView;
    private TextView tvUsername;

    //service
    private Intent serviceIntent;
    private static final int REQUEST_ENABLE_BT = 1;


    private SpraySignalReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;

        initNavigationDrawerActivity();
        initLayout();

        //Broadcase Receiver
        mReceiver = new SpraySignalReceiver();
        IntentFilter register = new IntentFilter();
        register.addAction(GlobalVariable.BROADCASTER);
        registerReceiver(mReceiver, register);
        startService();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void startService() {
        //Check permission

        int bluetoothPermissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        int smsPermissionCheck  = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);

        if(bluetoothPermissionCheck == PackageManager.PERMISSION_DENIED || smsPermissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.SEND_SMS},
                    REQUEST_ENABLE_BT);
        }

        //check if service is already started
        boolean flag = false;
        ActivityManager manager = (ActivityManager)MainActivity.this.getSystemService(Context.ACTIVITY_SERVICE);//use context received in broadcastreceiver
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {

            if (SprayService.class.getName().equals(service.service.getClassName())) {
                flag = true;
                break;
            }
        }
        if(flag == false)
        {
            serviceIntent = new Intent(mContext, SprayService.class);
            startService(serviceIntent);
            Log.d(LOG_TAG, "service start");
        }

        //TODO 서비스가 이미 켜져있으면, shared preference 로 현재 기기가 연결되어있는지 상태를 확인 후 연결되지 않았으면 서비스 종료후 다시 실행
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(this, "앱 실행을 위해서는 해당 권한을 반드시 허용해야 합니다.",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        int count = DBManager.getManager(mContext).getSosNumberCount();
        tvSosNumberCount.setText(count + "");

        //사용자 이름
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String name = sharedPref.getString("pref_my_name", "");
        String welcome = "Welcome";
        if(!name.equals("") && name != null)
        {
            tvUsername.setText(name);
        }

        refreshConnectionStatus();
    }

    private void initLayout() {
        llTotalStatus = (LinearLayout)findViewById(R.id.llTotalStatus);

        //## Layout Buttons
        llSafenow = (LinearLayout)findViewById(R.id.llSafenow);
        llSafenow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SafenowActivity.class);
                startActivity(intent);
            }
        });
        llSos = (LinearLayout)findViewById(R.id.llSos);
        llSos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SosActivity.class);
                startActivity(intent);
            }
        });
        llMap = (LinearLayout)findViewById(R.id.llMap);
        llMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, NMapActivity.class);
                startActivity(intent);
            }
        });
        llSettings = (LinearLayout)findViewById(R.id.llSettings);
        llSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SettingsActivity.class);
                startActivity(intent);
            }
        });

        //## Registered SOS number count
        tvSosNumberCount = (TextView)findViewById(R.id.tvSosNumberCount);

        //## Connection Status Check
        ivInternetStatus = (ImageView)findViewById(R.id.ivInternetStatus);
        ivGPSStatus = (ImageView)findViewById(R.id.ivGpsStatus);
        ivBluetoothStatus = (ImageView)findViewById(R.id.ivBluetoothStatus);
        ivWIFIStatus = (ImageView)findViewById(R.id.ivWIFIStatus);

        statusList = new ArrayList<ImageView>();
        statusList.add(ivInternetStatus);
        statusList.add(ivGPSStatus);
        statusList.add(ivBluetoothStatus);
        statusList.add(ivWIFIStatus);

        //## Refresh
        ivRefresh = (ImageView)findViewById(R.id.ivRefresh);
        ivRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshConnectionStatus();
            }
        });

        //##nav_header_view
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        tvUsername = (TextView) navigationView.getHeaderView(0).findViewById(R.id.tvUserName);
        tvUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initNavigationDrawerActivity() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void refreshConnectionStatus() {
        int result = 0;
        result += NetworkUtil.getConnectivityStatus(mContext);
        result += GpsUtil.getConnectivityStatus(mContext);
        result += BluetoothUtil.getConnectivityStatus(mContext);

        boolean isConnected = true;
        if(result == 7 || result == 15) {
            llTotalStatus.setBackgroundResource(R.drawable.main_status_con);
        }
        else {
            llTotalStatus.setBackgroundResource(R.drawable.main_status_dis);
        }

        for(int i = 0; i < 4; i ++) {
            int on = result & 1;
            if(on == 1) {
                statusList.get(i).setBackgroundResource(R.drawable.icon_success);
            } else {
                statusList.get(i).setBackgroundResource(R.drawable.icon_fail);
            }
            result = result >> 1;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_connect) {
            startService();
        } else if (id == R.id.nav_disconnect) {
            //check if service is already started
            /*
            boolean flag = false;
            ActivityManager manager = (ActivityManager)MainActivity.this.getSystemService(Context.ACTIVITY_SERVICE);//use context received in broadcastreceiver
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
            {

                if (SprayService.class.getName().equals(service.service.getClassName())) {
                    flag = true;
                    break;
                }
            }
            if(flag == true)
            {
                stopService(serviceIntent);
                Toast.makeText(mContext, "Disconnected Successfully", Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, "service disconnected");
            }*/
            Intent serviceIntent = new Intent(mContext, SprayService.class);
            stopService(serviceIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
