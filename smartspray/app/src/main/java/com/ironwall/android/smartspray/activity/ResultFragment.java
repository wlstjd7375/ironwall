package com.ironwall.android.smartspray.activity;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ironwall.android.smartspray.R;
import com.ironwall.android.smartspray.database.DBManager;
import com.ironwall.android.smartspray.dto.LogSms;

import java.util.ArrayList;

/**
 * Created by KimJS on 2016-09-10.
 */
public class ResultFragment extends Fragment {

    private Context mContext;

    private Button btOK;
    private Button btCancel;
    private LinearLayout llButtonGroup;
    private TextView tvResult;
    private TextView tvtitle;
    private String result;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContext = getActivity();
        final View view = inflater.inflate(R.layout.fragment_result, container, false);

        btOK = (Button)view.findViewById(R.id.btOK);
        btCancel = (Button)view.findViewById(R.id.btCancel);
        tvResult = (TextView)view.findViewById(R.id.tvResult);
        llButtonGroup = (LinearLayout)view.findViewById(R.id.llButtonGroup);
        tvtitle = (TextView)view.findViewById(R.id.tvtitle);

        result = "결과 : \n";
        btOK.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(view.getContext());
                String name = sharedPref.getString("pref_my_name", "");
                ArrayList <LogSms> logsmses = DBManager.getAllLogSms();
                String message = name+"님의 위험상황이 종료되었습니다.";
                for(LogSms ls : logsmses) {

                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(ls.number, null, message, null, null);
                    DBManager.modifyLogSms(ls.group_key, ls.name, ls.number);
                    result += ls.name + ", " + ls.number + "님 에게 취소 문자가 전송되었습니다.\n";
                }

                ArrayList <LogSms> ls = DBManager.getAllLogSms();
                for(LogSms l : ls) {
                    Log.d("LOGSMS", l.result);
                }

                tvtitle.setText("취소 문자가 전송되었습니다.");
                tvResult.setText(result);
                llButtonGroup.setVisibility(View.INVISIBLE);
            }
        });

        btCancel.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvtitle.setText("문자 전송이 취소되었습니다.");
                llButtonGroup.setVisibility(View.INVISIBLE);
            }
        });

/*
        //TODO
        // 안전 문자 보낼지 확인후 전송
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("SOS취소 알림창").setMessage("SMS전송을 취소하시겠습니까 ? ").setCancelable(false)
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(view.getContext());
                        String name = sharedPref.getString("pref_my_name", "");
                        ArrayList <LogSms> logsmses = DBManager.getAllLogSms();
                        String message = name+"님의 위험상황이 종료되었습니다.";
                        for(LogSms ls : logsmses) {

                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(ls.number, null, message, null, null);
                            DBManager.modifyLogSms(ls.group_key, ls.name, ls.number);
                        }

                        ArrayList <LogSms> ls = DBManager.getAllLogSms();
                        for(LogSms l : ls) {
                            Log.d("LOGSMS", l.result);
                        }
                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog dialog = builder.create();    // 알림창 객체 생성
        dialog.show();
*/
        return view;
    }
}
