package com.neerajms99b.neeraj.mymutualfunds.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.json.JSONArray;
import org.json.JSONException;

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
    private Context mContext;
    public FetchFundsTask(){}
    public FetchFundsTask(Context context){
        mContext = context;
    }
    @Override
    public int onRunTask(TaskParams taskParams) {
        HttpResponse<JsonNode> response;
        String query = "{\"search\":\"franklin bluechip\"}";
        if (taskParams.getTag().equals("force")) {
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
                        JSONArray jsonArray1 = jsonArray.getJSONArray(i);
                        Log.d(TAG, jsonArray1.getString(3));
                        Intent intent = new Intent("result");
                        intent.putExtra("result",jsonArray1.getString(3));
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                    }

                }catch (JSONException e){
                    Log.d(TAG,e.toString());
                }
            } catch (UnirestException e) {
                Log.d(TAG, e.toString());
            }
        }
        return 0;
    }
}
