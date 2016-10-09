package com.neerajms99b.neeraj.mymutualfunds.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.neerajms99b.neeraj.mymutualfunds.R;
import com.neerajms99b.neeraj.mymutualfunds.adapter.UpdateFragment;
import com.neerajms99b.neeraj.mymutualfunds.models.FundInfo;
import com.neerajms99b.neeraj.mymutualfunds.models.NetWorthGraphModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;


/**
 * Created by neeraj on 27/8/16.
 */
public class MyStatsFragment extends Fragment implements UpdateFragment {
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private float mNetWorth;
    private String TAG = MyStatsFragment.class.getSimpleName();
    private ArrayList<FundInfo> mFundsArrayList;
    private TextView mNetWorthAmountTextView;
    private String mToday;
    private String mYesterday;
    private ArrayList<String> mLabels;
    private ArrayList<String> mEntriesString;
    private ArrayList<Entry> mEntries;
    private LineChart mChart;
    private int mLabelIndex;
    private ArrayList<NetWorthGraphModel> mGraphList;
    private float mNetWorthFire;
    private Iterable<DataSnapshot> mDataSnapshotIterable;
    private String mNetWorthChange;
    private boolean mIsNetChangeNegative;
    private TextView mNetWorthChangeTextView;
    private ImageView mChangeArrow;
    private static final String KEY_FIREBASE_FUNDS_CHILD = "funds";
    private static final String KEY_NETWORTH = "net_worth";
    private static final String NET_WORTH_GRAPH_VALUES = "Net worth values";
    private static final String GRAPH_DESCRIPTION = "Net worth for the last few days";
    private static final String KEY_NETWORTH_CHANGE = "netWorthChange";
    private static final String KEY_NET_CHANGE_NEGATIVE = "isNetWorthNegative";
    private static final String KEY_ACTION_UPDATE_WIDGET_DATA = "actionUpdateWidgetData";
    private static final String KEY_WIDGET_DATA_BUNDLE = "widgetDataBundle";
    private ProgressBar mProgressBarNetWorth;
    private ProgressBar mProgressBarNetWorthGraph;
    private SharedPreferences mSharedPreferences;
    private FrameLayout mNetWorthFrame;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFundsArrayList = new ArrayList<FundInfo>();
        mDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mEntries = new ArrayList<>();
        mLabels = new ArrayList<String>();
        mGraphList = new ArrayList<>();
        getDates();
        readFirebaseFundsList();
        setHasOptionsMenu(false);
        mSharedPreferences = getActivity().getSharedPreferences(
                getString(R.string.key_shared_prefs_my_stats), Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stats, container, false);
        mChart = (LineChart) rootView.findViewById(R.id.chart_networth);
        mChart.setVisibility(View.INVISIBLE);
        mProgressBarNetWorth = (ProgressBar) rootView.findViewById(R.id.progress_bar_networth);
        mProgressBarNetWorthGraph =
                (ProgressBar) rootView.findViewById(R.id.progress_bar_networth_graph);
        readFirebaseNetworthList();
        mNetWorthAmountTextView = (TextView) rootView.findViewById(R.id.net_worth_amount);
        mNetWorthChangeTextView = (TextView) rootView.findViewById(R.id.net_worth_change);
        mChangeArrow = (ImageView) rootView.findViewById(R.id.net_worth_arrow);
        mNetWorthFrame = (FrameLayout) rootView.findViewById(R.id.net_worth_frame);
        AdView mAdView = (AdView) rootView.findViewById(R.id.adView);
        MobileAds.initialize(getActivity().getApplicationContext(), "ca-app-pub-3940256099942544~3347511713");
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        showCase();
    }

    public void showCase() {
        if (mSharedPreferences.getBoolean(getString(R.string.key_is_firstrun), true)) {
            new ShowcaseView.Builder(getActivity())
                    .withMaterialShowcase()
                    .setStyle(R.style.CustomShowcaseTheme)
                    .setShowcaseEventListener(new OnShowcaseEventListener() {
                        @Override
                        public void onShowcaseViewHide(ShowcaseView showcaseView) {
                            new ShowcaseView.Builder(getActivity())
                                    .withMaterialShowcase()
                                    .setStyle(R.style.CustomShowcaseTheme)
                                    .setTarget(new ViewTarget(mChart))
                                    .setContentTitle(getString(R.string.net_worth_graph_showcase_title))
                                    .setContentText(getString(R.string.net_worth_graph_showcase_content))
                                    .hideOnTouchOutside()
                                    .build();
                        }

                        @Override
                        public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

                        }

                        @Override
                        public void onShowcaseViewShow(ShowcaseView showcaseView) {

                        }

                        @Override
                        public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) {

                        }
                    })
                    .setTarget(new ViewTarget(mNetWorthAmountTextView))
                    .setContentTitle(getString(R.string.net_worth_showcase_title))
                    .setContentText(getString(R.string.net_worth_showcase_content))
                    .hideOnTouchOutside()
                    .build();

            mSharedPreferences.edit().putBoolean(getString(R.string.key_is_firstrun), false).apply();
        }
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
                Log.e(TAG, fundInfo.getFundName());

                if (!alreadyPresent(fundInfo)) {
                    mFundsArrayList.add(fundInfo);
                }
                calculateNetWorth();
                storeNetWorthInFirebase();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                FundInfo fundInfo = dataSnapshot.getValue(FundInfo.class);
                reflectChange(fundInfo);
                calculateNetWorth();
                storeNetWorthInFirebase();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                FundInfo fundInfo = dataSnapshot.getValue(FundInfo.class);
                reflectRemoval(fundInfo);
                calculateNetWorth();
                storeNetWorthInFirebase();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        fundsListReference.addChildEventListener(childEventListener);
    }

    public boolean alreadyPresent(FundInfo fundInfo) {
        if (mFundsArrayList.size() > 0) {
            for (int i = 0; i < mFundsArrayList.size(); i++) {
                if (mFundsArrayList.get(i).getScode().equals(fundInfo.getScode())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void reflectChange(FundInfo fundInfoNew) {
        for (int index = 0; index < mFundsArrayList.size(); index++) {
            if (mFundsArrayList.get(index).getScode().equals(fundInfoNew.getScode())) {
                mFundsArrayList.remove(index);
                mFundsArrayList.add(index, fundInfoNew);
                break;
            }
        }
        Log.e(TAG, "reflect change executed");
    }

    public void calculateNetWorth() {
        mNetWorth = 0;
        for (int index = 0; index < mFundsArrayList.size(); index++) {
            FundInfo fundInfo = mFundsArrayList.get(index);
            mNetWorth = mNetWorth + (Float.parseFloat(fundInfo.getNav())
                    * Float.parseFloat(fundInfo.getUnits()));
        }
        Log.e(TAG, String.valueOf(mNetWorth));
    }

    public void storeNetWorthInFirebase() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String date = dateFormat.format(calendar.getTime());
        DatabaseReference netWorthRef = mDatabase.getReference(
                mFirebaseUser.getUid()).child(KEY_NETWORTH);
        netWorthRef.child(date).setValue(String.format("%.2f", mNetWorth));
    }

    @Override
    public void updateNetWorth() {
        setNetWorth();
        populateChart();
    }

    public void reflectRemoval(FundInfo fundInfo) {
        for (int index = 0; index < mFundsArrayList.size(); index++) {
            if (mFundsArrayList.get(index).getScode().equals(fundInfo.getScode())) {
                mFundsArrayList.remove(index);
                break;
            }
        }
    }

    public void readFirebaseNetworthList() {
        mLabelIndex = 0;
        mEntries.clear();
        mLabels.clear();
        DatabaseReference netWorthRef = mDatabase.getReference(
                mFirebaseUser.getUid()).child(getString(R.string.key_net_worth));
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                processGraphData(dataSnapshot);
                calculateNetWorthChange();
                if (isAdded()) {
                    setNetWorth();
                    populateChart();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                changeNetworthList(dataSnapshot);
                calculateNetWorthChange();
                if (isAdded()) {
                    mProgressBarNetWorth.setVisibility(View.VISIBLE);
                    mProgressBarNetWorthGraph.setVisibility(View.VISIBLE);
                    setNetWorth();
                    populateChart();
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

            }
        };
        netWorthRef.addChildEventListener(childEventListener);
    }

    public void getDates() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        mToday = format.format(calendar.getTime());
        try {
            calendar.setTime(format.parse(mToday));
            calendar.add(Calendar.DATE, -1);
            mYesterday = format.format(calendar.getTime());
        } catch (ParseException pe) {
            Log.e(TAG, pe.toString());
        }
    }

    public void populateChart() {
        LineDataSet lineDataSet = new LineDataSet(mEntries,
                NET_WORTH_GRAPH_VALUES);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setDrawCircleHole(true);
        lineDataSet.setCircleColorHole(getResources().getColor(R.color.colorAccent));
        lineDataSet.setCircleRadius(4.5f);
//        lineDataSet.setDrawFilled(true);
//        lineDataSet.setFillColor(getResources().getColor(R.color.colorAccent));
        lineDataSet.setColor(getResources().getColor(R.color.colorAccent), 220);
//        lineDataSet.setFillAlpha(220);
        lineDataSet.setDrawValues(false);
        lineDataSet.setLineWidth(3.5f);

        YAxis yAxisLeft = mChart.getAxisLeft();
        yAxisLeft.setTextColor(getResources().getColor(android.R.color.black));
        yAxisLeft.setDrawGridLines(false);

        YAxis yAxisRight = mChart.getAxisRight();
        yAxisRight.setDrawLabels(false);
//        yAxisRight.setTextColor(getResources().getColor(android.R.color.white));
        yAxisRight.setDrawGridLines(false);
        yAxisRight.setDrawAxisLine(false);

        AxisValueFormatter formatter = new AxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return mLabels.get((int) value);
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        };

        XAxis xAxis = mChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setTextColor(getResources().getColor(android.R.color.black));
        xAxis.setValueFormatter(formatter);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);


        LineData data = new LineData(lineDataSet);
        mChart.setDescription(GRAPH_DESCRIPTION);
        mChart.setData(data);
        mChart.animateY(0);
        mProgressBarNetWorthGraph.setVisibility(View.INVISIBLE);
        mChart.setVisibility(View.VISIBLE);
    }

    public void changeNetworthList(DataSnapshot dataSnapshot) {
        int index = mGraphList.size() - 1;
        mGraphList.remove(index);
        mEntries.remove(index);
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date date = format.parse(dataSnapshot.getKey());
            NetWorthGraphModel netWorthGraphModel = new NetWorthGraphModel(date,
                    dataSnapshot.getValue().toString());
            mGraphList.add(index, netWorthGraphModel);
            mEntries.add(index, new Entry(index, Float.valueOf(mGraphList.get(index).getNetworth())));
        } catch (ParseException pe) {
            Log.e(TAG, pe.toString());
        }
    }

    public void processGraphData(DataSnapshot dataSnapshot) {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date date = format.parse(dataSnapshot.getKey());
            NetWorthGraphModel netWorthGraphModel = new NetWorthGraphModel(date,
                    dataSnapshot.getValue().toString());
            mGraphList.add(netWorthGraphModel);
            Collections.sort(mGraphList);
        } catch (ParseException pe) {
            Log.e(TAG, pe.toString());
        }
        mEntries.clear();
        mLabels.clear();
        if (mGraphList.size() > 30) {
            removeOldNetWorth();
        }
        for (int i = 0; i < mGraphList.size(); i++) {
            mEntries.add(i, new Entry(i, Float.valueOf(mGraphList.get(i).getNetworth())));
            mLabels.add(i, mGraphList.get(i).getDate().toString().substring(4, 10));
        }
    }

    public void removeOldNetWorth() {
        DatabaseReference netWorthRef = mDatabase.getReference(
                mFirebaseUser.getUid()).child(KEY_NETWORTH);
        Date date = mGraphList.get(0).getDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String dateStr = dateFormat.format(date);
        Log.e(TAG, dateStr);
        netWorthRef.child(dateStr).removeValue();
        mGraphList.remove(0);
    }

    public void calculateNetWorthChange() {
        float netWorthLatest = 0.0f;
        float netWorthPrevious = 0.0f;
        float changeValue = 0.0f;
        float changePercent = 0.0f;
        mIsNetChangeNegative = false;
        if (mGraphList.size() >= 2) {
            netWorthLatest = Float.parseFloat(mGraphList.get(mGraphList.size() - 1).getNetworth());
            netWorthPrevious = Float.parseFloat(mGraphList.get(mGraphList.size() - 2).getNetworth());
            changeValue = netWorthLatest - netWorthPrevious;
            changePercent = Math.abs(changeValue) * 100 / netWorthPrevious;
            String changePercentStr = String.format("%.2f", changePercent);
            if (changeValue < 0) {
                mIsNetChangeNegative = true;
            }
            mNetWorthChange = String.format("%.2f", Math.abs(changeValue)) + "(" + changePercentStr + "%)";
            Log.e(TAG, "netChPerc: " + String.valueOf(changeValue) + " " + String.valueOf(changePercent) + "%");
        } else if (mGraphList.size() == 1) {
            netWorthLatest = Float.parseFloat(mGraphList.get(mGraphList.size() - 1).getNetworth());
            netWorthPrevious = 0.0f;
            changeValue = netWorthLatest - netWorthPrevious;
            if (changeValue < 0) {
                mIsNetChangeNegative = true;
            }
            mNetWorthChange = String.format("%.2f", Math.abs(changeValue));
        }
        mNetWorth = netWorthLatest;
    }

    public void setNetWorth() {
        if (mNetWorthChange != null) {
            mNetWorthChangeTextView.setText(mNetWorthChange);
            String netWorthStr = getString(R.string.rupee_symbol) + String.format("%.2f", mNetWorth);
            mNetWorthAmountTextView.setText(netWorthStr);
            if (mIsNetChangeNegative) {
                mNetWorthChangeTextView.setTextColor(getResources().getColor(R.color.colorRed));
                mChangeArrow.setImageResource(R.drawable.ic_arrow_down);
                mNetWorthAmountTextView.setTextColor(getResources().getColor(R.color.colorRed));
            } else {
                mNetWorthChangeTextView.setTextColor(getResources().getColor(R.color.colorGreen));
                mChangeArrow.setImageResource(R.drawable.ic_arrow_up);
                mNetWorthAmountTextView.setTextColor(getResources().getColor(R.color.colorGreen));
            }
        }
        mProgressBarNetWorth.setVisibility(View.INVISIBLE);
    }
}
