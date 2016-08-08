package com.neerajms99b.neeraj.mymutualfunds.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.TaskParams;

/**
 * Created by neeraj on 8/8/16.
 */
public class FundsIntentService extends IntentService {
    public FundsIntentService(){
        super(FundsIntentService.class.getName());
    }
    public FundsIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(FundsIntentService.class.getName(),"intent service executed");
        FetchFundsTask fetchFundsTask = new FetchFundsTask(this);
        fetchFundsTask.onRunTask(new TaskParams(intent.getStringExtra("tag")));
    }
}
