package com.ironwall.android.smartspray.database;

import android.provider.BaseColumns;

/**
 * Created by KimJS on 2016-07-25.
 */
public class IronwallDB {


    //sos number
    public static final String _SOS_NUMBER_TABLE_NAME = "sos_number";
    public static final String _SOS_COL_NUMBER = "number";
    public static final String _SOS_COL_NAME = "name";

    //message
    public static final String _MESSAGE_TYPE_TABLE_NAME = "message";
    public static final String _MESSAGE_COL_TYPE = "type";
    public static final String _MESSAGE_COL_DEFAULT_MESSAGE = "default_message";
    public static final String _MESSAGE_COL_CUSTOM_MESSAGE = "custom_message";
    public static final String _MESSAGE_COL_FLAG = "flag";

    //police
    public static final String _POLICE_TABLE_NAME = "police";
    public static final String _POLICE_COL_MAIN_KEY = "main_key";
    public static final String _POLICE_COL_GOV_CODE = "gov_code";
    public static final String _POLICE_COL_NAME = "name";
    public static final String _POLICE_COL_ADD_KOR = "add_kor";
    public static final String _POLICE_COL_ADD_KOR_ROAD = "add_kor_road";
    public static final String _POLICE_COL_H_KOR_CITY = "h_kor_city";
    public static final String _POLICE_COL_H_KOR_GU = "h_kor_gu";
    public static final String _POLICE_COL_H_KOR_DONG = "h_kor_dong";
    public static final String _POLICE_COL_TEL = "tel";
    public static final String _POLICE_COL_LONGITUDE = "longitude";     // double vs int
    public static final String _POLICE_COL_LATITUDE = "latitude";       // double vs int


    //log
    public static final String _LOG_SOS_TABLE_NAME = "log_sos";
    public static final String _LOG_SOS_COL_GROUP_KEY = "group_key"; //타임스탬프, 인덱스 만들기
    public static final String _LOG_SOS_COL_NAME = "name";
    public static final String _LOG_SOS_COL_NUMBER = "number"; //유니크
    public static final String _LOG_SOS_COL_RESULT = "result";
    public static final String _LOG_SOS_COL_LATITUDE = "latitude";
    public static final String _LOG_SOS_COL_LONGITUDE = "longitude";
    public static final String _LOG_SOS_COL_MESSAGE = "message"; //우선 보류

    //create database
    public static final class CreateDB implements BaseColumns {
        //TODO Add NOT NULL Statement!!!

        //TODO key : integer autoincrement, number : 후보키
        public static final String _CREATE_SOS_NUMBER_TABLE = "create table " + _SOS_NUMBER_TABLE_NAME
                + " (" + _SOS_COL_NAME + " text, "
                + _SOS_COL_NUMBER + " text, "
                + " primary key(" + _SOS_COL_NUMBER + ") ); ";

        public static final String _CREATE_MESSAGE_TYPE_TABLE = "create table " + _MESSAGE_TYPE_TABLE_NAME
                + " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + _MESSAGE_COL_TYPE + " text,"
                + _MESSAGE_COL_DEFAULT_MESSAGE + " text,"
                + _MESSAGE_COL_CUSTOM_MESSAGE + " text,"
                + _MESSAGE_COL_FLAG + " text "
                + " );";

        public static final String _CREATE_POLICE_TABLE = "create table " + _POLICE_TABLE_NAME
                + " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + _POLICE_COL_MAIN_KEY + " text,"
                + _POLICE_COL_GOV_CODE + " text, "
                + _POLICE_COL_NAME + " text not null, "
                + _POLICE_COL_ADD_KOR + " text, "
                + _POLICE_COL_ADD_KOR_ROAD + " text, "
                + _POLICE_COL_H_KOR_CITY + " text, "
                + _POLICE_COL_H_KOR_GU + " text, "
                + _POLICE_COL_H_KOR_DONG + " text, "
                + _POLICE_COL_TEL + " text not null, "
                + _POLICE_COL_LONGITUDE + " integer not null, "
                + _POLICE_COL_LATITUDE + " integer not null "
                + " );";

        public static final String _CREATE_LOG_SMS_TABLE = "create table " + _LOG_SOS_TABLE_NAME
                + " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + _LOG_SOS_COL_GROUP_KEY + " text,"
                + _LOG_SOS_COL_NAME + " text, "
                + _LOG_SOS_COL_NUMBER + " text not null, "
                + _LOG_SOS_COL_RESULT + " text, "
                + _LOG_SOS_COL_LATITUDE + " integer, "
                + _LOG_SOS_COL_LONGITUDE + " integer, "
                + _LOG_SOS_COL_MESSAGE + " text"
                + " );";

    }
}
