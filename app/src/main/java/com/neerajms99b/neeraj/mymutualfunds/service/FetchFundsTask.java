package com.neerajms99b.neeraj.mymutualfunds.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.neerajms99b.neeraj.mymutualfunds.R;
import com.neerajms99b.neeraj.mymutualfunds.data.FundsContentProvider;
import com.neerajms99b.neeraj.mymutualfunds.models.FundInfo;
import com.neerajms99b.neeraj.mymutualfunds.ui.MainActivity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;

/**
 * Created by neeraj on 8/8/16.
 */
public class FetchFundsTask extends GcmTaskService {
    private final String TAG = getClass().getSimpleName();
    private static final String TEXT_FILE_URL = "http://portal.amfiindia.com/spages/NAV0.txt";

    private Context mContext;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mTaskParamTag;

    private String[] HISTORICAL_COLUMNS = {
            FundsContentProvider.FUND_SCODE,
            FundsContentProvider.LAST_UPDATED_NAV
    };

    public static final int DOWNLOAD_PROGRESS = 4283;

    public FetchFundsTask() {
    }

    public FetchFundsTask(Context context) {
        mContext = context;
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        mTaskParamTag = taskParams.getTag();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mContext == null) {
            mContext = this;
        }

        if (mTaskParamTag.equals(mContext.getResources().getString(R.string.tag_update_nav))) {
            boolean toBeUpdated = false;
            try {
                URL url = new URL(TEXT_FILE_URL);
                URLConnection connection = url.openConnection();
                connection.connect();
                InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                String[] tokens = new String[8];
                int i;
                bufferedReader.readLine();
                boolean firstRow = true;
                while ((line = bufferedReader.readLine()) != null) {
                    StringTokenizer tokenizer = splitOnSemiColon(line);
                    if (tokenizer.countTokens() == 8) {
                        i = 0;
                        while (i < 8) {
                            tokens[i] = tokenizer.nextToken();
                            i++;
                        }
                        if (firstRow) {
                            firstRow = false;
                            toBeUpdated = toBeUpdated(tokens[7]);
                            if (!toBeUpdated) {
                                break;
                            }
                        }
                        Uri uri = Uri.parse(FundsContentProvider.mUriFullFundsList.toString() + "/" + tokens[0]);
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(FundsContentProvider.NAV, tokens[4]);
                        contentValues.put(FundsContentProvider.LAST_UPDATED_NAV, tokens[7]);
                        int result = mContext.getContentResolver().update(uri, contentValues, null, null);
                        if (result == 0) {
                            contentValues.put(FundsContentProvider.FUND_SCODE, tokens[0]);
                            contentValues.put(FundsContentProvider.FUND_NAME, tokens[3]);
                            mContext.getContentResolver().insert(FundsContentProvider.mUriFullFundsList, contentValues);
                        }
                    }
                }
                inputStream.close();
                if (toBeUpdated) {
                    Cursor cursorPortfolio = mContext.getContentResolver()
                            .query(FundsContentProvider.mUriPortfolio, null, null, null, null);
                    if (cursorPortfolio.moveToFirst()) {
                        do {
                            String scode = cursorPortfolio.getString(
                                    cursorPortfolio.getColumnIndex(FundsContentProvider.FUND_SCODE));
                            Uri uriFullFunds = Uri.parse(FundsContentProvider.mUriFullFundsList.toString() + "/" + scode);
                            Cursor cursor = mContext.getContentResolver().query(uriFullFunds, null, null, null, null);
                            cursor.moveToFirst();
                            String lastUpdated = cursor.getString(cursor.getColumnIndex(FundsContentProvider.LAST_UPDATED_NAV));
                            String nav = cursor.getString(cursor.getColumnIndex(FundsContentProvider.NAV));
                            Uri uriPortfolio = Uri.parse(FundsContentProvider.mUriPortfolio.toString() + "/" + scode);
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(FundsContentProvider.LAST_UPDATED_NAV, lastUpdated);
                            mContext.getContentResolver().update(uriPortfolio, contentValues, null, null);
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference(mFirebaseUser.getUid())
                                    .child(mContext.getString(R.string.firebase_child_funds));
                            myRef.child(scode).child(mContext.getString(R.string.key_fund_nav)).setValue(nav);
                            myRef.child(scode).child(mContext.getString(R.string.last_updated_string)).setValue(lastUpdated);
                        } while (cursorPortfolio.moveToNext());
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
            if (toBeUpdated) {
                showNotification();
            }
        } else if (mTaskParamTag.equals(mContext.getString(R.string.tag_insert_scodes))) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(FundsContentProvider.FUND_SCODE,
                    taskParams.getExtras().getString(mContext.getString(R.string.key_scode)));
            contentValues.put(FundsContentProvider.LAST_UPDATED_NAV,
                    taskParams.getExtras().getString(mContext.getString(R.string.key_last_updated_nav)));
            mContext.getContentResolver().insert(FundsContentProvider.mUriPortfolio, contentValues);
        } else if (mTaskParamTag.equals(mContext.getString(R.string.tag_download_data))) {
            ResultReceiver receiver = (ResultReceiver) taskParams.getExtras().getParcelable(
                    mContext.getString(R.string.key_download_progress_receiver));
            try {
                URL url = new URL(TEXT_FILE_URL);
                URLConnection connection = url.openConnection();
                connection.connect();
                String fileLengthStr = connection.getHeaderField("content-length");
                int fileLength = 14000;
                Log.d(TAG, "Length =" + fileLengthStr);
                InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                String[] tokens = new String[8];
                int i;
                bufferedReader.readLine();
                int lineCount = 0;
                Bundle resultData = new Bundle();
                while ((line = bufferedReader.readLine()) != null) {
                    lineCount++;
                    StringTokenizer tokenizer = splitOnSemiColon(line);
                    if (tokenizer.countTokens() == 8) {
                        i = 0;
                        while (i < 8) {
                            tokens[i] = tokenizer.nextToken();
                            i++;
                        }
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(FundsContentProvider.FUND_SCODE, tokens[0]);
                        contentValues.put(FundsContentProvider.FUND_NAME, tokens[3]);
                        contentValues.put(FundsContentProvider.NAV, tokens[4]);
                        contentValues.put(FundsContentProvider.LAST_UPDATED_NAV, tokens[7]);
                        mContext.getContentResolver().insert(FundsContentProvider.mUriFullFundsList, contentValues);

                        int progress = (int) (lineCount * 100 / fileLength);
                        if (!(progress > 99)) {
                            resultData.putInt(mContext.getString(R.string.key_download_progress), progress);
                            receiver.send(DOWNLOAD_PROGRESS, resultData);
                        }
                    }
                }
                resultData.putInt(mContext.getString(R.string.key_download_progress), 100);
                receiver.send(DOWNLOAD_PROGRESS, resultData);
                inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        } else if (mTaskParamTag.equals(mContext.getString(R.string.tag_add_fund))) {
            String scode = taskParams.getExtras().getString(mContext.getString(R.string.key_scode));
            Uri uriPortfolio = Uri.parse(FundsContentProvider.mUriPortfolio.toString() + "/" + scode);
            Cursor cursorPortfolio = mContext.getContentResolver().query(uriPortfolio, null, null, null, null);
            if (!cursorPortfolio.moveToFirst()) {
                Uri uri = Uri.parse(FundsContentProvider.mUriFullFundsList.toString() + "/" + scode);
                Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
                if (cursor.moveToFirst()) {
                    storeDataInFirebase(cursor);
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(FundsContentProvider.FUND_SCODE,
                            cursor.getString(cursor.getColumnIndex(FundsContentProvider.FUND_SCODE)));
                    contentValues.put(FundsContentProvider.LAST_UPDATED_NAV,
                            cursor.getString(cursor.getColumnIndex(FundsContentProvider.LAST_UPDATED_NAV)));
                    mContext.getContentResolver().insert(FundsContentProvider.mUriPortfolio, contentValues);
                }

            } else {
                sendToast(mContext.getString(R.string.unique_constraint_failed_message));
            }
        }
        return 0;
    }

    public boolean toBeUpdated(String lastUpdated) {
        Uri uriPortfolio = Uri.parse(FundsContentProvider.mUriPortfolio.toString() + "/_id/1");
        Cursor cursor = mContext.getContentResolver().query(uriPortfolio, null, null, null, null);
        if (cursor.moveToFirst()) {
            String localDate = cursor.getString(cursor.getColumnIndex(FundsContentProvider.LAST_UPDATED_NAV));
            if (!lastUpdated.equals(localDate)) {
                return true;
            }
        }
        return false;
    }

    public StringTokenizer splitOnSemiColon(String splitStr) {
        return new StringTokenizer(splitStr, ";");
    }

    public void sendToast(String message) {
        Intent intent = new Intent();
        intent.setAction(mContext.getString(R.string.gcmtask_intent));
        intent.putExtra(mContext.getString(R.string.key_toast_message),
                message);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    public void storeDataInFirebase(Cursor cursor) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(mFirebaseUser.getUid())
                .child(mContext.getString(R.string.firebase_child_funds));
        String scode = cursor.getString(cursor.getColumnIndex(FundsContentProvider.FUND_SCODE));
        String fundName = cursor.getString(cursor.getColumnIndex(FundsContentProvider.FUND_NAME));
        String nav = cursor.getString(cursor.getColumnIndex(FundsContentProvider.NAV));
        String lastUpdatedDate = cursor.getString(cursor.getColumnIndex(FundsContentProvider.LAST_UPDATED_NAV));
        if (mTaskParamTag.equals(mContext.getString(R.string.tag_add_fund))) {
            fundName = fundName.toLowerCase();
            String[] words = fundName.split("[ /-]");
            String formattedFundName = words[0].toUpperCase();
            for (int i = 1; i < words.length; i++) {
                if (words[i].length() > 1) {
                    String firstLetter = words[i].substring(0, 1).toUpperCase();
                    words[i] = firstLetter + words[i].substring(1, words[i].length());
                    formattedFundName = formattedFundName + " " + words[i];
                }
            }

            myRef.child(scode).setValue(new FundInfo(
                    scode, formattedFundName, nav, "0", "0"/*changeValue*/,
                    "0%"/*changePercent*/, lastUpdatedDate).toMap());
            sendToast(mContext.getString(R.string.fund_added_message));
        }

    }

    public void showNotification() {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long[] v = {100, 200};

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle(mContext.getString(R.string.notification_title))
                        .setContentText(mContext.getString(R.string.notification_content));

        Intent resultIntent = new Intent(mContext, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        builder.setContentIntent(resultPendingIntent);
        builder.setSound(uri);
        builder.setVibrate(v);
        builder.setColor(mContext.getResources().getColor(R.color.colorPrimary));
        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Integer.parseInt(
                mContext.getString(R.string.notification_id)), builder.build());
    }
}
