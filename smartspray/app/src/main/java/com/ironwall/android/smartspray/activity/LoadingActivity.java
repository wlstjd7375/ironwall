package com.ironwall.android.smartspray.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.ironwall.android.smartspray.R;
import com.ironwall.android.smartspray.database.DBManager;
import com.ironwall.android.smartspray.dto.PoliceStation;
import com.ironwall.android.smartspray.global.GlobalVariable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

public class LoadingActivity extends AppCompatActivity {

    private String LOG_TAG = "LoadingActivity##";
    private Context mContext;

    private Thread mThread;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == GlobalVariable.SUCCESS) {
                if(mThread != null) {
                    mThread.interrupt();
                }
                Intent intent = new Intent(mContext, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    };

    private Runnable setPoliceData = new Runnable() {
        @Override
        public void run() {
            setPoliceData();
            mHandler.sendEmptyMessageDelayed(GlobalVariable.SUCCESS, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set Status bar color
        if (Build.VERSION.SDK_INT >= 21) {
            //getWindow().setStatusBarColor(Color.parseColor("#222222"));
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        setContentView(R.layout.activity_loading);
        mContext = this;

        int count = DBManager.getManager(mContext).getPoliceStationCount();
        if(GlobalVariable.IS_DEBUG_MODE) {
            Log.d(LOG_TAG, "Police Station Count = " + count);
        }
        if(count == 0) {
            mThread = new Thread(setPoliceData);
            mThread.start();
        }
        else {
            mHandler.sendEmptyMessageDelayed(GlobalVariable.SUCCESS, 1000);
        }


    }

    private void setPoliceData() {
        String personXMLString = null;
        try {
            personXMLString = getXMLFileFromAssets();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if(personXMLString != null) {
            parseAndSavePoliceData(personXMLString);
        }
        else {
            // XML 파일 읽기 실패
        }
    }

    private String getXMLFileFromAssets() throws IOException {
        AssetManager assetManager = getResources().getAssets();
        AssetManager.AssetInputStream ais = (AssetManager.AssetInputStream)assetManager.open("policeDB.xml");
        BufferedReader br = new BufferedReader(new InputStreamReader(ais));

        String line;
        StringBuilder data = new StringBuilder();
        while((line=br.readLine()) != null) {
            data.append(line);
        }

        return data.toString();
    }

    private void parseAndSavePoliceData(String data) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(data));

            String sTag;
            PoliceStation ps = new PoliceStation();

            // nextText();
            // 현재 eventType이 START_TAG 일때 다음 element 가 TEXT 인 경우 해당 element 값을 리턴, END_TAG 인 경우 empty string 리턴
            // 그렇지 않은경우 예외를 리턴
            // 해당 함수가 성공적으로 호출된 후 parser 는 END_TAG 에 위치함
            // https://developer.android.com/reference/org/xmlpull/v1/XmlPullParser.html#nextText()
            int eventType = parser.getEventType();
            while(eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        sTag = parser.getName();
                        if(sTag.equalsIgnoreCase("ROW")) {
                            //객체 초기화
                            ps = new PoliceStation();
                        } else if(sTag.equalsIgnoreCase("MAIN_KEY")) {
                            ps.main_key = parser.nextText();
                        } else if(sTag.equalsIgnoreCase("GOV_CODE")) {
                            ps.gov_code = parser.nextText();
                        } else if(sTag.equalsIgnoreCase("NAME_KOR")) {
                            ps.name = parser.nextText();
                        } else if(sTag.equalsIgnoreCase("ADD_KOR")) {
                            ps.add_kor = parser.nextText();
                        } else if(sTag.equalsIgnoreCase("ADD_KOR_ROAD")) {
                            ps.add_kor_road = parser.nextText();
                        } else if(sTag.equalsIgnoreCase("H_KOR_CITY")) {
                            ps.h_kor_city = parser.nextText();
                        } else if(sTag.equalsIgnoreCase("H_KOR_GU")) {
                            ps.h_kor_gu = parser.nextText();
                        } else if(sTag.equalsIgnoreCase("H_KOR_DONG")) {
                            ps.h_kor_dong = parser.nextText();
                        } else if(sTag.equalsIgnoreCase("TEL")) {
                            ps.tel = parser.nextText();
                        } else if(sTag.equalsIgnoreCase("LATITUDE")) {
                            ps.latitude = Integer.parseInt(parser.nextText());
                        } else if(sTag.equalsIgnoreCase("LONGITUDE")) {
                            ps.longitude = Integer.parseInt(parser.nextText());
                        } else {
                            //예외
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        sTag = parser.getName();
                        if(sTag.equalsIgnoreCase("ROW")) {
                            //TODO save to db
                            long result = DBManager.getManager(mContext).setPoliceStation(ps);
                            if(result == -1) { // fail
                                Toast.makeText(mContext, "DB Insertion Fail", Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                }
                eventType = parser.next();
            }


        } catch (Exception e) {
            Log.d(LOG_TAG, "parsePoliceData error: " +  e.toString());
        }
    }
}
