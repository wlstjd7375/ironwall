package com.ironwall.android.smartspray.util;

import android.content.Context;
import android.location.LocationManager;

import com.ironwall.android.smartspray.global.GlobalVariable;

/**
 * Created by KimJS on 2016-07-28.
 */
public class GpsUtil {

    public static int getConnectivityStatus(Context context) {
        LocationManager manager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE );
        int result = 0;
        if(manager != null) {
            //## GPS 위성 사용 여부 체크
            boolean gps_on = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            //## GPS 무선인터넷 사용 여부 체크
            //boolean gps_network_on = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if(gps_on) {
                result += GlobalVariable.TYPE_GPS;
            }
        }

        return result;
    }
}
