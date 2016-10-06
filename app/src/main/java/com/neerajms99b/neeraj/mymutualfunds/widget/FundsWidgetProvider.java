package com.neerajms99b.neeraj.mymutualfunds.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.neerajms99b.neeraj.mymutualfunds.R;

/**
 * Created by neeraj on 6/10/16.
 */

public class FundsWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, FundsWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
        int length = appWidgetIds.length;
        String action = intent.getAction();
        if (length < 1) {
            return;
        } else {
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
        String netWorth = context.getResources().getString(R.string.rupee_symbol) +
                bundle.getString(context.getResources().getString(R.string.key_net_worth));
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        remoteViews.setTextViewText(R.id.net_worth_amount_widget,
                netWorth);
        remoteViews.setTextViewText(R.id.net_worth_change_widget,
                bundle.getString(context.getResources().getString(R.string.key_net_worth_change)));
        if (bundle.getBoolean(context.getResources().getString(R.string.key_is_net_change_negative))) {
            remoteViews.setTextColor(R.id.net_worth_amount_widget,
                    context.getResources().getColor(R.color.colorRed));
            remoteViews.setTextColor(R.id.net_worth_change_widget,
                    context.getResources().getColor(R.color.colorRed));
            remoteViews.setImageViewResource(R.id.net_worth_arrow_widget, R.drawable.ic_arrow_down);
        } else {
            remoteViews.setTextColor(R.id.net_worth_amount_widget,
                    context.getResources().getColor(R.color.colorGreen));
            remoteViews.setTextColor(R.id.net_worth_change_widget,
                    context.getResources().getColor(R.color.colorGreen));
            remoteViews.setImageViewResource(R.id.net_worth_arrow_widget, R.drawable.ic_arrow_up);
        }
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }
}
