package com.neerajms99b.neeraj.mymutualfunds.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.TaskParams;
import com.neerajms99b.neeraj.mymutualfunds.R;

/**
 * Created by neeraj on 8/8/16.
 */
public class FundsIntentService extends IntentService {
    public FundsIntentService() {
        super(FundsIntentService.class.getName());
    }

    public FundsIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(FundsIntentService.class.getName(), "intent service executed");
        if (intent.getStringExtra("tag").equals("force")) {
            Bundle bundle = new Bundle();
            bundle.putString("fund", intent.getStringExtra("fund"));
            FetchFundsTask fetchFundsTask = new FetchFundsTask(this);
            fetchFundsTask.onRunTask(new TaskParams(intent.getStringExtra("tag"), bundle));
        } else if (intent.getStringExtra("tag").equals(getString(R.string.tag_search_scode))) {
            Bundle bundle = new Bundle();
            bundle.putString(getString(R.string.key_scode), intent.getStringExtra(getString(R.string.key_scode)));
            FetchFundsTask fetchFundsTask = new FetchFundsTask(this);
            fetchFundsTask.onRunTask(new TaskParams(intent.getStringExtra("tag"), bundle));
        }else if (intent.getStringExtra("tag").equals(getString(R.string.tag_fetch_graph_data))){
            Bundle bundle = new Bundle();
            bundle.putString(getString(R.string.key_scode),intent.getStringExtra(getString(R.string.key_scode)));
            FetchFundsTask fetchFundsTask = new FetchFundsTask(this);
            fetchFundsTask.onRunTask(new TaskParams(intent.getStringExtra("tag"),bundle));
        }
    }
}
