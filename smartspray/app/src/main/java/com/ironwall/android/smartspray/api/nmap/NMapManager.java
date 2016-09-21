package com.ironwall.android.smartspray.api.nmap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import com.ironwall.android.smartspray.dto.PoliceStation;
import com.ironwall.android.smartspray.global.GlobalVariable;
import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapCompassManager;
import com.nhn.android.maps.NMapContext;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapLocationManager;
import com.nhn.android.maps.NMapOverlay;
import com.nhn.android.maps.NMapOverlayItem;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.nmapmodel.NMapPlacemark;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.mapviewer.overlay.NMapCalloutCustomOverlay;
import com.nhn.android.mapviewer.overlay.NMapCalloutOverlay;
import com.nhn.android.mapviewer.overlay.NMapMyLocationOverlay;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;

import java.util.ArrayList;

/**
 * Created by KimJS on 2016-07-24.
 */
public class NMapManager {

    private static final String LOG_TAG = "NMapManager##";
    private static final boolean DEBUG = GlobalVariable.IS_DEBUG_MODE;
    private static final int NMAP_ZOOM_LEVEL_DEFAULT = 12; //## zoom level 이 커질수록 확대율도 커진다.

    private NMapView mMapView;
    private Context mContext;
    private NMapController mMapController;
    private NMapContext mMapActivityContext;

    //Overlay
    private NMapViewerResourceProvider mMapViewerResourceProvider;
    private NMapOverlayManager mOverlayManager;

    //My Location
    private NMapLocationManager mMapLocationManager;
    private NMapCompassManager mMapCompassManager;
    private NMapMyLocationOverlay mMyLocationOverlay;

    private NMapPOIitem mFloatingPOIitem;
    private NMapPOIdataOverlay mFloatingPOIdataOverlay;

    //## 주변의 경찰서 위치를 표시해주는 POI data 집합
    private NMapPOIdataOverlay mPolicePOIDataOverlay;
    //## 주변의 위험지역을 표시해주는 POI data 집합
    private NMapPOIdataOverlay mDangerZonePOIDataOverlay;

    //TODO test
    private NGeoPoint myCurrentPosition = new NGeoPoint();

    public NMapManager(Context _mContext, NMapView _mMapView) {
        mContext = _mContext;
        mMapView = _mMapView;
    }

    public void setClientId(String CLIENT_ID) {
        // set a registered Client Id for Open MapViewer Library
        mMapView.setClientId(CLIENT_ID);

        //TODO 예외처리
    }

    public void init(NMapContext _mMapActivityContext) {
        mMapActivityContext = _mMapActivityContext;

        // initialize map view
        mMapView.setClickable(true);
        mMapView.setEnabled(true);
        mMapView.setFocusable(true);
        mMapView.setFocusableInTouchMode(true);
        mMapView.requestFocus();
    }

    public void setListener() {
        // register listener for map state changes
        mMapView.setOnMapStateChangeListener(onMapViewStateChangeListener);
        mMapView.setOnMapViewTouchEventListener(onMapViewTouchEventListener);
        mMapView.setOnMapViewDelegate(onMapViewTouchDelegate);
    }

    public void setFunction() {
        // use map controller to zoom in/out, pan and set map center, zoom level etc.
        mMapController = mMapView.getMapController();

        // use built in zoom controls
        NMapView.LayoutParams lp = new NMapView.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, NMapView.LayoutParams.BOTTOM_RIGHT);
        mMapView.setBuiltInZoomControls(true, lp);
        mMapView.displayZoomControls(true);


        //Overlay
        // create resource provider
        mMapViewerResourceProvider = new NMapViewerResourceProvider(mContext);
        // set data provider listener
        mMapActivityContext.setMapDataProviderListener(onDataProviderListener);

        // create overlay manager
        mOverlayManager = new NMapOverlayManager(mContext, mMapView, mMapViewerResourceProvider);
        // register callout overlay listener to customize it.
        mOverlayManager.setOnCalloutOverlayListener(onCalloutOverlayListener);
        // register callout overlay view listener to customize it.
        mOverlayManager.setOnCalloutOverlayViewListener(onCalloutOverlayViewListener);


        // Set my Location
        // location manager
        mMapLocationManager = new NMapLocationManager(mContext);
        mMapLocationManager.setOnLocationChangeListener(onMyLocationChangeListener);
        // compass manager
        mMapCompassManager = new NMapCompassManager((Activity)mContext);
        // create my location overlay
        mMyLocationOverlay = mOverlayManager.createMyLocationOverlay(mMapLocationManager, mMapCompassManager);
    }

    public void setDefault() {
        mMapController.setZoomLevel(NMAP_ZOOM_LEVEL_DEFAULT);
    }

    /* MapView State Change Listener*/
    private final NMapView.OnMapStateChangeListener onMapViewStateChangeListener = new NMapView.OnMapStateChangeListener() {

        @Override
        public void onMapInitHandler(NMapView mapView, NMapError errorInfo) {

            if (errorInfo == null) { // success
                // restore map view state such as map center position and zoom level.
                //restoreInstanceState();

            } else { // fail
                Log.e(LOG_TAG, "onFailedToInitializeWithError: " + errorInfo.toString());

                Toast.makeText(mContext, errorInfo.toString(), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onAnimationStateChange(NMapView mapView, int animType, int animState) {
            if (DEBUG) {
                Log.i(LOG_TAG, "onAnimationStateChange: animType=" + animType + ", animState=" + animState);
            }
        }

        @Override
        public void onMapCenterChange(NMapView mapView, NGeoPoint center) {
            if (DEBUG) {
                Log.i(LOG_TAG, "onMapCenterChange: center=" + center.toString());
            }
        }

        @Override
        public void onZoomLevelChange(NMapView mapView, int level) {
            if (DEBUG) {
                Log.i(LOG_TAG, "onZoomLevelChange: level=" + level);
            }

            //TODO 지도 확대, 축소에 따라 근처의 POI를 재검색하여 근방의 결과만 보여준다. / 아니면 처음부터 좌표 다 불러놓고,,?
        }

        @Override
        public void onMapCenterChangeFine(NMapView mapView) {

        }
    };

    private final NMapView.OnMapViewTouchEventListener onMapViewTouchEventListener = new NMapView.OnMapViewTouchEventListener() {

        @Override
        public void onLongPress(NMapView mapView, MotionEvent ev) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLongPressCanceled(NMapView mapView) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSingleTapUp(NMapView mapView, MotionEvent ev) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onTouchDown(NMapView mapView, MotionEvent ev) {

        }

        @Override
        public void onScroll(NMapView mapView, MotionEvent e1, MotionEvent e2) {
        }

        @Override
        public void onTouchUp(NMapView mapView, MotionEvent ev) {
            // TODO Auto-generated method stub

        }

    };

    private final NMapView.OnMapViewDelegate onMapViewTouchDelegate = new NMapView.OnMapViewDelegate() {

        @Override
        public boolean isLocationTracking() {
            if (mMapLocationManager != null) {
                if (mMapLocationManager.isMyLocationEnabled()) {
                    return mMapLocationManager.isMyLocationFixed();
                }
            }
            return false;
        }
    };


    /* NMapDataProvider Listener */
    private final NMapActivity.OnDataProviderListener onDataProviderListener = new NMapActivity.OnDataProviderListener() {

        @Override
        public void onReverseGeocoderResponse(NMapPlacemark placeMark, NMapError errInfo) {

            if (DEBUG) {
                Log.i(LOG_TAG, "onReverseGeocoderResponse: placeMark="
                        + ((placeMark != null) ? placeMark.toString() : null));
            }

            if (errInfo != null) {
                Log.e(LOG_TAG, "Failed to findPlacemarkAtLocation: error=" + errInfo.toString());

                Toast.makeText(mContext, errInfo.toString(), Toast.LENGTH_LONG).show();
                return;
            }

            if (mFloatingPOIitem != null && mFloatingPOIdataOverlay != null) {
                mFloatingPOIdataOverlay.deselectFocusedPOIitem();

                if (placeMark != null) {
                    mFloatingPOIitem.setTitle(placeMark.toString());
                }
                mFloatingPOIdataOverlay.selectPOIitemBy(mFloatingPOIitem.getId(), false);
            }
        }

    };

    private final NMapOverlayManager.OnCalloutOverlayListener onCalloutOverlayListener = new NMapOverlayManager.OnCalloutOverlayListener() {

        @Override
        public NMapCalloutOverlay onCreateCalloutOverlay(NMapOverlay itemOverlay, NMapOverlayItem overlayItem,
                                                         Rect itemBounds) {

            // handle overlapped items
            if (itemOverlay instanceof NMapPOIdataOverlay) {
                NMapPOIdataOverlay poiDataOverlay = (NMapPOIdataOverlay)itemOverlay;

                // check if it is selected by touch event
                if (!poiDataOverlay.isFocusedBySelectItem()) {
                    int countOfOverlappedItems = 1;

                    NMapPOIdata poiData = poiDataOverlay.getPOIdata();
                    for (int i = 0; i < poiData.count(); i++) {
                        NMapPOIitem poiItem = poiData.getPOIitem(i);

                        // skip selected item
                        if (poiItem == overlayItem) {
                            continue;
                        }

                        // check if overlapped or not
                        if (Rect.intersects(poiItem.getBoundsInScreen(), overlayItem.getBoundsInScreen())) {
                            countOfOverlappedItems++;
                        }
                    }

                    if (countOfOverlappedItems > 1) {
                        String text = countOfOverlappedItems + " overlapped items for " + overlayItem.getTitle();
                        Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
                        return null;
                    }
                }
            }

            // use custom old callout overlay
            if (overlayItem instanceof NMapPOIitem) {
                NMapPOIitem poiItem = (NMapPOIitem)overlayItem;

                if (poiItem.showRightButton()) {
                    return new NMapCalloutCustomOldOverlay(itemOverlay, overlayItem, itemBounds,
                            mMapViewerResourceProvider);
                }
            }

            // use custom callout overlay
            return new NMapCalloutCustomOverlay(itemOverlay, overlayItem, itemBounds, mMapViewerResourceProvider);

            // set basic callout overlay
            //return new NMapCalloutBasicOverlay(itemOverlay, overlayItem, itemBounds);
        }

    };

    private final NMapOverlayManager.OnCalloutOverlayViewListener onCalloutOverlayViewListener = new NMapOverlayManager.OnCalloutOverlayViewListener() {

        @Override
        public View onCreateCalloutOverlayView(NMapOverlay itemOverlay, NMapOverlayItem overlayItem, Rect itemBounds) {

            if (overlayItem != null) {
                // [TEST] 말풍선 오버레이를 뷰로 설정함
                String title = overlayItem.getTitle();
                if (title != null && title.length() > 5) {
                    return new NMapCalloutCustomOverlayView(mContext, itemOverlay, overlayItem, itemBounds);
                }
            }

            // null을 반환하면 말풍선 오버레이를 표시하지 않음
            return null;
        }
    };

    /* MyLocation Listener */
    private final NMapLocationManager.OnLocationChangeListener onMyLocationChangeListener = new NMapLocationManager.OnLocationChangeListener() {

        @Override
        public boolean onLocationChanged(NMapLocationManager locationManager, NGeoPoint myLocation) {
            // 현재 위치 변경시 호출, myLocation 객체에 변경된 좌표가 전달
            if (mMapController != null) {
                mMapController.animateTo(myLocation);
            }
            myCurrentPosition = myLocation;
            GlobalVariable.nowloc = myLocation;
            if(DEBUG) {
                Log.d(LOG_TAG, myLocation.getLatitude() +", " + myLocation.getLongitude());
            }
            return true;
        }

        @Override
        public void onLocationUpdateTimeout(NMapLocationManager locationManager) {

            // stop location updating
            //			Runnable runnable = new Runnable() {
            //				public void run() {
            //					stopMyLocation();
            //				}
            //			};
            //			runnable.run();
            Toast.makeText(mContext, "Your current location is temporarily unavailable.", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onLocationUnavailableArea(NMapLocationManager locationManager, NGeoPoint myLocation) {

            Toast.makeText(mContext, "Your current location is unavailable area.", Toast.LENGTH_LONG).show();

            stopMyLocation();
        }

    };

    /* ############### Action ############### */
    public void startMyLocation() {

        if (mMyLocationOverlay != null) {
            if (!mOverlayManager.hasOverlay(mMyLocationOverlay)) {
                mOverlayManager.addOverlay(mMyLocationOverlay);
            }

            if (mMapLocationManager.isMyLocationEnabled()) {
/*
                if (!mMapView.isAutoRotateEnabled()) {
                    mMyLocationOverlay.setCompassHeadingVisible(true);

                    mMapCompassManager.enableCompass();

                    mMapView.setAutoRotateEnabled(true, false);

                    //mMapContainerView.requestLayout();
                } else {
                    stopMyLocation();
                }*/

                stopMyLocation();
                mMapView.postInvalidate();
            } else {
                boolean isMyLocationEnabled = mMapLocationManager.enableMyLocation(true);
                if (!isMyLocationEnabled) {
                    Toast.makeText(mContext, "gps를 활성화 시켜서 내 위치를 확인하세요.",
                            Toast.LENGTH_LONG).show();
                    Intent goToSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    mContext.startActivity(goToSettings);

                    //## 활성화 시키지 않고 뒤로가기 버튼을 누르면 계속 gps 활성화 창이 뜬다.
                    //## 뒤로가기를 눌렀을 때 메인 화면으로 넘어가도록 설정
                    ((Activity)mContext).finish();
                    return;
                }
                //## 내 위치 표시
                mMyLocationOverlay.setHidden(false);
            }
        }
    }

    public void stopMyLocation() {
        if (mMyLocationOverlay != null) {
            mMapLocationManager.disableMyLocation();
            //## 내 위치 표시 해제
            mMyLocationOverlay.setHidden(true);

            /*
            if (mMapView.isAutoRotateEnabled()) {
                mMyLocationOverlay.setCompassHeadingVisible(false);

                mMapCompassManager.disableCompass();

                mMapView.setAutoRotateEnabled(false, false);

                //mMapContainerView.requestLayout();
            }*/
        }
    }

    // ## 내 위치로 화면 이동
    public void moveToMyLocation() {
        if (mMyLocationOverlay != null) {
            if (!mOverlayManager.hasOverlay(mMyLocationOverlay)) {
                mOverlayManager.addOverlay(mMyLocationOverlay);
            }


            boolean isMyLocationEnabled = mMapLocationManager.enableMyLocation(true);
            if (!isMyLocationEnabled) {
                Toast.makeText(mContext, "gps를 활성화 시켜서 내 위치를 확인하세요.",
                        Toast.LENGTH_LONG).show();
                Intent goToSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(goToSettings);
                ((Activity)mContext).finish();
                return;
            }
        }

    }


    //  NGeoPoint(double longitude, double latitude) point : (경도, 위도)
    //
    public void setPolicePOIdataOverlay(ArrayList<PoliceStation> mDataList) {

        if(mDataList == null) {
            return;
        }

        //## 이미 주위 경창서 위치가 검색되어 있으면
        if(mPolicePOIDataOverlay != null && mOverlayManager.hasOverlay(mPolicePOIDataOverlay)) {
            //## 위치 삭제
            mOverlayManager.removeOverlay(mPolicePOIDataOverlay);
            mPolicePOIDataOverlay = null;

            //## 화면 갱신
            mMapView.postInvalidate();
            return;
        }

        // Markers for POI item
        int markerId = NMapPOIflagType.PIN;

        int size = mDataList.size();
        double longitude;
        double latitude;
        String policeName;
        String policeNumber;

        // set POI data
        NMapPOIdata poiData = new NMapPOIdata(size, mMapViewerResourceProvider);
        poiData.beginPOIdata(size);
        for(PoliceStation ps : mDataList) {
            longitude = NGeoPoint.toLongitude(ps.longitude);
            latitude = NGeoPoint.toLatitude(ps.latitude);

            policeName = ps.name;
            policeNumber = ps.tel;

            NMapPOIitem item = poiData.addPOIitem(longitude, latitude, "" + policeName + "\n" + policeNumber, markerId, 0);
            item.setRightAccessory(true, NMapPOIflagType.CLICKABLE_ARROW);
        }
        poiData.endPOIdata();


        // create POI data overlay
        mPolicePOIDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
        if(DEBUG) {
            Log.d(LOG_TAG, "registered overlay count: " + mOverlayManager.sizeofOverlays());
        }

        // set event listener to the overlay
        mPolicePOIDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);

        // select an item
        //poiDataOverlay.selectPOIitem(0, true);

        // show all POI data
        //poiDataOverlay.showAllPOIdata(0);
    }

    /* POI data State Change Listener*/
    private final NMapPOIdataOverlay.OnStateChangeListener onPOIdataStateChangeListener = new NMapPOIdataOverlay.OnStateChangeListener() {

        @Override
        public void onCalloutClick(NMapPOIdataOverlay poiDataOverlay, NMapPOIitem item) {
            if (DEBUG) {
                Log.i(LOG_TAG, "onCalloutClick: title=" + item.getTitle());
            }

            // [[TEMP]] handle a click event of the callout
            //Toast.makeText(mContext, "onCalloutClick: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            // TODO 근처의 파출소 전화번호를 가져올 수 있으면 클릭 시 바로 전화 가능하게 구현.
            String title = item.getTitle();
            String number = title.split("\n")[1];
            String TELL_FORMAT = "tel:";
            //Toast.makeText(mContext, number, Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(TELL_FORMAT + number));
            mContext.startActivity(intent);
            getCurrentPosition();
        }

        @Override
        public void onFocusChanged(NMapPOIdataOverlay poiDataOverlay, NMapPOIitem item) {
            if (DEBUG) {
                if (item != null) {
                    Log.i(LOG_TAG, "onFocusChanged: " + item.toString());
                } else {
                    Log.i(LOG_TAG, "onFocusChanged: ");
                }
            }
        }
    };

    public void getCurrentPosition() {
        NGeoPoint currentPosition = mMapLocationManager.getMyLocation();
        if(currentPosition != null) {
            //Toast.makeText(mContext, "경도 : " + currentPosition.getLongitude() + "위도 : " + currentPosition.getLatitude() , Toast.LENGTH_SHORT).show();
            pointToAddress(currentPosition);
        }
    }

    public void pointToAddress(NGeoPoint point) {
        mMapActivityContext.findPlacemarkAtLocation(point.getLongitude(), point.getLatitude());
    }

    public NGeoPoint getMyLocation() {
        //return mMapLocationManager.getMyLocation();
        return myCurrentPosition;
    }
}
