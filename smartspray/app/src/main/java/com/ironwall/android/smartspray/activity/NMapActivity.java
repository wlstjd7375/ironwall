package com.ironwall.android.smartspray.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.ironwall.android.smartspray.R;
import com.ironwall.android.smartspray.api.nmap.NMapFragment;
import com.ironwall.android.smartspray.api.nmap.NMapManager;
import com.ironwall.android.smartspray.dto.PoliceStation;
import com.ironwall.android.smartspray.global.GlobalVariable;
import com.nhn.android.maps.NMapContext;
import com.nhn.android.maps.NMapView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class NMapActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MainActivity";

    private NMapView mMapView;
    private NMapManager mMapManager;    //my class

    private ImageView ivPolice;
    private ImageView ivDanger;
    private ImageView ivMyLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nmap);

        mMapView = (NMapView)findViewById(R.id.mapView);
        mMapManager = new NMapManager(this, mMapView);

        initNMap();
        initButton();
    }

    @Override
    protected void onStart() {
        super.onStart();
        settingNMap();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ArrayList<PoliceStation> mDataList = setTestData();
        mMapManager.setPolicePOIdataOverlay(mDataList);
    }


    public void initNMap() {
        mMapManager.setClientId(GlobalVariable.CLIENT_ID);
        mMapManager.init(getNMapContext());
    }

    public void settingNMap() {
        mMapManager.setListener();
        mMapManager.setFunction();
        mMapManager.setDefault();

        mMapManager.startMyLocation();

    }

    public void initButton() {
        ivPolice = (ImageView)findViewById(R.id.ivPolice);
        ivDanger = (ImageView)findViewById(R.id.ivDanger);
        ivMyLocation = (ImageView)findViewById(R.id.ivMyLocation);

        ivPolice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO get real data from DB
                ArrayList<PoliceStation> mDataList = setTestData();
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

    public NMapContext getNMapContext() {
        NMapFragment mMapFragment = (NMapFragment) getFragmentManager().findFragmentById(R.id.nmap_fragment);
        return mMapFragment.getNMapContext();
    }

    private ArrayList<PoliceStation> setTestData() {
        ArrayList<PoliceStation> mDataList = new ArrayList<PoliceStation>();
        PoliceStation ps = new PoliceStation();
        ps.longitude = 127.05355883;
        ps.latitude = 37.58356171;
        ps.name = "파출소1";
        ps.number = "02-123-1234";
        mDataList.add(ps);

        ps = new PoliceStation();
        ps.longitude = 127.0537734;
        ps.latitude = 37.58590829;
        ps.name = "파출소2";
        ps.number = "02-321-4321";
        mDataList.add(ps);

        return mDataList;
    }
}
