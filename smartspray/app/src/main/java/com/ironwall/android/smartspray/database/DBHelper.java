package com.ironwall.android.smartspray.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ironwall.android.smartspray.global.GlobalVariable;

/**
 * Created by KimJS on 2016-07-25.
 */
public class DBHelper {

    private static final String LOG_TAG = "DBHelper##";
    private static final String DATABASE_NAME = "ironwall.sqlite";

    //DB 버전 : 테이블 구조가 변경되면 버전을 업그레이드 시켜준다 > drop table, create table
    private static final int DATABASE_VERSION = 3;

    public static SQLiteDatabase mDB;
    private DatabaseHelper mDBHelper;
    private Context mCtx;

    public DBHelper(Context context){
        this.mCtx = context;
    }

    public DBHelper open() throws SQLException {
        mDBHelper = new DatabaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        mDB = mDBHelper.getWritableDatabase();
        if(GlobalVariable.IS_DEBUG_MODE) {
            Log.d(LOG_TAG, "DBHelper opened");
        }
        return this;
    }

    public void close(){
        mDB.close();
        Log.d(LOG_TAG, "DBHelper closed");
    }

    private class DatabaseHelper extends SQLiteOpenHelper {

        // 생성자
        public DatabaseHelper(Context context, String name,
                              SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        // 최초 DB를 만들때 한번만 호출된다.
        @Override
        public void onCreate(SQLiteDatabase db) {
            if(GlobalVariable.IS_DEBUG_MODE) {
                Log.d(LOG_TAG, "Database onCreate()");
            }
            db.execSQL(IronwallDB.CreateDB._CREATE_SOS_NUMBER_TABLE);
            db.execSQL(IronwallDB.CreateDB._CREATE_MESSAGE_TYPE_TABLE);
            db.execSQL(IronwallDB.CreateDB._CREATE_POLICE_TABLE);
        }

        // 버전이 업데이트 되었을 경우 DB를 다시 만들어 준다.
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if(GlobalVariable.IS_DEBUG_MODE) {
                Log.d(LOG_TAG, "Database onUpgrade()");
            }
            db.execSQL("DROP TABLE IF EXISTS " + IronwallDB._SOS_NUMBER_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + IronwallDB._MESSAGE_TYPE_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + IronwallDB._POLICE_TABLE_NAME);

            onCreate(db);
        }
    }

}
