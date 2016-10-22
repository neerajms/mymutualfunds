package com.neerajms99b.neeraj.mymutualfunds.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

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
        if (intent.getStringExtra(getString(R.string.key_tag))
                .equals(getString(R.string.tag_search_fund))) {
            Bundle bundle = new Bundle();
            bundle.putString(getString(R.string.key_fund_search_word),
                    intent.getStringExtra(getString(R.string.key_fund_search_word)));
            FetchFundsTask fetchFundsTask = new FetchFundsTask(this);
            fetchFundsTask.onRunTask(new TaskParams(
                    intent.getStringExtra(getString(R.string.key_tag)), bundle));
        } else if (intent.getStringExtra(getString(R.string.key_tag))
                .equals(getString(R.string.tag_search_scode))) {
            Bundle bundle = new Bundle();
            bundle.putString(getString(R.string.key_scode),
                    intent.getStringExtra(getString(R.string.key_scode)));
            FetchFundsTask fetchFundsTask = new FetchFundsTask(this);
            fetchFundsTask.onRunTask(new TaskParams(
                    intent.getStringExtra(getString(R.string.key_tag)), bundle));
        } else if (intent.getStringExtra(getString(R.string.key_tag))
                .equals(getString(R.string.tag_fetch_graph_data))) {
            Bundle bundle = new Bundle();
            bundle.putString(getString(R.string.key_scode),
                    intent.getStringExtra(getString(R.string.key_scode)));
            FetchFundsTask fetchFundsTask = new FetchFundsTask(this);
            fetchFundsTask.onRunTask(new TaskParams(
                    intent.getStringExtra(getString(R.string.key_tag)), bundle));
        } else if (intent.getStringExtra(getString(R.string.key_tag))
                .equals(getString(R.string.tag_update_nav))) {
            FetchFundsTask fetchFundsTask = new FetchFundsTask(this);
            fetchFundsTask.onRunTask(new TaskParams(
                    intent.getStringExtra(getString(R.string.key_tag))));
        } else if (intent.getStringExtra(getString(R.string.key_tag))
                .equals(getString(R.string.tag_insert_scodes))) {
            Bundle bundle = new Bundle();
            bundle.putString(getString(R.string.key_scode),
                    intent.getStringExtra(getString(R.string.key_scode)));
            bundle.putString(getString(R.string.key_last_updated_nav),
                    intent.getStringExtra(getString(R.string.key_last_updated_nav)));
            FetchFundsTask fetchFundsTask = new FetchFundsTask(this);
            fetchFundsTask.onRunTask(new TaskParams(
                    intent.getStringExtra(getString(R.string.key_tag)), bundle));
        }
    }
}
