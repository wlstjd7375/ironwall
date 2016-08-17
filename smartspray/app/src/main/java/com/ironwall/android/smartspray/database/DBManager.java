package com.ironwall.android.smartspray.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.ironwall.android.smartspray.dto.PoliceStation;
import com.ironwall.android.smartspray.dto.SosNumber;
import com.ironwall.android.smartspray.global.GlobalVariable;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by KimJS on 2016-07-25.
 */
public class DBManager {

    private static DBManager instance;

    private static DBHelper dbh;
    private static String LOG_TAG = "DBManager##";

    // 생성자
    private DBManager(Context context) {
        dbh = new DBHelper(context);
        dbh.open();
        if(GlobalVariable.IS_DEBUG_MODE) {
            Log.d(LOG_TAG, "DBHelper Opened");
        }
    }

    //For Single SQLite Connection
    //http://touchlabblog.tumblr.com/post/24474750219/single-sqlite-connection
    public static synchronized DBManager getManager(Context context) {
        if (instance == null) {
            instance = new DBManager(context);
        }
        //Log.d(LOG_TAG, "getManager() return instance");
        return instance;
    }

    public static synchronized long setSosNumber(SosNumber sn) {
        ContentValues values = new ContentValues();
        values.put(IronwallDB._SOS_COL_NAME, sn.name);
        values.put(IronwallDB._SOS_COL_NUMBER, sn.number);


        //error : return -1, success : return rowId
        long result = dbh.mDB.insert(IronwallDB._SOS_NUMBER_TABLE_NAME, null, values);
        if(GlobalVariable.IS_DEBUG_MODE) {
            Log.d(LOG_TAG, "setSosNumber Done, result code: " + result);
        }
        return result;
    }

    public static synchronized ArrayList<SosNumber> getAllSosNumber() {
        ArrayList<SosNumber> result = new ArrayList<SosNumber>();

        String sql = "select * from " + IronwallDB._SOS_NUMBER_TABLE_NAME + " ;";
        Cursor c = dbh.mDB.rawQuery(sql, null);
        c.moveToFirst();

        while(!c.isAfterLast()) {
            String name = c.getString(c.getColumnIndex(IronwallDB._SOS_COL_NAME));
            String number = c.getString(c.getColumnIndex(IronwallDB._SOS_COL_NUMBER));

            SosNumber sn = new SosNumber();
            sn.name = name;
            sn.number = number;
            result.add(sn);

            c.moveToNext();
        }

        c.close();
        if(GlobalVariable.IS_DEBUG_MODE) {
            Log.d(LOG_TAG, "getAllSosNumber Done, result size: " + result.size());
        }
        return result;
    }

    public static synchronized int deleteSosNumber(String number) {
        String sql = "delete from " + IronwallDB._SOS_NUMBER_TABLE_NAME
                + " where " + IronwallDB._SOS_COL_NUMBER + " = '" + number + "' ;";

        String whereClause = IronwallDB._SOS_COL_NUMBER + "=?";
        String[] whereArgs = new String[]{number};

        //fail : return 0, success : return rowId
        int result = dbh.mDB.delete(IronwallDB._SOS_NUMBER_TABLE_NAME, whereClause, whereArgs);
        if(GlobalVariable.IS_DEBUG_MODE) {
            Log.d(LOG_TAG, "deleteSosNumber Done, result code: " + result);
        }
        return result;
    }

    public static synchronized int getSosNumberCount() {
        String sql = "select count(*) from " + IronwallDB._SOS_NUMBER_TABLE_NAME + " ;";

        Cursor c = dbh.mDB.rawQuery(sql, null);
        c.moveToFirst();
        int count = c.getInt(0);
        c.close();

        return count;
    }

    public static synchronized long setPoliceStation(PoliceStation ps) {
        ContentValues values = new ContentValues();
        values.put(IronwallDB._POLICE_COL_MAIN_KEY, ps.main_key);
        values.put(IronwallDB._POLICE_COL_GOV_CODE, ps.gov_code);
        values.put(IronwallDB._POLICE_COL_NAME, ps.name);
        values.put(IronwallDB._POLICE_COL_ADD_KOR, ps.add_kor);
        values.put(IronwallDB._POLICE_COL_ADD_KOR_ROAD, ps.add_kor_road);
        values.put(IronwallDB._POLICE_COL_H_KOR_CITY, ps.h_kor_city);
        values.put(IronwallDB._POLICE_COL_H_KOR_GU, ps.h_kor_gu);
        values.put(IronwallDB._POLICE_COL_H_KOR_DONG, ps.h_kor_dong);
        values.put(IronwallDB._POLICE_COL_TEL, ps.tel);
        values.put(IronwallDB._POLICE_COL_LONGITUDE, ps.longitude);
        values.put(IronwallDB._POLICE_COL_LATITUDE, ps.latitude);

        //error : return -1, success : return rowId
        long result = dbh.mDB.insert(IronwallDB._POLICE_TABLE_NAME, null, values);
        /*
        if(GlobalVariable.IS_DEBUG_MODE) {
            Log.d(LOG_TAG, "setPoliceStation Done, result code: " + result);
        }*/
        return result;
    }

    public static synchronized int getPoliceStationCount() {
        String sql = "select count(*) from " + IronwallDB._POLICE_TABLE_NAME + " ;";

        Cursor c = dbh.mDB.rawQuery(sql, null);
        c.moveToFirst();
        int count = c.getInt(0);
        c.close();

        return count;
    }

    public static synchronized ArrayList<PoliceStation> getPoliceNearby(int longitude, int latitude, int range_long, int range_lat) {
        int long_from = longitude - range_long;
        int long_to = longitude + range_long;
        int lat_from = latitude - range_lat;
        int lat_to = latitude + range_lat;
        ArrayList<PoliceStation> result = new ArrayList<PoliceStation>();

        String sql = "select "+ IronwallDB._POLICE_COL_NAME + ", "
                + IronwallDB._POLICE_COL_TEL + ", "
                + IronwallDB._POLICE_COL_LONGITUDE + ", "
                + IronwallDB._POLICE_COL_LATITUDE
                + " from " + IronwallDB._POLICE_TABLE_NAME
                + " where " + IronwallDB._POLICE_COL_LONGITUDE + " between " + long_from + " and " + long_to
                + " and " + IronwallDB._POLICE_COL_LATITUDE + " between " + lat_from + " and " + lat_to
                + " ;";

        Cursor c = dbh.mDB.rawQuery(sql, null);
        c.moveToFirst();
        while(!c.isAfterLast()) {
            PoliceStation ps = new PoliceStation();
            ps.name = c.getString(c.getColumnIndex(IronwallDB._POLICE_COL_NAME));
            ps.tel = c.getString(c.getColumnIndex(IronwallDB._POLICE_COL_TEL));
            ps.longitude = c.getInt(c.getColumnIndex(IronwallDB._POLICE_COL_LONGITUDE));
            ps.latitude = c.getInt(c.getColumnIndex(IronwallDB._POLICE_COL_LATITUDE));

            result.add(ps);

            c.moveToNext();
        }

        c.close();
        if(GlobalVariable.IS_DEBUG_MODE) {
            Log.d(LOG_TAG, "getPoliceNearby Done, result size: " + result.size());
        }
        return result;
    }
}
