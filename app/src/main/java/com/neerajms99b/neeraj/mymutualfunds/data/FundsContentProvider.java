package com.neerajms99b.neeraj.mymutualfunds.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by neeraj on 13/8/16.
 */
public class FundsContentProvider extends ContentProvider {
    private static final String AUTHORITY = "com.neerajms99b.neeraj.mymutualfunds.data";
    private static final String TABLE_NAME_HISTORICAL = "historical";
    private static final String URL_HISTORICAL = "content://" + AUTHORITY + "/" + TABLE_NAME_HISTORICAL;
    public static Uri mUriHistorical = Uri.parse(URL_HISTORICAL);

    public static final String KEY_ID = "_id";
    public static final String FUND_SCODE = "scode";
    public static final String NAV_Q1 = "q1";
    public static final String NAV_Q2 = "q2";
    public static final String NAV_Q3 = "q3";
    public static final String NAV_Q4 = "q4";
    public static final String NAV_Q5 = "q5";
    public static final String NAV_Q6 = "q6";
    public static final String NAV_Q7 = "q7";
    public static final String NAV_Q8 = "q8";
    public static final String NAV_Q9 = "q9";
    public static final String NAV_Q10 = "q10";
    public static final String NAV_Q11 = "q11";
    public static final String NAV_Q12 = "q12";
    private SQLiteDatabase database;

    private static final UriMatcher mUriMatcher;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(AUTHORITY, TABLE_NAME_HISTORICAL + "/#", 2);
    }

    @Override
    public boolean onCreate() {
        FundsDBHelper fundsDBHelper = new FundsDBHelper(getContext());
        database = fundsDBHelper.getWritableDatabase();
        return (database != null) ? true : false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME_HISTORICAL);

        switch (mUriMatcher.match(uri)) {
//            case 1:
//                break;

            case 2:
                selection = FUND_SCODE + "=" + uri.getLastPathSegment();
                Log.d("SELECTION:", selection);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        Cursor c = qb.query(database, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        ContentValues cv = contentValues;
        long rowID = database.insert(TABLE_NAME_HISTORICAL, null, contentValues);
        if (rowID > 0) {
            String _uri = String.valueOf(uri) + "/" + String.valueOf(cv.get(KEY_ID));
            Uri tempUri = Uri.parse(_uri);
            getContext().getContentResolver().notifyChange(tempUri, null);
            return tempUri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public int delete(Uri uri, String column, String[] args) {
        String whereClause = column + " = " + uri.getLastPathSegment();
        int deleted = database.delete(TABLE_NAME_HISTORICAL, whereClause, args);
        return deleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionValues) {
        selection = FUND_SCODE + "=" + uri.getLastPathSegment();
        int result = database.update(TABLE_NAME_HISTORICAL, contentValues, selection, selectionValues);
        return result;
    }
}
