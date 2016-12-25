package com.neerajms99b.neeraj.mymutualfunds.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;

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
        String tag = intent.getStringExtra(getString(R.string.key_tag));
        if (tag.equals(getString(R.string.tag_search_fund))) {
            Bundle bundle = new Bundle();
            bundle.putString(getString(R.string.key_fund_search_word),
                    intent.getStringExtra(getString(R.string.key_fund_search_word)));
            FetchFundsTask fetchFundsTask = new FetchFundsTask(this);
            fetchFundsTask.onRunTask(new TaskParams(tag, bundle));
        } else if (tag.equals(getString(R.string.tag_search_scode))) {
            Bundle bundle = new Bundle();
            bundle.putString(getString(R.string.key_scode),
                    intent.getStringExtra(getString(R.string.key_scode)));
            bundle.putString(getString(R.string.key_fundname),
                    intent.getStringExtra(getString(R.string.key_fundname)));
            FetchFundsTask fetchFundsTask = new FetchFundsTask(this);
            fetchFundsTask.onRunTask(new TaskParams(tag, bundle));
        } else if (tag.equals(getString(R.string.tag_fetch_graph_data))) {
            Bundle bundle = new Bundle();
            bundle.putString(getString(R.string.key_scode),
                    intent.getStringExtra(getString(R.string.key_scode)));
            FetchFundsTask fetchFundsTask = new FetchFundsTask(this);
            fetchFundsTask.onRunTask(new TaskParams(tag, bundle));
        } else if (tag.equals(getString(R.string.tag_update_nav))) {
            FetchFundsTask fetchFundsTask = new FetchFundsTask(this);
            fetchFundsTask.onRunTask(new TaskParams(tag));
        } else if (tag.equals(getString(R.string.tag_insert_scodes))) {
            Bundle bundle = new Bundle();
            bundle.putString(getString(R.string.key_scode),
                    intent.getStringExtra(getString(R.string.key_scode)));
            bundle.putString(getString(R.string.key_last_updated_nav),
                    intent.getStringExtra(getString(R.string.key_last_updated_nav)));
            FetchFundsTask fetchFundsTask = new FetchFundsTask(this);
            fetchFundsTask.onRunTask(new TaskParams(tag, bundle));
        } else if (tag.equals(getString(R.string.tag_download_data))) {
            ResultReceiver receiver = intent.getParcelableExtra(getString(R.string.key_download_progress_receiver));
            Bundle bundle = new Bundle();
            bundle.putParcelable(getString(R.string.key_download_progress_receiver), receiver);
            FetchFundsTask fetchFundsTask = new FetchFundsTask(this);
            fetchFundsTask.onRunTask(new TaskParams(tag, bundle));
        }
    }
}
