package com.neerajms99b.neeraj.mymutualfunds.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.text.format.DateUtils;
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
        String tag = intent.getStringExtra("tag");
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"");
        if (tag.equals(context.getString(R.string.tag_update_nav))) {
            FetchFundsTask fetchFundsTask = new FetchFundsTask(context);
            Log.e("Alarm", "Alarm triggered");
            wakeLock.acquire();
            GcmNetworkManager gcmNetworkManager = GcmNetworkManager.getInstance(context);
            OneoffTask task = new OneoffTask.Builder()
                    .setService(FetchFundsTask.class)
                    .setTag(context.getString(R.string.tag_update_nav))
                    .setExecutionWindow(0L, 12 * DateUtils.HOUR_IN_MILLIS)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .build();
            gcmNetworkManager.schedule(task);
            wakeLock.release();
        } else if (tag.equals(context.getString(R.string.retrigger_update_nav))) {
            FetchFundsTask fetchFundsTask = new FetchFundsTask(context);
            Log.e("Alarm", "Alarm Re-triggered");
            wakeLock.acquire();
            GcmNetworkManager gcmNetworkManager = GcmNetworkManager.getInstance(context);
            OneoffTask task = new OneoffTask.Builder()
                    .setService(FetchFundsTask.class)
                    .setTag(context.getString(R.string.tag_update_nav))
                    .setExecutionWindow(0L, 15 * DateUtils.MINUTE_IN_MILLIS)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .build();
            gcmNetworkManager.schedule(task);
            wakeLock.release();
        }
    }
}
