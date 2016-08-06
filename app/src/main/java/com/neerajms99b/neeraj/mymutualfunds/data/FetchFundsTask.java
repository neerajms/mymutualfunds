package com.neerajms99b.neeraj.mymutualfunds.data;

import android.os.AsyncTask;
import android.util.Log;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by neeraj on 6/8/16.
 */
public class FetchFundsTask extends AsyncTask<String,Void, JsonNode> {
    private final String TAG = getClass().getSimpleName();
    public static final MediaType JSON
            = MediaType.parse("application/json");
    private OkHttpClient mClient;
    private final String FUNDS_BASE_URL = "https://mutualfundsnav.p.mashape.com/";
    private final String KEY_PARAM = "X-Mashape-Key";
    private final String CONTENT_TYPE_PARAM = "Content-Type";
    private final String ACCEPT_PARAM = "Accept";
    private final String CONTENT_TYPE_VALUE = "application/json";
    private final String ACCEPT_VALUE = "application/json";
    private final String KEY_VALUE = "My Key";


    @Override
    protected JsonNode doInBackground(String... strings) {
        Log.d(TAG, "Entered bgtask" + strings[0]);
        HttpURLConnection urlConnection = null;
        HttpResponse<JsonNode> response;


//        Uri builtUri = Uri.parse(FUNDS_BASE_URL).buildUpon()
//                .appendQueryParameter(KEY_PARAM,KEY_VALUE)
//                .appendQueryParameter(CONTENT_TYPE_PARAM,CONTENT_TYPE_VALUE)
//                .appendQueryParameter(ACCEPT_PARAM,ACCEPT_VALUE)
//                .

        try {

            Log.d(TAG, "inside try 1");
//            String url = "https://mutualfundsnav.p.mashape.com/";
//            urlConnection = (HttpURLConnection) url.openConnection();
//            Log.d(TAG,"inside try 2");
//            urlConnection.setRequestMethod("POST");
//            urlConnection.setDoOutput(true);
//            Log.d(TAG,"inside try 3");
//            urlConnection.addRequestProperty(KEY_PARAM,KEY_VALUE);
//            Log.d(TAG,"inside try 4");
//            urlConnection.addRequestProperty(CONTENT_TYPE_PARAM,CONTENT_TYPE_VALUE);
//            Log.d(TAG,"inside try 5");
//            urlConnection.addRequestProperty(ACCEPT_PARAM,ACCEPT_VALUE);
//            Log.d(TAG,"inside try 6");
            String query = "{\"search\":\"franklin bluechip\"}";
////            urlConnection.getOutputStream().write(query.getBytes());
//            Log.d(TAG,"inside try 7");
//
////            urlConnection.connect();

//            mClient = new OkHttpClient();

            response = Unirest.post("https://mutualfundsnav.p.mashape.com/")
                    .header("X-Mashape-Key", KEY_VALUE)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .body(query)
                    .asJson();
//            String response = post(url,query);

//            InputStream is = urlConnection.getInputStream();
//            BufferedReader br = new BufferedReader(new InputStreamReader(is));
//            String response = br.readLine();

//            Log.d(TAG,response.getStatusText());
            return response.getBody();

        } catch (UnirestException e) {
            Log.e(TAG,e.toString());

        }
        return null;
    }

    @Override
    protected void onPostExecute(JsonNode jsonNodeHttpResponse) {
        super.onPostExecute(jsonNodeHttpResponse);
        try {


            if (jsonNodeHttpResponse != null) {
                JSONArray jsonArray = jsonNodeHttpResponse.getArray();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONArray jsonArray1 = jsonArray.getJSONArray(i);
                    Log.d(TAG,jsonArray1.getString(3));
                }
            }
        }catch (JSONException e){
            Log.e(TAG,e.toString());

        }
    }

    private String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .addHeader(KEY_PARAM, KEY_VALUE)
                .addHeader(CONTENT_TYPE_PARAM, CONTENT_TYPE_VALUE)
                .addHeader(ACCEPT_PARAM, ACCEPT_VALUE)
                .post(body)
                .build();
        Log.d(TAG, request.toString());
        Response response = mClient.newCall(request).execute();
        return response.body().toString();
    }
}
