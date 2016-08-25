package com.ironwall.android.smartspray.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ironwall.android.smartspray.global.GlobalVariable;

/**
 * Created by KimJS on 2016-08-24.
 */
public class RingtonePlayingService extends Service {

    private Ringtone ringtone;
    private static final String LOG_TAG = "RingtoneService##";


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, intent.toString());
        SharedPreferences getAlarms = PreferenceManager.getDefaultSharedPreferences(this);
        String ringtoneName = getAlarms.getString("pref_warning_alarm_ringtone", "");
        Uri ringtoneUri = Uri.parse(ringtoneName);
        if(GlobalVariable.IS_DEBUG_MODE) {
            Log.d(LOG_TAG, ringtoneName);
        }
        ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
        ringtone.play();

        //서비스 종료시 재시작 하지 않음
        return START_NOT_STICKY;
        //return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        ringtone.stop();
        super.onDestroy();
    }
}
