package com.neerajms99b.neeraj.mymutualfunds.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.neerajms99b.neeraj.mymutualfunds.R;

/**
 * Created by neeraj on 24/10/16.
 */

public class FundWidgetProvider extends AppWidgetProvider {
    private FirebaseUser mFirebaseUser;
    private Context mContext;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        mContext = context;
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = firebaseAuth.getCurrentUser();
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.fund_widget);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, FundWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
        int length = appWidgetIds.length;
        String action = intent.getAction();
        if (length >= 1) {
            if (action != null && action.equals(context.getResources()
                    .getString(R.string.action_update_widget_data))) {
                Bundle bundle = intent.getBundleExtra(
                        context.getResources().getString(R.string.key_widget_data_bundle));
                updateWidget(context, bundle, appWidgetManager, appWidgetIds[length - 1]);
            }
        }
    }

    public void updateWidget(Context context, Bundle bundle,
                             AppWidgetManager appWidgetManager, int appWidgetId) {
        String fundName = bundle.getString(context.getResources().getString(R.string.key_fundname));
//        Toast.makeText(context,fundName,Toast.LENGTH_SHORT).show();
        Log.e("widget",fundName);
        String nav = bundle.getString(context.getResources().getString(R.string.key_fund_nav));
        String changeValue = bundle.getString(context.getResources().getString(R.string.key_change_value));
        String changePercent = bundle.getString(context.getResources().getString(R.string.key_change_percent));
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.fund_widget);
        remoteViews.setTextViewText(R.id.fund_name_widget,
                fundName);
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }
}
