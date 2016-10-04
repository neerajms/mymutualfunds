package com.neerajms99b.neeraj.mymutualfunds.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;
import com.neerajms99b.neeraj.mymutualfunds.R;

/**
 * Created by neeraj on 4/10/16.
 */

public class Alarm extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        FetchFundsTask fetchFundsTask = new FetchFundsTask(context);
        Log.e("Alarm","Alarm triggered");
        GcmNetworkManager gcmNetworkManager = GcmNetworkManager.getInstance(context);
        OneoffTask task = new OneoffTask.Builder()
                .setService(FetchFundsTask.class)
                .setTag(context.getString(R.string.tag_update_nav))
                .setExecutionWindow(0L,10800L)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .build();
        gcmNetworkManager.schedule(task);
    }
}
