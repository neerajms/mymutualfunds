package com.neerajms99b.neeraj.mymutualfunds.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
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

import java.util.Calendar;

/**
 * Created by neeraj on 4/10/16.
 */

public class Alarm extends BroadcastReceiver {
    private AlarmManager mAlarmManager;
    private PendingIntent mPendingIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        String tag = intent.getStringExtra(context.getString(R.string.key_tag));
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");

        if (tag.equals(context.getString(R.string.tag_update_nav))) {
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
            wakeLock.acquire();
            GcmNetworkManager gcmNetworkManager = GcmNetworkManager.getInstance(context);
            OneoffTask task = new OneoffTask.Builder()
                    .setService(FetchFundsTask.class)
                    .setTag(context.getString(R.string.tag_update_nav))
                    .setExecutionWindow(0L, 60 * DateUtils.MINUTE_IN_MILLIS)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .build();
            gcmNetworkManager.schedule(task);
            wakeLock.release();
        }
    }

    public void setAlarm(Context context) {
        Log.e("alarm","alarm set");
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, Alarm.class);
        intent.putExtra(context.getString(R.string.key_tag), context.getString(R.string.tag_update_nav));
        mPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 6);
        mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, mPendingIntent);
    }
}
