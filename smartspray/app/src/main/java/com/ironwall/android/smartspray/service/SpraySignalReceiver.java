package com.ironwall.android.smartspray.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.ironwall.android.smartspray.database.DBManager;
import com.ironwall.android.smartspray.dto.LogSms;
import com.ironwall.android.smartspray.dto.SosNumber;
import com.ironwall.android.smartspray.global.GlobalVariable;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by KimJS on 2016-08-17.
 */
public class SpraySignalReceiver extends BroadcastReceiver {

    private final static String LOG_TAG = "SpraySignalReceiver##";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(GlobalVariable.BROADCASTER)) {

            int emresult = intent.getIntExtra(GlobalVariable.emergency, 0);
            int lbresult = intent.getIntExtra(GlobalVariable.lowbattery, 0);
            Log.d(LOG_TAG, "BroadcasterRecieved");
            if(emresult == 1) {
                //TODO DB transaction and send msg
                //Toast.makeText(context, "result recieved " + emresult, Toast.LENGTH_SHORT).show();


                Intent ringtonePlayingService = new Intent(context, RingtonePlayingService.class);


                //Telephone
                ArrayList<SosNumber> telnos = DBManager.getAllSosNumber();

                double lat = GlobalVariable.nowloc.getLatitude();
                double lng = GlobalVariable.nowloc.getLongitude();
                String uri = "https://www.google.co.kr/maps/place/"+lat +","+lng;

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
                        smsManager.sendTextMessage(sn.number, null, Uri.parse(uri).toString(), null, null);
                        DBManager.setLogSms(ls);
                    }
                } catch (Exception e) {

                    e.printStackTrace();
                }

                context.startService(ringtonePlayingService);
            }
            if(lbresult == 2) {
                Toast.makeText(context, "result recieved " + lbresult, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
