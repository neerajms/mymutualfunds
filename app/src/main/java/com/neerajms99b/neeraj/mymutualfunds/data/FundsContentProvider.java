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
    private static final String TABLE_NAME = "funds";
    private static final String URL = "content://" + AUTHORITY + "/" + TABLE_NAME;
    public static Uri mUri = Uri.parse(URL);

    public static final String KEY_ID = "_id";
    public static final String FUND_SCODE = "scode";
    public static final String FUND_NAME = "fund_name";
    public static final String FUND_NAV = "nav";
    public static final String UNITS_OWNED = "units";

    private SQLiteDatabase database;

    private static final UriMatcher mUriMatcher;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(AUTHORITY, TABLE_NAME, 1);
        mUriMatcher.addURI(AUTHORITY, TABLE_NAME + "/#", 2);
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
        qb.setTables(TABLE_NAME);

        switch (mUriMatcher.match(uri)) {
            case 1:
                break;

            case 2:
                selection = FUND_SCODE + "=" + uri.getLastPathSegment();
                Log.d("SELECTION:", selection);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        Cursor c = qb.query(database, projection, selection, selectionArgs, null, null, sortOrder);
        Log.d("Query:", String.valueOf(c));
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
        long rowID = database.insert(TABLE_NAME, null, contentValues);
        if (rowID > 0) {
            String _uri = String.valueOf(uri) + "/" + String.valueOf(cv.get(KEY_ID));
            Uri tempUri = Uri.parse(_uri);
            getContext().getContentResolver().notifyChange(tempUri, null);
            return tempUri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionValues) {
        selection = FUND_SCODE + " = ? ";
        int result = database.update(TABLE_NAME,contentValues,selection,selectionValues);
        Log.d("update",String.valueOf(result));
        return result;
    }
}
