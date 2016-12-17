package com.neerajms99b.neeraj.mymutualfunds.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.neerajms99b.neeraj.mymutualfunds.R;
import com.neerajms99b.neeraj.mymutualfunds.models.FundInfo;

/**
 * Created by neeraj on 24/10/16.
 */

public class FundWidgetProvider extends AppWidgetProvider {
    private FirebaseUser mFirebaseUser;
    private Context mContext;
    private static final String KEY_FUNDS = "funds";
    private static final String KEY_ACTION = "com.neerajms99b.neeraj.mymutualfunds.FUNDS_WIDGET_INITIAL";
    private static final String TAG_ACTION = "tag";
    private static final String KEY_NAV = "mNav";
    private static final String KEY_CHANGE_VALUE = "mChangeValue";
    private static final String KEY_SCODE = "mScode";
    private static final String KEY_CHANGE_PERCENT = "mChangePercent";
    private static final String KEY_FUND_NAME = "mFundName";
    private static final String KEY_BUNDLE = "widgetDataBundle";
    private static final String KEY_FIRST_TIME = "first_time";
    private static final String KEY_WIDGET_ID = "appWidgetId";
    private static final String KEY_ACTION_UPDATE = "com.neerajms99b.neeraj.mymutualfunds.FUNDS_WIDGET_UPDATE";
    private static final String TAG = FundWidgetProvider.class.getSimpleName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        mContext = context;
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = firebaseAuth.getCurrentUser();
        for (int appWidgetId : appWidgetIds) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(
                    context.getString(R.string.shared_pref_widget), Context.MODE_PRIVATE);
            if (sharedPreferences.contains(String.valueOf(appWidgetId))) {
                String scode = sharedPreferences.getString(String.valueOf(appWidgetId), null);
                readFirebaseFundsList(appWidgetId, scode);
            }
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.fund_widget);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        mContext = context;
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = firebaseAuth.getCurrentUser();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, FundWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
        int length = appWidgetIds.length;
        String action = intent.getAction();
        if (length >= 1) {
            if (action != null && action.equals(KEY_ACTION)) {
                Bundle bundle = intent.getBundleExtra(
                        context.getResources().getString(R.string.key_widget_data_bundle));
                if (intent.getBooleanExtra(KEY_FIRST_TIME, false)) {
                    readFirebaseFundsList(bundle.getInt(KEY_WIDGET_ID), bundle.getString(KEY_SCODE));
                }
                updateWidget(context, bundle, appWidgetManager, bundle.getInt(KEY_WIDGET_ID));
            } else if (action != null && action.equals(KEY_ACTION_UPDATE)) {
                SharedPreferences sharedPreferences = context.getSharedPreferences(
                        context.getString(R.string.shared_pref_widget), Context.MODE_PRIVATE);
                for (int widgetId : appWidgetIds) {
                    readFirebaseFundsList(widgetId, sharedPreferences.getString(String.valueOf(widgetId), null));
                }
            }
        }
    }

    public void updateWidget(Context context, Bundle bundle,
                             AppWidgetManager appWidgetManager, int appWidgetId) {
        String fundName = bundle.getString(context.getResources().getString(R.string.key_fundname));
        String nav = bundle.getString(context.getResources().getString(R.string.key_fund_nav));
        String navFormatted = String.format("%.2f", Float.parseFloat(nav));
        String changeValue = bundle.getString(context.getResources().getString(R.string.key_change_value));
        String changeValueFormatted = String.format("%.2f", Float.parseFloat(changeValue));
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.fund_widget);
        remoteViews.setTextViewText(R.id.fund_name_widget, fundName);
        remoteViews.setTextViewText(R.id.fund_nav_widget, navFormatted + "  " + changeValueFormatted);
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    public void readFirebaseFundsList(final int appWidgetId, final String scode) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference fundsListReference =
                database.getReference(mFirebaseUser.getUid())
                        .child(KEY_FUNDS);
        fundsListReference.keepSynced(true);
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                FundInfo fundInfo = dataSnapshot.getValue(FundInfo.class);
                if (fundInfo.getScode().equals(scode)) {
                    setValuesInWidget(appWidgetId, fundInfo);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                FundInfo fundInfo = dataSnapshot.getValue(FundInfo.class);
                if (fundInfo.getScode().equals(scode)) {
                    setValuesInWidget(appWidgetId, fundInfo);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.toString());
            }
        };
        fundsListReference.addChildEventListener(childEventListener);
    }

    public void setValuesInWidget(int appWidgetId, FundInfo fundInfo) {
        Intent intent = new Intent(mContext, FundWidgetProvider.class);
        intent.setAction(KEY_ACTION);
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_WIDGET_ID, appWidgetId);
        bundle.putString(KEY_FUND_NAME, fundInfo.getFundName());
        bundle.putString(KEY_NAV, fundInfo.getNav());
        bundle.putString(KEY_CHANGE_VALUE, fundInfo.getChangeValue());
        intent.putExtra(KEY_BUNDLE, bundle);
        mContext.sendBroadcast(intent);
    }
}
