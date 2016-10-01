//package com.neerajms99b.neeraj.mymutualfunds.service;
//
//import android.app.AlarmManager;
//import android.app.PendingIntent;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.util.Log;
//
//import com.neerajms99b.neeraj.mymutualfunds.R;
//
//import java.util.Calendar;
//
///**
// * Created by neeraj on 1/10/16.
// */
//
//public class AlarmReceiver extends BroadcastReceiver {
//    private AlarmManager mAlarmManager;
//    private PendingIntent mPendingIntent;
//    private String TAG = AlarmReceiver.class.getSimpleName();
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        Log.d(TAG, "alarm triggered");
//        if (intent.getStringExtra("tag").equals(context.getString(R.string.tag_update_nav))) {
//            Intent intent1 = new Intent(context, FundsIntentService.class);
//            intent.putExtra("tag", context.getString(R.string.tag_update_nav));
//            context.startService(intent1);
//        }
//    }
//
//    public void setAlarm(Context context) {
//        Log.e(TAG, "AlarmService set");
//        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        Intent intent = new Intent(context, AlarmReceiver.class);
//        intent.putExtra("tag", context.getString(R.string.tag_update_nav));
//        mPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(System.currentTimeMillis());
//        calendar.set(Calendar.HOUR_OF_DAY, 21);
//        calendar.set(Calendar.MINUTE, 18);
//        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
//                1000 * 60, mPendingIntent);
//    }
//}
//
