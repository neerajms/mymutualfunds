package com.neerajms99b.neeraj.mymutualfunds.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by neeraj on 12/8/16.
 */
public class FundsDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "fundsdb";
    private static final String FUNDS_TABLE_NAME = "funds";
    private static final String KEY_ID = "_id";
    private static final String FUND_SCODE = "scode";
    private static final String FUND_NAME = "fund_name";
    private static final String FUND_NAV = "nav";
    private static final String FUND_CHANGE_VALUE = "change_value";
    private static final String FUND_CHANGE_PERCENT = "change_percent";

    public static final String CREATE_FUNDS_DATABASE = "CREATE TABLE " + FUNDS_TABLE_NAME + " ("+
            KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
            FUND_SCODE + " TEXT UNIQUE, "+
            FUND_NAME + " TEXT, " +
            FUND_NAV + " TEXT);";


    public FundsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_FUNDS_DATABASE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
