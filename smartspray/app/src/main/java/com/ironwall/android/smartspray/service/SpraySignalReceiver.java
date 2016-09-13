package com.ironwall.android.smartspray.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Debug;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.ironwall.android.smartspray.database.DBManager;
import com.ironwall.android.smartspray.global.GlobalVariable;

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


                //Intent ringtonePlayingService = new Intent(context, RingtonePlayingService.class);
                //context.startService(ringtonePlayingService);
            }
            if(lbresult == 2) {
                Toast.makeText(context, "result recieved " + lbresult, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
