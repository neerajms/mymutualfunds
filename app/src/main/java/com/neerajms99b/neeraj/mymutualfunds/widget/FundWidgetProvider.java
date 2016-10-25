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
    private static final String KEY_ACTION = "android.appwidget.action.APPWIDGET_UPDATE";
    private static final String KEY_NAV = "mNav";
    private static final String KEY_CHANGE_VALUE = "mChangeValue";
    private static final String KEY_CHANGE_PERCENT = "mChangePercent";
    private static final String KEY_FUND_NAME = "mFundName";
    private static final String KEY_BUNDLE = "mBundle";

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
                Log.e("widget", scode);
                readFirebaseFundsList(scode);
            }
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.fund_widget);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.e("widget", "onReceive");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, FundWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
        int length = appWidgetIds.length;
        String action = intent.getAction();
        if (length >= 1) {
            Log.e("widget", "length satisfied" + action);
            if (action != null && action.equals(KEY_ACTION)) {
                Log.e("widget", "action matched");
                Bundle bundle = intent.getBundleExtra(
                        context.getResources().getString(R.string.key_widget_data_bundle));
                updateWidget(context, bundle, appWidgetManager, appWidgetIds[length - 1]);
            }
        }
    }

    public void updateWidget(Context context, Bundle bundle,
                             AppWidgetManager appWidgetManager, int appWidgetId) {
        String fundName = bundle.getString(context.getResources().getString(R.string.key_fundname));
        Log.e("widget", fundName);
        String nav = bundle.getString(context.getResources().getString(R.string.key_fund_nav));
        String changeValue = bundle.getString(context.getResources().getString(R.string.key_change_value));
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.fund_widget);
        remoteViews.setTextViewText(R.id.fund_name_widget,
                fundName);
        remoteViews.setTextViewText(R.id.fund_nav_widget, nav + "  " + changeValue);
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    public void readFirebaseFundsList(final String scode) {
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
                    setValuesInWidget(fundInfo);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                FundInfo fundInfo = dataSnapshot.getValue(FundInfo.class);
                if (fundInfo.getScode().equals(scode)) {
                    setValuesInWidget(fundInfo);
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
                Log.e("widget", databaseError.toString());
            }
        };
        fundsListReference.addChildEventListener(childEventListener);
    }

    public void setValuesInWidget(FundInfo fundInfo) {
        Intent intent = new Intent(mContext, FundWidgetProvider.class);
        intent.setAction(KEY_ACTION);
        Bundle bundle = new Bundle();
        bundle.putString(KEY_FUND_NAME, fundInfo.getFundName());
        bundle.putString(KEY_NAV, fundInfo.getNav());
        bundle.putString(KEY_CHANGE_VALUE, fundInfo.getChangeValue());
        intent.putExtra(KEY_BUNDLE, bundle);
        mContext.sendBroadcast(intent);
    }
}
