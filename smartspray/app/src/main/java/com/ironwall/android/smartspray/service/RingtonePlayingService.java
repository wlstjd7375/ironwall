package com.ironwall.android.smartspray.service;

import android.app.Notification;
import android.app.PendingIntent;
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
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.ironwall.android.smartspray.R;
import com.ironwall.android.smartspray.activity.MainActivity;
import com.ironwall.android.smartspray.activity.SafenowActivity;
import com.ironwall.android.smartspray.global.GlobalVariable;

/**
 * Created by KimJS on 2016-08-24.
 */
public class RingtonePlayingService extends Service {

    private Context mContext;
    private Ringtone ringtone;
    private static final String LOG_TAG = "RingtoneService##";

    private static final int STARTFOREGROUND_ACTION = 1;
    private static final int STOP_ALARM_ACTION = 2;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, intent.toString());

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean isRingtonePlayOn = sp.getBoolean("pref_warning_alarm", false);
        if(isRingtonePlayOn) {
            SharedPreferences getAlarms = PreferenceManager.getDefaultSharedPreferences(this);
            String ringtoneName = getAlarms.getString("pref_warning_alarm_ringtone", "");
            Uri ringtoneUri = Uri.parse(ringtoneName);
            if(GlobalVariable.IS_DEBUG_MODE) {
                Log.d(LOG_TAG, ringtoneName);
            }
            ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
            ringtone.play();
        }
        //TODO
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentTitle("IRONWALL")
                        .setContentText("스프레이가 작동되었습니다.")
                        .setAutoCancel(true);

        Intent baseIntent = new Intent(getApplicationContext(), MainActivity.class);
        Intent resultIntent = new Intent(getApplicationContext(), SafenowActivity.class);

        baseIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(baseIntent);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPandingIntent = stackBuilder.getPendingIntent(
                        0, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPandingIntent);
        startForeground(1112, mBuilder.build());

        //서비스 종료시 재시작 하지 않음
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if(ringtone != null) {
            ringtone.stop();
        }
        super.onDestroy();
    }
}
