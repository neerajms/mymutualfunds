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
import android.util.Log;

import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.neerajms99b.neeraj.mymutualfunds.BuildConfig;
import com.neerajms99b.neeraj.mymutualfunds.R;
import com.neerajms99b.neeraj.mymutualfunds.data.FundsContentProvider;
import com.neerajms99b.neeraj.mymutualfunds.models.BasicFundInfoParcelable;
import com.neerajms99b.neeraj.mymutualfunds.models.FundInfo;
import com.neerajms99b.neeraj.mymutualfunds.ui.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

/**
 * Created by neeraj on 8/8/16.
 */
public class FetchFundsTask extends GcmTaskService {
    private final String FUNDS_BASE_URL = "https://mutualfundsnav.p.mashape.com/";
    private final String KEY_PARAM = "X-Mashape-Key";
    private final String CONTENT_TYPE_PARAM = "Content-Type";
    private final String ACCEPT_PARAM = "Accept";
    private final String CONTENT_TYPE_VALUE = "application/json";
    private final String ACCEPT_VALUE = "application/json";
    private final String KEY_VALUE = BuildConfig.API_KEY;
    private final String TAG = getClass().getSimpleName();
    private final String KEY_FUNDNAME = "fund";
    private final String KEY_NAV = "nav";
    private final String KEY_SCODE = "scode";
    private final String KEY_CHANGE = "change";
    private final String KEY_CHANGE_VALUE = "value";
    private final String KEY_CHANGE_PERCENT = "percent";
    private final String KEY_QUARTER = "q";
    private Context mContext;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private ArrayList<String> mFundsScodesArrayList;
    private boolean mIsUpdateSuccessful;

    public FetchFundsTask() {
    }

    public FetchFundsTask(Context context) {
        mContext = context;
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mContext == null) {
            mContext = this;
        }
        if (taskParams.getTag().equals(mContext.getString(R.string.tag_search_fund))) {
            HttpResponse<JsonNode> response;
            ArrayList<BasicFundInfoParcelable> fundsArrayList = new ArrayList<BasicFundInfoParcelable>();
            String fundName = taskParams.getExtras().getString(
                    mContext.getString(R.string.key_fund_search_word));
            String query = "{\"search\":\"" + fundName + "\"}";
            try {
                response = Unirest.post(FUNDS_BASE_URL)
                        .header(KEY_PARAM, KEY_VALUE)
                        .header(CONTENT_TYPE_PARAM, CONTENT_TYPE_VALUE)
                        .header(ACCEPT_PARAM, ACCEPT_VALUE)
                        .body(query)
                        .asJson();
                if (response.getBody().toString().equals("[]")) {
                    throw new UnirestException(mContext.getString(R.string.message_fund_not_found));
                }


                try {
                    JsonNode jsonNodeHttpResponse = response.getBody();
                    JSONArray jsonArray = jsonNodeHttpResponse.getArray();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONArray innerJsonArray = jsonArray.getJSONArray(i);
                        fundsArrayList.add(new BasicFundInfoParcelable(innerJsonArray.getString(0),
                                innerJsonArray.getString(3)));
                        Log.e(TAG, innerJsonArray.getString(3));
                    }

                } catch (JSONException e) {
                    sendToast(mContext.getString(R.string.message_something_went_wrong));
                    Log.e(TAG, e.toString());
                }
            } catch (UnirestException e) {
                if (e.getMessage().equals(mContext.getString(R.string.message_fund_not_found))) {
                    sendToast(mContext.getString(R.string.message_fund_not_found));
                } else {
                    sendToast(mContext.getString(R.string.message_something_went_wrong));
                }
                Log.e(TAG, e.toString());
            }

            if (fundsArrayList.size() > 0) {
                Bundle dataBundle = new Bundle();
                dataBundle.putParcelableArrayList(mContext.getString(R.string.basic_search_results_parcelable), fundsArrayList);
                Intent intent = new Intent();
                intent.setAction(mContext.getString(R.string.gcmtask_intent));
                intent.putExtra(mContext.getString(R.string.search_data_bundle), dataBundle);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        } else if (taskParams.getTag().equals(mContext.getString(R.string.tag_search_scode))) {
            HttpResponse<JsonNode> response;
            String fundName = null;
            String nav = null;
            String changeValue = null;
            String changePercent = null;
            String scode = taskParams.getExtras().getString(mContext.getString(R.string.key_scode));
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference(mFirebaseUser.getUid());
            ContentValues initial = new ContentValues();
            initial.put(FundsContentProvider.FUND_SCODE, scode);
            Uri resultUri = mContext.getContentResolver().insert(FundsContentProvider.mUriHistorical, initial);

            if (resultUri == null) {
                sendToast(mContext.getString(R.string.unique_constraint_failed_message));
            } else {
                String query = "{\"scodes\":[\"" + scode + "\"]}";
                try {
                    response = Unirest.post(FUNDS_BASE_URL)
                            .header(KEY_PARAM, KEY_VALUE)
                            .header(CONTENT_TYPE_PARAM, CONTENT_TYPE_VALUE)
                            .header(ACCEPT_PARAM, ACCEPT_VALUE)
                            .body(query)
                            .asJson();
                    Log.d(TAG, response.toString());
                    JsonNode jsonNodeHttpResponse = response.getBody();
                    Log.d(TAG, jsonNodeHttpResponse.toString());
                    try {
                        JSONObject jsonObject = jsonNodeHttpResponse.getObject();
                        JSONObject jsonObject1 = jsonObject.getJSONObject(scode);
                        fundName = jsonObject1.getString(KEY_FUNDNAME);
                        nav = String.format("%.2f", Float.parseFloat(jsonObject1.getString(KEY_NAV)));
                        JSONObject jsonObject2 = jsonObject1.getJSONObject(KEY_CHANGE);
                        changeValue = jsonObject2.getString(KEY_CHANGE_VALUE);
                        changePercent = jsonObject2.getString(KEY_CHANGE_PERCENT);
                        Log.d(TAG, nav);
                    } catch (JSONException je) {
                        Log.d(TAG, je.toString());
                    }
                } catch (UnirestException ue) {
                    Log.d(TAG, ue.toString());
                }
                if (fundName != null && nav != null && changePercent != null && changeValue != null) {
                    String units = "0";
                    FundInfo info = new FundInfo(scode, fundName, nav, units, changeValue, changePercent);
                    Map<String, Object> fund = info.toMap();
                    myRef.child(mContext.getString(R.string.firebase_child_funds)).child(scode).setValue(fund);
                    sendToast(mContext.getString(R.string.fund_added_message));
//                    showNotification();
                } else {
                    sendToast(mContext.getString(R.string.message_failed_to_add_fund));
                    Uri uriDelete = Uri.parse(FundsContentProvider.mUriHistorical.toString() +
                            "/" + scode);
                    mContext.getContentResolver().delete(uriDelete, null, null);
                }
            }
        } else if (taskParams.getTag().equals(mContext.getResources().getString(R.string.tag_update_nav))) {
            mIsUpdateSuccessful = false;
            Log.e("fetchfundstask", "executed");
            Cursor cursor = mContext.getContentResolver().query(FundsContentProvider.mUriHistorical,
                    new String[]{FundsContentProvider.FUND_SCODE}, null, null, null);
            if (cursor.moveToFirst()) {
                int index = 0;
                String scodes = "\"scodes\":[";
                ArrayList<String> scodesArrayList = new ArrayList<>();
                do {
                    String scode = cursor.getString(
                            cursor.getColumnIndex(FundsContentProvider.FUND_SCODE));
                    scodes = scodes + "\"" + scode + "\"" + ",";
                    scodesArrayList.add(scode);
                    if (index == 4) {
                        scodes = scodes + "\"" + cursor.getString(
                                cursor.getColumnIndex(FundsContentProvider.FUND_SCODE)) + "\"";
                        fetchFundInfoFromApi(scodes, scodesArrayList);
                        scodes = "\"scodes\":[";
                        scodesArrayList.clear();
                    }
                    index++;
                    if (index == 5) {
                        index = 0;
                    }
                } while (cursor.moveToNext());
                if (scodesArrayList.size() != 0) {
                    scodes = scodes.substring(0, scodes.length() - 1);
                    fetchFundInfoFromApi(scodes, scodesArrayList);
                }
            }
            cursor.close();
            if (mIsUpdateSuccessful) {
                showNotification();
            }
        } else if (taskParams.getTag().equals(mContext.getString(R.string.tag_fetch_graph_data))) {
            String scode = taskParams.getExtras().getString(mContext.getString(R.string.key_scode));
            Calendar date = Calendar.getInstance();
            SimpleDateFormat dateFormatYear = new SimpleDateFormat("yyyy");
            SimpleDateFormat dateFormatMonth = new SimpleDateFormat("MM");
            String yearString = dateFormatYear.format(date.getTime());
            String monthString = dateFormatMonth.format(date.getTime());
            int currentQuarter = Integer.valueOf(monthString) / 4;
            int monthInt = 0;
            int yearInt = Integer.valueOf(yearString);
            Log.d(TAG, String.valueOf(currentQuarter));
            switch (currentQuarter) {
                case 0:
                    monthInt = 12;
                    yearInt = yearInt - 1;
                    break;
                case 1:
                    monthInt = 3;
                    break;
                case 2:
                    monthInt = 6;
                    break;
                default:
                    monthInt = 9;
            }
            Uri uri = Uri.parse(FundsContentProvider.mUriHistorical.toString() + "/" + scode);
            int year = yearInt;
            int quarter = 12;
            while (year >= yearInt - 2) {
                while (monthInt >= 3) {
                    String day = "01";
                    if (monthInt == 12 || monthInt == 3) {
                        day = "31";
                    } else if (monthInt == 6 || monthInt == 9) {
                        day = "30";
                    }
                    String month = String.format("%02d", monthInt);

                    String dateFull = day + "/" + month + "/" + String.valueOf(year);
                    Log.d(TAG, dateFull);
                    String value = getGraph(scode, dateFull);
                    if (value != null) {
                        ContentValues navForQuarter = new ContentValues();
                        navForQuarter.put(KEY_QUARTER + quarter, value);
                        mContext.getContentResolver().update(uri, navForQuarter, null, null);
                    } else {
                        Log.d(TAG, dateFull);
                        value = getGraph(scode, dateFull);
                        if (value != null) {
                            ContentValues navForQuarter = new ContentValues();
                            navForQuarter.put(KEY_QUARTER + quarter, value);
                            mContext.getContentResolver().update(uri, navForQuarter, null, null);
                        } else {
                            if (day.equals("31")) {
                                day = "29";
                            } else {
                                day = "28";
                            }
                            dateFull = day + "/" + month + "/" + String.valueOf(year);
                            value = getGraph(scode, dateFull);
                            if (value != null) {
                                ContentValues navForQuarter = new ContentValues();
                                navForQuarter.put(KEY_QUARTER + quarter, value);
                                mContext.getContentResolver().update(uri, navForQuarter, null, null);
                            }
                        }
                    }
                    quarter--;
                    monthInt = monthInt - 3;
                }
                year--;
                monthInt = 12;
            }
            Intent intent = new Intent();
            intent.setAction(mContext.getString(R.string.gcmtask_intent));
            intent.putExtra(mContext.getString(R.string.key_graph_fetched), true);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        } else if (taskParams.getTag().equals(mContext.getString(R.string.tag_insert_scodes))) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(FundsContentProvider.FUND_SCODE,
                    taskParams.getExtras().getString(mContext.getString(R.string.key_scode)));
            mContext.getContentResolver().insert(FundsContentProvider.mUriHistorical, contentValues);
        }
        return 0;
    }

    public void sendToast(String message) {
        Intent intent = new Intent();
        intent.setAction(mContext.getString(R.string.gcmtask_intent));
        intent.putExtra(mContext.getString(R.string.key_toast_message),
                message);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    public String getGraph(String scode, String date) {
        String nav = null;
        Log.e(TAG, scode);
        try {
            String requestBody = "{\"scode\":" + scode + ",\"date\":" + "\"" + date + "\"" + "}";
            HttpResponse<JsonNode> response = Unirest.post("https://mutualfundsnav.p.mashape.com/historical")
                    .header(KEY_PARAM, KEY_VALUE)
                    .header(CONTENT_TYPE_PARAM, CONTENT_TYPE_VALUE)
                    .header(ACCEPT_PARAM, ACCEPT_VALUE)
                    .body(requestBody)
                    .asJson();
            Log.d(TAG, response.getBody().toString());
            JsonNode jsonNode = response.getBody();
            try {
                JSONObject jsonObject = jsonNode.getObject();
                nav = jsonObject.getString(KEY_NAV);
            } catch (JSONException je) {
                Log.d(TAG, je.toString());
            }
        } catch (UnirestException ue) {
            Log.d(TAG, ue.toString());
        }
        return nav;
    }

    public void fetchFundInfoFromApi(String scodes, ArrayList<String> scodesArrayList) {
        String query = "{" + scodes + "]}";
        try {
            HttpResponse<JsonNode> response = Unirest.post(FUNDS_BASE_URL)
                    .header(KEY_PARAM, KEY_VALUE)
                    .header(CONTENT_TYPE_PARAM, CONTENT_TYPE_VALUE)
                    .header(ACCEPT_PARAM, ACCEPT_VALUE)
                    .body(query)
                    .asJson();
            JsonNode jsonNode = response.getBody();
            Log.e(TAG, query.toString());
            JSONObject jsonObject = jsonNode.getObject();
            for (int index = 0; index < scodesArrayList.size(); index++) {
                extractInfoFromJson(scodesArrayList.get(index), jsonObject);
            }
        } catch (UnirestException ue) {
            Log.e(TAG, ue.toString());
            mIsUpdateSuccessful = false;
            retriggerTask();
        }
    }

    public void retriggerTask() {
        Intent intent = new Intent(mContext, Alarm.class);
        intent.putExtra(mContext.getString(R.string.key_tag), mContext.getString(R.string.retrigger_update_nav));
        mContext.sendBroadcast(intent);
    }

    public void extractInfoFromJson(String scode, JSONObject object) {
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference(mFirebaseUser.getUid())
                    .child(mContext.getString(R.string.firebase_child_funds)).child(scode);
            JSONObject jsonObject = object.getJSONObject(scode);
            String nav = jsonObject.getString(KEY_NAV);
            JSONObject jsonObject1 = jsonObject.getJSONObject(KEY_CHANGE);
            String changePercent = jsonObject1.getString(KEY_CHANGE_PERCENT);
            String changeValue = jsonObject1.getString(KEY_CHANGE_VALUE);
            Log.e(TAG, nav + " " + changePercent + " " + changeValue);
            Cursor cursor = mContext.getContentResolver().query(FundsContentProvider.mUriHistorical,
                    new String[]{FundsContentProvider.FUND_SCODE}, null, null, null);
            if (cursor.moveToFirst()) {
                myRef.child(mContext.getString(R.string.key_fund_nav)).setValue(nav);
                myRef.child(mContext.getString(R.string.key_change_percent)).setValue(changePercent);
                myRef.child(mContext.getString(R.string.key_change_value)).setValue(changeValue);
            }
            mIsUpdateSuccessful = true;
        } catch (JSONException je) {
            Log.e(TAG, je.toString());
        }
    }

    public void showNotification() {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long[] v = {100,200};

        NotificationCompat.Builder mBuilder =
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

        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setSound(uri);
        mBuilder.setVibrate(v);
        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Integer.parseInt(
                mContext.getString(R.string.notification_id)), mBuilder.build());
    }
}
