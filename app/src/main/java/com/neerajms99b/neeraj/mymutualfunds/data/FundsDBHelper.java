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
    private static final String TABLE_NAME_HISTORICAL = "historical";
    private static final String KEY_ID = "_id";
    private static final String FUND_SCODE = "scode";
    private static final String NAV_Q1 = "q1";
    private static final String NAV_Q2 = "q2";
    private static final String NAV_Q3 = "q3";
    private static final String NAV_Q4 = "q4";
    private static final String NAV_Q5 = "q5";
    private static final String NAV_Q6 = "q6";
    private static final String NAV_Q7 = "q7";
    private static final String NAV_Q8 = "q8";
    private static final String NAV_Q9 = "q9";
    private static final String NAV_Q10 = "q10";
    private static final String NAV_Q11 = "q11";
    private static final String NAV_Q12 = "q12";
    private static final String LAST_UPDATED = "lastupdated";

    private static final String TABLE_NAME_RECENT_SEARCH = "recent";
    private static final String SEARCH_WORD = "keyword";


    public static final String CREATE_FUNDS_DATABASE = "CREATE TABLE " + TABLE_NAME_HISTORICAL + " (" +
            KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            FUND_SCODE + " TEXT UNIQUE, " +
            NAV_Q1 + " TEXT, " +
            NAV_Q2 + " TEXT, " +
            NAV_Q3 + " TEXT, " +
            NAV_Q4 + " TEXT, " +
            NAV_Q5 + " TEXT, " +
            NAV_Q6 + " TEXT, " +
            NAV_Q7 + " TEXT, " +
            NAV_Q8 + " TEXT, " +
            NAV_Q9 + " TEXT, " +
            NAV_Q10 + " TEXT, " +
            NAV_Q11 + " TEXT, " +
            NAV_Q12 + " TEXT, " +
            LAST_UPDATED + " TEXT);";

    public static final String CREATE_RECENTS_DATABASE = "CREATE TABLE " + TABLE_NAME_RECENT_SEARCH
            + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            SEARCH_WORD + " TEXT UNIQUE);";

    public static final String DELETE_RECENTS_DATABASE = "DROP TABLE IF EXISTS " +
            TABLE_NAME_RECENT_SEARCH;

    public static final String DELETE_HISTORICAL_DATABASE = "DROP TABLE IF EXISTS " +
            TABLE_NAME_HISTORICAL;

    public FundsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_FUNDS_DATABASE);
        sqLiteDatabase.execSQL(CREATE_RECENTS_DATABASE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(DELETE_RECENTS_DATABASE);
        sqLiteDatabase.execSQL(DELETE_HISTORICAL_DATABASE);
    }
}
