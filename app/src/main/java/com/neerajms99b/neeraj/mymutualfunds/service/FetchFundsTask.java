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

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.neerajms99b.neeraj.mymutualfunds.BuildConfig;
import com.neerajms99b.neeraj.mymutualfunds.R;
import com.neerajms99b.neeraj.mymutualfunds.data.FundsContentProvider;
import com.neerajms99b.neeraj.mymutualfunds.models.BasicFundInfoParcelable;
import com.neerajms99b.neeraj.mymutualfunds.request.CustomRequest;
import com.neerajms99b.neeraj.mymutualfunds.ui.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by neeraj on 8/8/16.
 */
public class FetchFundsTask extends GcmTaskService {
    private final String TAG = getClass().getSimpleName();
    private final String FUNDS_BASE_URL = "https://mutualfundsnav.p.mashape.com/";
    private final String FUNDS_HISTORICAL_URL = "https://mutualfundsnav.p.mashape.com/historical";
    private final String KEY_PARAM = "X-Mashape-Key";
    private final String CONTENT_TYPE_PARAM = "Content-Type";
    private final String ACCEPT_PARAM = "Accept";
    private final String CONTENT_TYPE_VALUE = "application/json";
    private final String ACCEPT_VALUE = "application/json";
    private final String KEY_VALUE = BuildConfig.API_KEY;
    private final String KEY_FUNDNAME = "fund";
    private final String KEY_NAV = "nav";
    private final String KEY_CHANGE = "change";
    private final String KEY_CHANGE_VALUE = "value";
    private final String KEY_CHANGE_PERCENT = "percent";
    private final String KEY_QUARTER = "q";
    private final String KEY_SCODE = "scode";

    private Context mContext;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mDatabase;
    private ArrayList<String> mFundsScodesArrayList;
    private boolean mIsUpdateSuccessful;
    private RequestQueue mRequestQueue;
    private String mTaskParamTag;

    public FetchFundsTask() {
    }

    public FetchFundsTask(Context context) {
        mContext = context;
        mRequestQueue = Volley.newRequestQueue(mContext);
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        mTaskParamTag = taskParams.getTag();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        if (mContext == null) {
            mContext = this;
        }
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext);
        }

        if (mTaskParamTag.equals(mContext.getString(R.string.tag_search_fund))) {
            ArrayList<BasicFundInfoParcelable> fundsArrayList = new ArrayList<BasicFundInfoParcelable>();
            String fundName = taskParams.getExtras().getString(
                    mContext.getString(R.string.key_fund_search_word));
            String query = "{\"search\":\"" + fundName + "\"}";
            volleyRequestJsonArray(FUNDS_BASE_URL, query);
        } else if (mTaskParamTag.equals(mContext.getString(R.string.tag_search_scode))) {
            ArrayList<String> scodesArrayList = new ArrayList<>();
            String scode = taskParams.getExtras().getString(mContext.getString(R.string.key_scode));
            ContentValues initial = new ContentValues();
            initial.put(FundsContentProvider.FUND_SCODE, scode);
            Uri resultUri = mContext.getContentResolver().insert(FundsContentProvider.mUriHistorical, initial);

            if (resultUri == null) {
                sendToast(mContext.getString(R.string.unique_constraint_failed_message));
            } else {
                scodesArrayList.add(scode);
                String scodes = "\"scodes\":[" + "\"" + scode + "\"";
                String query = "{" + scodes + "]}";
                volleyRequestJsonObject(FUNDS_BASE_URL, query, scodesArrayList, null);
            }
        } else if (mTaskParamTag.equals(mContext.getResources().getString(R.string.tag_update_nav))) {
            Log.d(TAG, "Auto update triggered");
            mIsUpdateSuccessful = false;
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
                        String query = "{" + scodes + "]}";
                        volleySynchronous(FUNDS_BASE_URL, query, null, scodesArrayList);
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
                    String query = "{" + scodes + "]}";
                    volleySynchronous(FUNDS_BASE_URL, query, null, scodesArrayList);
                }
            }
            cursor.close();
            if (mIsUpdateSuccessful) {
                showNotification();
            }
        } else if (mTaskParamTag.equals(mContext.getString(R.string.tag_fetch_graph_data))) {
            ArrayList<String> scodeArrayList = new ArrayList<>();
            String scode = taskParams.getExtras().getString(mContext.getString(R.string.key_scode));
            scodeArrayList.add(scode);
            Calendar date = Calendar.getInstance();
            SimpleDateFormat dateFormatYear = new SimpleDateFormat("yyyy");
            SimpleDateFormat dateFormatMonth = new SimpleDateFormat("MM");
            String yearString = dateFormatYear.format(date.getTime());
            String monthString = dateFormatMonth.format(date.getTime());
            int currentQuarter = Integer.valueOf(monthString) / 4;
            int monthInt = 0;
            int yearInt = Integer.valueOf(yearString);
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
                    String requestBody = "{\"scode\":" + scode + ",\"date\":" + "\"" + dateFull + "\"" + "}";
                    String value = volleySynchronous(FUNDS_HISTORICAL_URL,
                            requestBody, String.valueOf(quarter), scodeArrayList);
                    if (value == null) {
                        value = volleySynchronous(FUNDS_HISTORICAL_URL,
                                requestBody, String.valueOf(quarter), scodeArrayList);
                        if (value == null) {
                            if (day.equals("31")) {
                                day = "29";
                            } else if (day.equals("30")) {
                                day = "28";
                            }
                            dateFull = day + "/" + month + "/" + String.valueOf(year);
                            requestBody = "{\"scode\":" + scode + ",\"date\":" + "\"" + dateFull + "\"" + "}";
                            volleySynchronous(FUNDS_HISTORICAL_URL,
                                    requestBody, String.valueOf(quarter), scodeArrayList);
                        }
                    }
                    quarter--;
                    monthInt = monthInt - 3;
                }
                year--;
                monthInt = 12;
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(FundsContentProvider.LAST_UPDATED,
                    String.valueOf(currentQuarter) + "-" + yearString);
            mContext.getContentResolver().update(uri, contentValues, null, null);
            Intent intent = new Intent();
            intent.setAction(mContext.getString(R.string.gcmtask_intent));
            intent.putExtra(mContext.getString(R.string.key_graph_fetched), true);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        } else if (mTaskParamTag.equals(mContext.getString(R.string.tag_insert_scodes))) {
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
            String fundName = jsonObject.getString(KEY_FUNDNAME);
            String nav = jsonObject.getString(KEY_NAV);
            JSONObject jsonObject1 = jsonObject.getJSONObject(KEY_CHANGE);
            String changePercent = jsonObject1.getString(KEY_CHANGE_PERCENT);
            String changeValue = jsonObject1.getString(KEY_CHANGE_VALUE);
            Cursor cursor = mContext.getContentResolver().query(FundsContentProvider.mUriHistorical,
                    new String[]{FundsContentProvider.FUND_SCODE}, null, null, null);
            if (fundName != null && nav != null && changePercent != null && changeValue != null) {
                if (cursor.moveToFirst()) {
                    Log.d(TAG, fundName);
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
                    myRef.child(mContext.getString(R.string.key_fundname)).setValue(formattedFundName);
                    myRef.child(mContext.getString(R.string.key_scode)).setValue(scode);
                    myRef.child(mContext.getString(R.string.key_fund_nav)).setValue(nav);
                    myRef.child(mContext.getString(R.string.key_change_percent)).setValue(changePercent);
                    myRef.child(mContext.getString(R.string.key_change_value)).setValue(changeValue);
                    if (mTaskParamTag.equals(mContext.getString(R.string.tag_search_scode))) {
                        myRef.child(mContext.getString(R.string.key_units_in_hand)).setValue("0");
                    }
                    mIsUpdateSuccessful = true;
                    if (mTaskParamTag.equals(mContext.getString(R.string.tag_search_scode))) {
                        sendToast(mContext.getString(R.string.fund_added_message));
                    }
                } else {
                    if (mTaskParamTag.equals(mContext.getString(R.string.tag_search_scode))) {

                        sendToast(mContext.getString(R.string.message_failed_to_add_fund));
                        Uri uriDelete = Uri.parse(FundsContentProvider.mUriHistorical.toString() +
                                "/" + scode);
                        mContext.getContentResolver().delete(uriDelete, null, null);
                    }
                }
            }
        } catch (JSONException je) {
            Log.e(TAG, je.toString());
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


    public void volleyRequestJsonArray(String url, String body) {

        try {
            JSONObject jsonObject = new JSONObject(body);
            CustomRequest request = new CustomRequest(Request.Method.POST,
                    url, jsonObject, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    Log.d(TAG, response.toString());
                    processSearchResponse(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error.toString().equals("com.android.volley.AuthFailureError") ||
                            error.toString().equals("com.android.volley.ServerError")) {
                        sendToast(mContext.getString(R.string.message_something_went_wrong));
                    } else {
                        sendToast(mContext.getString(R.string.message_fund_not_found));
                    }
                    Log.e(TAG, error.toString());
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> header = new HashMap<>();
                    header.put(KEY_PARAM, KEY_VALUE);
                    header.put(CONTENT_TYPE_PARAM, CONTENT_TYPE_VALUE);
                    header.put(ACCEPT_PARAM, ACCEPT_VALUE);
                    return header;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> header = new HashMap<>();
                    header.put(KEY_PARAM, KEY_VALUE);
                    header.put(CONTENT_TYPE_PARAM, CONTENT_TYPE_VALUE);
                    header.put(ACCEPT_PARAM, ACCEPT_VALUE);
                    return header;
                }
            };
            request.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 1, 0.0f));
            mRequestQueue.add(request);
        } catch (JSONException je) {
            Log.e(TAG, je.toString());
        }
    }

    public void volleyRequestJsonObject(String url, String body, final ArrayList<String> scodesArrayList, final String quarter) {
        try {
            JSONObject jsonBody = new JSONObject(body);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                    url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, response.toString());

                    if (mTaskParamTag.equals(mContext.getString(R.string.tag_update_nav)) ||
                            mTaskParamTag.equals(mContext.getString(R.string.tag_search_scode))) {
                        processFundDetailsJson(scodesArrayList, response);
                    } else if (mTaskParamTag.equals(mContext.getString(R.string.tag_fetch_graph_data))) {
                        processGraphData(response, quarter);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, error.toString());
                    if (mTaskParamTag.equals(mContext.getString(R.string.tag_search_scode))
                            && scodesArrayList.size() > 0) {
                        Uri uriDelete = Uri.parse(FundsContentProvider.mUriHistorical.toString() +
                                "/" + scodesArrayList.get(0));
                        mContext.getContentResolver().delete(uriDelete, null, null);
                        sendToast(mContext.getString(R.string.message_failed_to_add_fund));
                    } else if (mTaskParamTag.equals(mContext.getString(R.string.tag_update_nav))) {
                        mIsUpdateSuccessful = false;
                        retriggerTask();
                    }
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> header = new HashMap<>();
                    header.put(KEY_PARAM, KEY_VALUE);
                    header.put(CONTENT_TYPE_PARAM, CONTENT_TYPE_VALUE);
                    header.put(ACCEPT_PARAM, ACCEPT_VALUE);
                    return header;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> header = new HashMap<>();
                    header.put(KEY_PARAM, KEY_VALUE);
                    header.put(CONTENT_TYPE_PARAM, CONTENT_TYPE_VALUE);
                    header.put(ACCEPT_PARAM, ACCEPT_VALUE);
                    return header;
                }
            };
            request.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 1, 0.0f));
            mRequestQueue.add(request);
        } catch (JSONException je) {
            Log.e(TAG, je.toString());
        }
    }

    public void processSearchResponse(JSONArray jsonArray) {
        ArrayList<BasicFundInfoParcelable> fundsArrayList = new ArrayList<BasicFundInfoParcelable>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray innerJsonArray = jsonArray.getJSONArray(i);
                fundsArrayList.add(new BasicFundInfoParcelable(innerJsonArray.getString(0),
                        innerJsonArray.getString(3)));
            }

        } catch (JSONException e) {
            sendToast(mContext.getString(R.string.message_something_went_wrong));
            Log.e(TAG, e.toString());
        }
        if (fundsArrayList.size() > 0) {
            Bundle dataBundle = new Bundle();
            dataBundle.putParcelableArrayList(mContext.getString(R.string.basic_search_results_parcelable), fundsArrayList);
            Intent intent = new Intent();
            intent.setAction(mContext.getString(R.string.gcmtask_intent));
            intent.putExtra(mContext.getString(R.string.search_data_bundle), dataBundle);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        } else {
            sendToast(mContext.getString(R.string.message_fund_not_found));
        }
    }

    public void processFundDetailsJson(ArrayList<String> scodesArrayList, JSONObject jsonObject) {
        for (int index = 0; index < scodesArrayList.size(); index++) {
            extractInfoFromJson(scodesArrayList.get(index), jsonObject);
        }
    }

    public String processGraphData(JSONObject jsonObject, String quarter) {
        String nav = null;
        try {
            String scode = jsonObject.getString(KEY_SCODE);
            nav = jsonObject.getString(KEY_NAV);
            Uri uri = Uri.parse(FundsContentProvider.mUriHistorical.toString() + "/" + scode);
            if (nav != null && scode != null) {
                ContentValues navForQuarter = new ContentValues();
                navForQuarter.put(KEY_QUARTER + quarter, nav);
                mContext.getContentResolver().update(uri, navForQuarter, null, null);
            }
        } catch (JSONException je) {
            Log.e(TAG, je.toString());
        }
        return nav;
    }

    public String volleySynchronous(String url, String body, String quarter, final ArrayList<String> scodesArrayList) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody = new JSONObject(body);
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                url, jsonBody, future, future) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put(KEY_PARAM, KEY_VALUE);
                header.put(CONTENT_TYPE_PARAM, CONTENT_TYPE_VALUE);
                header.put(ACCEPT_PARAM, ACCEPT_VALUE);
                return header;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put(KEY_PARAM, KEY_VALUE);
                header.put(CONTENT_TYPE_PARAM, CONTENT_TYPE_VALUE);
                header.put(ACCEPT_PARAM, ACCEPT_VALUE);
                return header;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 1, 0.0f));
        mRequestQueue.add(request);
        try {
            JSONObject response = future.get(10, TimeUnit.SECONDS); // Blocks for at most 10 seconds.
            Log.d(TAG, response.toString());
            if (mTaskParamTag.equals(mContext.getString(R.string.tag_fetch_graph_data))) {
                return processGraphData(response, quarter);
            } else if (mTaskParamTag.equals(mContext.getString(R.string.tag_update_nav))) {
                processFundDetailsJson(scodesArrayList, response);
            }
        } catch (InterruptedException e) {
            // Exception handling
            if (mTaskParamTag.equals(mContext.getString(R.string.tag_update_nav))) {
                mIsUpdateSuccessful = false;
                retriggerTask();
            }
            Log.e(TAG, e.toString());
        } catch (ExecutionException e) {
            if (mTaskParamTag.equals(mContext.getString(R.string.tag_update_nav))) {
                mIsUpdateSuccessful = false;
                retriggerTask();
            }
            // Exception handling
            Log.e(TAG, e.toString());
        } catch (TimeoutException e) {
            if (mTaskParamTag.equals(mContext.getString(R.string.tag_update_nav))) {
                mIsUpdateSuccessful = false;
                retriggerTask();
            }
            Log.e(TAG, e.toString());
        }
        return null;
    }
}
