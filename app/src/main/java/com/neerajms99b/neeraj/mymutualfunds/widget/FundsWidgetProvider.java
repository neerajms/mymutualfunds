package com.neerajms99b.neeraj.mymutualfunds.widget;

import android.app.PendingIntent;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.neerajms99b.neeraj.mymutualfunds.R;
import com.neerajms99b.neeraj.mymutualfunds.models.NetWorthGraphModel;
import com.neerajms99b.neeraj.mymutualfunds.ui.MainActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * Created by neeraj on 6/10/16.
 */

public class FundsWidgetProvider extends AppWidgetProvider {
    private FirebaseAuth mFirebaseAuth;
    private Context mContext;
    private FirebaseUser mFirebaseUser;
    private static final String KEY_NET_WORTH = "net_worth";
    private static final String KEY_ACTION_UPDATE_WIDGET_DATA = "actionUpdateWidgetData";
    private static final String KEY_WIDGET_DATA_BUNDLE = "widgetDataBundle";
    private String mLatestNetWorth;
    private ArrayList<NetWorthGraphModel> mNetWorthList;
    private String TAG = FundsWidgetProvider.class.getSimpleName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        mContext = context;
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mNetWorthList = new ArrayList<>();
        if (mFirebaseUser != null) {
            fireBaseReceiver();
        }
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.widget_frame, pendingIntent);
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
        String netWorth = context.getResources().getString(R.string.rupee_symbol) +
                bundle.getString(context.getResources().getString(R.string.key_net_worth));
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        remoteViews.setTextViewText(R.id.net_worth_amount_widget,
                netWorth);
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    public void fireBaseReceiver() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference netWorthRef = database.getReference(
                mFirebaseUser.getUid()).child(KEY_NET_WORTH);
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                getLatestNetWorth(dataSnapshot);
                setNetWorthInWidget();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                getLatestNetWorth(dataSnapshot);
                setNetWorthInWidget();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        netWorthRef.addChildEventListener(childEventListener);
    }

    public void setNetWorthInWidget() {
        String netWorth = mNetWorthList.get(mNetWorthList.size() - 1).getNetworth();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_NET_WORTH, netWorth);
        Intent intent = new Intent(mContext, FundsWidgetProvider.class);
        intent.setAction(KEY_ACTION_UPDATE_WIDGET_DATA);
        intent.putExtra(KEY_WIDGET_DATA_BUNDLE, bundle);
        mContext.sendBroadcast(intent);
    }

    public void getLatestNetWorth(DataSnapshot dataSnapshot) {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date date = format.parse(dataSnapshot.getKey());
            NetWorthGraphModel netWorthGraphModel = new NetWorthGraphModel(date,
                    dataSnapshot.getValue().toString());
            mNetWorthList.add(netWorthGraphModel);
            Collections.sort(mNetWorthList);
        } catch (java.text.ParseException pe) {
            Log.e(TAG, pe.toString());
        }
    }
}
