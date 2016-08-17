package com.ironwall.android.smartspray.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.ironwall.android.smartspray.global.GlobalVariable;

/**
 * Created by KimJS on 2016-07-28.
 */

//http://koreaparks.tistory.com/128
public class NetworkUtil {

    private static ConnectivityManager manager;

    public NetworkUtil(Context context) {
        manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

    }

    public static int getConnectivityStatus(Context context) {
        if(manager == null) {
            new NetworkUtil(context);
        }

        int result = 0;

        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                result += GlobalVariable.TYPE_WIFI;
                result += GlobalVariable.TYPE_INTERNET;
            }

            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                result += GlobalVariable.TYPE_INTERNET;
        }
        return result;
    }
}
