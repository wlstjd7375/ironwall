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

    public static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        int result = 0;

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
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
