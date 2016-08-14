package com.neerajms99b.neeraj.mymutualfunds.service;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.neerajms99b.neeraj.mymutualfunds.R;
import com.neerajms99b.neeraj.mymutualfunds.data.BasicFundInfoParcelable;
import com.neerajms99b.neeraj.mymutualfunds.data.FundsContentProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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
    private final String KEY_VALUE = "My API Key";
    private final String TAG = getClass().getSimpleName();
    private final String KEY_FUNDNAME = "fund";
    private final String KEY_NAV = "nav";
    private final String KEY_SCODE = "scode";
    private Context mContext;

    public FetchFundsTask() {
    }

    public FetchFundsTask(Context context) {
        mContext = context;
    }

    @Override
    public int onRunTask(TaskParams taskParams) {

        if (taskParams.getTag().equals("force")) {
            HttpResponse<JsonNode> response;
            ArrayList<BasicFundInfoParcelable> fundsArrayList = new ArrayList<BasicFundInfoParcelable>();
            String fundName = taskParams.getExtras().getString(KEY_FUNDNAME);
            String query = "{\"search\":\"" + fundName + "\"}";
            try {
                response = Unirest.post(FUNDS_BASE_URL)
                        .header(KEY_PARAM, KEY_VALUE)
                        .header(CONTENT_TYPE_PARAM, CONTENT_TYPE_VALUE)
                        .header(ACCEPT_PARAM, ACCEPT_VALUE)
                        .body(query)
                        .asJson();
                JsonNode jsonNodeHttpResponse = response.getBody();
                try {
                    JSONArray jsonArray = jsonNodeHttpResponse.getArray();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONArray innerJsonArray = jsonArray.getJSONArray(i);
                        fundsArrayList.add(new BasicFundInfoParcelable(innerJsonArray.getString(0),
                                innerJsonArray.getString(3)));
                        Log.d(TAG, innerJsonArray.getString(3));
                    }

                } catch (JSONException e) {
                    Log.d(TAG, e.toString());
                }
            } catch (UnirestException e) {
                Log.d(TAG, e.toString());
            }
            if (fundsArrayList.size() != 0) {
                Bundle dataBundle = new Bundle();
                dataBundle.putParcelableArrayList(mContext.getString(R.string.basic_search_results_parcelable), fundsArrayList);
                Intent intent = new Intent();
                intent.setAction(mContext.getString(R.string.gcmtask_intent));
                intent.putExtra(mContext.getString(R.string.search_data_bundle), dataBundle);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        }
        if (taskParams.getTag().equals(mContext.getString(R.string.tag_search_scode))) {
            HttpResponse<JsonNode> response;
            String fundName = null;
            String nav = null;
            String scode = taskParams.getExtras().getString(mContext.getString(R.string.key_scode));
            String url = String.valueOf(FundsContentProvider.mUri) + "/" + scode;
            Uri queryUri = Uri.parse(url);
            Cursor cursor = mContext.getContentResolver().query(queryUri, null, null, null, null);
            if (cursor.moveToFirst()) {
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
                    JsonNode jsonNodeHttpResponse = response.getBody();
                    Log.d(TAG, jsonNodeHttpResponse.toString());
                    try {
                        JSONObject jsonObject = jsonNodeHttpResponse.getObject();
                        JSONObject jsonObject1 = jsonObject.getJSONObject(scode);
                        fundName = jsonObject1.getString(KEY_FUNDNAME);
                        nav = jsonObject1.getString(KEY_NAV);
                        Log.d(TAG, nav);
                    } catch (JSONException je) {
                        Log.d(TAG, je.toString());
                    }
                } catch (UnirestException ue) {
                    Log.d(TAG, ue.toString());
                }
                if (fundName != null && nav != null) {
                    ContentValues fundContentValues = new ContentValues();
                    fundContentValues.put(FundsContentProvider.FUND_SCODE, scode);
                    fundContentValues.put(FundsContentProvider.FUND_NAME, fundName);
                    fundContentValues.put(FundsContentProvider.FUND_NAV, nav);
                    Uri uri = mContext.getContentResolver().insert(FundsContentProvider.mUri, fundContentValues);
                    if (uri != null){
                        sendToast(mContext.getString(R.string.fund_added_message));
                    }
                }
            }
        }
        return 0;
    }
    public void sendToast(String message){
        Intent intent = new Intent();
        intent.setAction(mContext.getString(R.string.gcmtask_intent));
        intent.putExtra(mContext.getString(R.string.key_toast_message),
                message);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
}
