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

    //create database
    public static final class CreateDB implements BaseColumns {
        //TODO Add NOT NULL Statement!!!

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
    }
}
