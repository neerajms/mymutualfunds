package com.neerajms99b.neeraj.mymutualfunds.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

import java.util.ArrayList;

public class FundWidgetConfigure extends AppCompatActivity {

    private static final String TAG = FundWidgetConfigure.class.getSimpleName();

    private FirebaseDatabase mDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private ArrayList<FundInfo> mFundsList;
    private ArrayList<String> mFundNamesList;
    private ArrayAdapter<String> mFundsListAdapter;

    private int mAppWidgetId;

    private Context mContext;

    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_widget_configure);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        mContext = this;
        mFundsList = new ArrayList<>();
        mFundNamesList = new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFundsListAdapter = new ArrayAdapter<>(this, R.layout.search_results_list_item,
                R.id.list_item, mFundNamesList);
        mListView = (ListView) findViewById(R.id.widget_configure_list_view);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
                RemoteViews views = new RemoteViews(mContext.getPackageName(),
                        R.layout.fund_widget);
                views.setTextViewText(R.id.fund_name_widget, mFundsList.get(i).getFundName());
                views.setTextViewText(R.id.fund_nav_widget,
                        String.format("%.2f", Float.parseFloat(mFundsList.get(i).getNav())) +
                                "  " + mFundsList.get(i).getChangeValue());

                appWidgetManager.updateAppWidget(mAppWidgetId, views);
//                Intent intent = new Intent(mContext, FundWidgetProvider.class);
//                intent.setAction(getString(R.string.action_update_widget_data));
//                Bundle bundle = new Bundle();
//                bundle.putString(getString(R.string.key_fundname), mFundsList.get(i).getFundName());
//                bundle.putString(getString(R.string.key_scode), mFundsList.get(i).getScode());
//                bundle.putString(getString(R.string.key_fund_nav), mFundsList.get(i).getNav());
//                bundle.putString(getString(R.string.key_change_value), mFundsList.get(i).getChangeValue());
//                bundle.putString(getString(R.string.key_change_percent), mFundsList.get(i).getChangePercent());
//                intent.putExtra(getString(R.string.key_widget_data_bundle), bundle);
//                startService(intent);
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        });
        mListView.setAdapter(mFundsListAdapter);
        readFirebaseFundsList();
    }

    public void readFirebaseFundsList() {
        DatabaseReference fundsListReference =
                mDatabase.getReference(mFirebaseUser.getUid())
                        .child(getString(R.string.firebase_child_funds));
        fundsListReference.keepSynced(true);
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                FundInfo fundInfo = dataSnapshot.getValue(FundInfo.class);
                Log.d(TAG, fundInfo.getFundName());
                mFundsList.add(fundInfo);
                mFundNamesList.add(fundInfo.getFundName());
                mFundsListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
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
}
