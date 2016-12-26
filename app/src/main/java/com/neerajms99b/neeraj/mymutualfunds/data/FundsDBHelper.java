package com.neerajms99b.neeraj.mymutualfunds.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by neeraj on 12/8/16.
 */
public class FundsDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "fundsdb";
    private static final String TABLE_NAME_HISTORICAL = "historical";
    private static final String TABLE_NAME_PORTFOLIO = "portfolio";
    private static final String KEY_ID = "_id";
    private static final String FUND_SCODE = "scode";
    private static final String LAST_UPDATED_NAV = "lastupdatednav";

    private static final String TABLE_NAME_RECENT_SEARCH = "recent";

    private static final String TABLE_NAME_FULL_FUNDS_LIST = "fullfundslist";
    private static final String FUND_NAME = "fundname";
    private static final String NAV = "nav";

    public static final String CREATE_FUNDS_DATABASE = "CREATE TABLE " + TABLE_NAME_PORTFOLIO + " (" +
            KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            FUND_SCODE + " TEXT UNIQUE, " +
            LAST_UPDATED_NAV + " TEXT);";

    public static final String CREATE_FULL_FUNDS_TABLE = "CREATE TABLE " + TABLE_NAME_FULL_FUNDS_LIST
            + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + FUND_SCODE + " TEXT UNIQUE, "
            + FUND_NAME + " TEXT, "
            + NAV + " TEXT, "
            + LAST_UPDATED_NAV + " TEXT);";

    public static final String DELETE_RECENTS_DATABASE = "DROP TABLE IF EXISTS " +
            TABLE_NAME_RECENT_SEARCH;

    public static final String DELETE_HISTORICAL_DATABASE = "DROP TABLE IF EXISTS " +
            TABLE_NAME_HISTORICAL;

    public static final String DELETE_FULL_FUNDS_LIST = "DROP TABLE IF EXISTS " +
            TABLE_NAME_FULL_FUNDS_LIST;

    public FundsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_FUNDS_DATABASE);
        sqLiteDatabase.execSQL(CREATE_FULL_FUNDS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            sqLiteDatabase.execSQL(DELETE_HISTORICAL_DATABASE);
            sqLiteDatabase.execSQL(DELETE_RECENTS_DATABASE);
            sqLiteDatabase.execSQL(CREATE_FULL_FUNDS_TABLE);
            sqLiteDatabase.execSQL(CREATE_FUNDS_DATABASE);
        }
    }
}
