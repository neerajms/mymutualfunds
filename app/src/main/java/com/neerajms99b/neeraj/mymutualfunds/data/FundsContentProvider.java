package com.neerajms99b.neeraj.mymutualfunds.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
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
    private static final String TABLE_NAME_RECENT_SEARCH = "recent";
    private static final String TABLE_NAME_FULL_FUNDS_LIST = "fullfundslist";
    private static final String TABLE_NAME_PORTFOLIO = "portfolio";
    private static final String URL_PORTFOLIO = "content://" + AUTHORITY + "/" + TABLE_NAME_PORTFOLIO;
    private static final String URL_RECENT_SEARCH = "content://" + AUTHORITY + "/" + TABLE_NAME_RECENT_SEARCH;
    private static final String URL_FULL_FUNDS_LIST = "content://" + AUTHORITY + "/" + TABLE_NAME_FULL_FUNDS_LIST;
    public static Uri mUriPortfolio = Uri.parse(URL_PORTFOLIO);
    public static Uri mUriFullFundsList = Uri.parse(URL_FULL_FUNDS_LIST);

    public static final String KEY_ID = "_id";
    public static final String FUND_SCODE = "scode";
    public static final String LAST_UPDATED_NAV = "lastupdatednav";

    public static final String SEARCH_WORD = "keyword";
    private SQLiteDatabase database;

    public static final String FUND_NAME = "fundname";
    public static final String NAV = "nav";

    private static final UriMatcher mUriMatcher;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(AUTHORITY, TABLE_NAME_RECENT_SEARCH + "/*", 1);
        mUriMatcher.addURI(AUTHORITY, TABLE_NAME_PORTFOLIO + "/#", 2);
        mUriMatcher.addURI(AUTHORITY, TABLE_NAME_RECENT_SEARCH, 3);
        mUriMatcher.addURI(AUTHORITY, TABLE_NAME_PORTFOLIO, 4);
        mUriMatcher.addURI(AUTHORITY, TABLE_NAME_PORTFOLIO + "/_id/#", 8);
        mUriMatcher.addURI(AUTHORITY, TABLE_NAME_FULL_FUNDS_LIST, 7);
        mUriMatcher.addURI(AUTHORITY, TABLE_NAME_FULL_FUNDS_LIST + "/#", 5);
        mUriMatcher.addURI(AUTHORITY, TABLE_NAME_FULL_FUNDS_LIST + "/*", 6);
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
        switch (mUriMatcher.match(uri)) {
            case 1:
                qb.setTables(TABLE_NAME_RECENT_SEARCH);
                selection = SEARCH_WORD + " LIKE \'%" + uri.getLastPathSegment() + "%\'";
                break;

            case 2:
                qb.setTables(TABLE_NAME_PORTFOLIO);
                selection = FUND_SCODE + "=" + uri.getLastPathSegment();
                Log.d("SELECTION:", selection);
                break;
            case 4:
                qb.setTables(TABLE_NAME_PORTFOLIO);
                break;
            case 5:
                qb.setTables(TABLE_NAME_FULL_FUNDS_LIST);
                selection = FUND_SCODE + "=" + uri.getLastPathSegment();
                break;
            case 6:
                qb.setTables(TABLE_NAME_FULL_FUNDS_LIST);
                selection = FUND_NAME + " LIKE \'" + uri.getLastPathSegment() + "%\'";
                projection = new String[]{KEY_ID, FUND_SCODE, FUND_NAME};
                break;
            case 8:
                qb.setTables(TABLE_NAME_PORTFOLIO);
                selection = KEY_ID + "=" + uri.getLastPathSegment();
                projection = new String[]{LAST_UPDATED_NAV};
                break;
            default:
                return null;
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
        try {
            long rowID = 0;
            switch (mUriMatcher.match(uri)) {
                case 3:
                    rowID = database.insert(TABLE_NAME_RECENT_SEARCH, null, contentValues);
                    break;
                case 4:
                    rowID = database.insert(TABLE_NAME_PORTFOLIO, null, contentValues);
                    break;
                case 7:
                    rowID = database.insert(TABLE_NAME_FULL_FUNDS_LIST, null, contentValues);
                    break;
            }

            if (rowID > 0) {
                String _uri = String.valueOf(uri) + "/" + String.valueOf(rowID);
                Uri tempUri = Uri.parse(_uri);
                getContext().getContentResolver().notifyChange(tempUri, null);
                return tempUri;
            }
        } catch (SQLiteConstraintException sce) {
            return null;
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String column, String[] args) {
        int deleted = 0;
        String whereClause = null;
        switch (mUriMatcher.match(uri)) {
            case 3:
                deleted = database.delete(TABLE_NAME_RECENT_SEARCH, null, args);
                break;
            case 2:
                whereClause = FUND_SCODE + "=" + uri.getLastPathSegment();
                deleted = database.delete(TABLE_NAME_PORTFOLIO, whereClause, args);
                break;
        }
        return deleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionValues) {
        int result = 0;
        switch (mUriMatcher.match(uri)) {
            case 1:
                selection = SEARCH_WORD + "=" + uri.getLastPathSegment();
                result = database.update(TABLE_NAME_RECENT_SEARCH, contentValues, selection, selectionValues);
                break;
            case 2:
                selection = FUND_SCODE + "=" + uri.getLastPathSegment();
                result = database.update(TABLE_NAME_PORTFOLIO, contentValues, selection, selectionValues);
                break;
            case 5:
                selection = FUND_SCODE + "=" + uri.getLastPathSegment();
                result = database.update(TABLE_NAME_FULL_FUNDS_LIST, contentValues, selection, selectionValues);
        }
        return result;
    }
}
