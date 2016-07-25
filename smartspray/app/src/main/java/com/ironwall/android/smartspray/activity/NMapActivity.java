package com.ironwall.android.smartspray.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewTreeObserver;

import com.ironwall.android.smartspray.R;
import com.ironwall.android.smartspray.api.nmap.NMapFragment;
import com.ironwall.android.smartspray.api.nmap.NMapManager;
import com.ironwall.android.smartspray.global.GlobalVariable;
import com.nhn.android.maps.NMapContext;
import com.nhn.android.maps.NMapView;

public class NMapActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MainActivity";

    private NMapView mMapView;
    private NMapManager mMapManager;    //my class

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nmap);

        mMapView = (NMapView)findViewById(R.id.mapView);
        mMapManager = new NMapManager(this, mMapView);

        NMapInit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        NMapSetting();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void NMapInit() {
        mMapManager.setClientId(GlobalVariable.CLIENT_ID);
        mMapManager.init(getNMapContext());
    }

    public void NMapSetting() {
        mMapManager.setListener();
        mMapManager.setFunction();
        mMapManager.setDefault();

        mMapManager.startMyLocation();
        double test_longitude = 127.05355883;
        double test_latitude = 37.58356171;
        mMapManager.setPOIdataOverlay(test_longitude, test_latitude);
    }


    public NMapContext getNMapContext() {
        NMapFragment mMapFragment = (NMapFragment) getFragmentManager().findFragmentById(R.id.nmap_fragment);
        return mMapFragment.getNMapContext();
    }
}
