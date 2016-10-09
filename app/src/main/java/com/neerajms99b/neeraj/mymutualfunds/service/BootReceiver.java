package com.neerajms99b.neeraj.mymutualfunds.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.neerajms99b.neeraj.mymutualfunds.R;

/**
 * Created by neeraj on 5/10/16.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Alarm alarm = new Alarm();
        if (intent.getAction().equals(context.getString(R.string.intent_action_boot_completed))) {
            alarm.setAlarm(context);
        }
    }
}