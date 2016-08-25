package com.ironwall.android.smartspray.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.ironwall.android.smartspray.R;
import com.ironwall.android.smartspray.api.nmap.NMapFragment;
import com.ironwall.android.smartspray.api.nmap.NMapManager;
import com.ironwall.android.smartspray.database.DBManager;
import com.ironwall.android.smartspray.dto.PoliceStation;
import com.ironwall.android.smartspray.global.GlobalVariable;
import com.nhn.android.maps.NMapContext;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;

import java.util.ArrayList;

public class NMapActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MainActivity";

    private NMapView mMapView;
    private NMapManager mMapManager;    //my class

    private ImageView ivPolice;
    private ImageView ivDanger;
    private ImageView ivMyLocation;

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private boolean has_permission = false;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set Status bar color
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Color.parseColor("#222222"));
        }
        setContentView(R.layout.activity_nmap);
        mContext = this;

        mMapView = (NMapView)findViewById(R.id.mapView);
        mMapManager = new NMapManager(this, mMapView);

        initNMap();
        initButton();

        checkPermission();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(has_permission) {
            settingNMap();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(has_permission) {
            //ArrayList<PoliceStation> mDataList = setTestData();
            //ArrayList<PoliceStation> mDataList = getPoliceStation();
            //mMapManager.setPolicePOIdataOverlay(mDataList);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mMapManager != null)
        {
            mMapManager = null;
        }
    }

    private void initNMap() {
        mMapManager.setClientId(GlobalVariable.CLIENT_ID);
        mMapManager.init(getNMapContext());
    }

    private void initButton() {
        ivPolice = (ImageView)findViewById(R.id.ivPolice);
        ivDanger = (ImageView)findViewById(R.id.ivDanger);
        ivMyLocation = (ImageView)findViewById(R.id.ivMyLocation);

        ivPolice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get real data from DB
                ArrayList<PoliceStation> mDataList = getPoliceStation();
                mMapManager.setPolicePOIdataOverlay(mDataList);
            }
        });

        ivDanger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        ivMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //## 내 위치 on / off
                //mMapManager.startMyLocation();

                //## 내 위치로 화면 이동
                mMapManager.moveToMyLocation();
            }
        });
    }

    private void checkPermission () {
        // gps 사용 권한 체크 (권한이 없는 경우 -1 리턴)
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //권한이 없는 경우
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //사용자가 임의로 권한을 취소시킨 경우
                //권한 재요청
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
            else {
                //최초로 권한을 요청하는 경우
                //권한 재요청
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        }
        else {
            //권한이 있는 경우
            has_permission = true;
        }
    }

    private void settingNMap() {
        mMapManager.setListener();
        mMapManager.setFunction();
        mMapManager.setDefault();

        mMapManager.startMyLocation();

    }

    public NMapContext getNMapContext() {
        NMapFragment mMapFragment = (NMapFragment) getFragmentManager().findFragmentById(R.id.nmap_fragment);
        return mMapFragment.getNMapContext();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // 권한에 대한 콜백
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //권한 동의 버튼 선택
                    has_permission = true;
                    //NMap 세팅
                    settingNMap();
                } else {
                    //권한 동의 안함 버튼 선택
                    //액티비티 종료
                    Toast.makeText(this, "권한사용을 동의해주셔야 이용이 가능합니다.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }

    private ArrayList<PoliceStation> getPoliceStation() {
        NGeoPoint p = mMapManager.getMyLocation();
        if(p != null) {
            int longitude = p.getLongitudeE6();
            int latitude = p.getLatitudeE6();
            int range_long = 30000; //0.03
            int range_lat = 20000;  //0.02

            ArrayList<PoliceStation> mDataList = DBManager.getManager(mContext).getPoliceNearby(longitude, latitude, range_long, range_lat);
            return mDataList;
        }
        else {
            return null;
        }
    }

    private ArrayList<PoliceStation> setTestData() {
        ArrayList<PoliceStation> mDataList = new ArrayList<PoliceStation>();
        PoliceStation ps = new PoliceStation();
        //ps.longitude = 127.05355883;
        //ps.latitude = 37.58356171;
        ps.longitude = 127053558;
        ps.latitude = 37583561;
        ps.name = "파출소1";
        ps.tel = "02-123-1234";
        mDataList.add(ps);

        ps = new PoliceStation();
        //ps.longitude = 127.0537734;
        //ps.latitude = 37.58590829;
        ps.longitude = 127053773;
        ps.latitude = 37585908;
        ps.name = "파출소2";
        ps.tel = "02-321-4321";
        mDataList.add(ps);

        return mDataList;
    }
}
