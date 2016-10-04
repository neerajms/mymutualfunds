package com.neerajms99b.neeraj.mymutualfunds.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
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
    private MainActivity mCallBack;
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mCallBack = (MainActivity) getActivity();
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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stats, container, false);
        mChart = (LineChart) rootView.findViewById(R.id.chart_networth);
        readFirebaseNetworthList();
        mNetWorthAmountTextView = (TextView) rootView.findViewById(R.id.net_worth_amount);
        mNetWorthChangeTextView = (TextView) rootView.findViewById(R.id.net_worth_change);
        mChangeArrow = (ImageView) rootView.findViewById(R.id.net_worth_arrow);
        Log.e(TAG, "oncreateview");
        setNetWorth();
        return rootView;
    }

    public void readFirebaseFundsList() {
        DatabaseReference fundsListReference =
                mDatabase.getReference(mFirebaseUser.getUid()).child(getString(R.string.firebase_child_funds));
        fundsListReference.keepSynced(true);
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                String key = dataSnapshot.getKey();
                FundInfo fundInfo = dataSnapshot.getValue(FundInfo.class);
                Log.e(TAG, fundInfo.getFundName());

                if (!alreadyPresent(fundInfo)) {
                    mFundsArrayList.add(fundInfo);
                }
//                readFIrebaseDataOnce();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                FundInfo fundInfo = dataSnapshot.getValue(FundInfo.class);
                reflectChange(fundInfo);
                calculateNetWorth();
                storeNetWorthInFirebase();
//                readFIrebaseDataOnce();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                FundInfo fundInfo = dataSnapshot.getValue(FundInfo.class);
                reflectRemoval(fundInfo);
                calculateNetWorth();
                storeNetWorthInFirebase();
//                readFIrebaseDataOnce();
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
                    mFundsArrayList.remove(i);
                    mFundsArrayList.add(i, fundInfo);
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
        Log.e(TAG,"reflect change executed");
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

    public void setNetWorth() {
        String netWorthStr = getString(R.string.rupee_symbol) + String.format("%.2f", mNetWorth);
        mNetWorthAmountTextView.setText(netWorthStr);
    }

    public void storeNetWorthInFirebase() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String date = dateFormat.format(calendar.getTime());
        DatabaseReference netWorthRef = mDatabase.getReference(
                mFirebaseUser.getUid()).child(getString(R.string.key_net_worth));
        netWorthRef.child(date).setValue(String.format("%.2f", mNetWorth));
    }

    @Override
    public void updateNetWorth() {
//        setNetWorth();
//        setNetWorthChange();
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
                Object value = dataSnapshot.getValue();
                String date = dataSnapshot.getKey();
                String netWorth = String.valueOf(value);
                if (date.equals(mToday)) {
                    netWorth = getString(R.string.rupee_symbol) + netWorth;
                    mNetWorthAmountTextView.setText(netWorth);
                }
                processGraphData(dataSnapshot);
                calculateNetWorthChange();
                setNetWorthChange();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                changeNetworthList(dataSnapshot);
                calculateNetWorthChange();
                setNetWorth();
                setNetWorthChange();
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
                getString(R.string.net_worth_graph_value));
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
        mChart.setDescription(getString(R.string.net_worth_graph_description));
        mChart.setData(data);
        mChart.animateY(0);
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
        populateChart();
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
        for (int i = 0; i < mGraphList.size(); i++) {
            mEntries.add(i, new Entry(i, Float.valueOf(mGraphList.get(i).getNetworth())));
            mLabels.add(i, mGraphList.get(i).getDate().toString().substring(4, 10));
        }
        populateChart();
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
            mNetWorthChange = String.format("%.2f",Math.abs(changeValue)) + "(" + changePercentStr + "%)";
            Log.e(TAG, "netChPerc: " + String.valueOf(changeValue) + " " + String.valueOf(changePercent) + "%");
        } else if (mGraphList.size() == 1) {
            netWorthLatest = Float.parseFloat(mGraphList.get(mGraphList.size() - 1).getNetworth());
            netWorthPrevious = 0.0f;
            changeValue = netWorthLatest - netWorthPrevious;
            changePercent = Math.abs(changeValue) * 100 / netWorthPrevious;
            String changePercentStr = String.format("%.2f", changePercent);
            if (changeValue < 0) {
                mIsNetChangeNegative = true;
            }
            mNetWorthChange = String.format("%.2f",Math.abs(changeValue)) + "(" + changePercentStr + "%)";
            Log.e(TAG, "netChPerc: " + String.valueOf(changeValue) + " " + String.valueOf(changePercent) + "%");
        }
    }

    public void setNetWorthChange() {
        if (mNetWorthChange != null) {
            mNetWorthChangeTextView.setText(mNetWorthChange);
            if (mIsNetChangeNegative) {
                mNetWorthChangeTextView.setTextColor(getResources().getColor(R.color.colorRed));
                mChangeArrow.setImageResource(R.drawable.ic_action_down);
                mNetWorthAmountTextView.setTextColor(getResources().getColor(R.color.colorRed));
            } else {
                mNetWorthChangeTextView.setTextColor(getResources().getColor(R.color.colorGreen));
                mChangeArrow.setImageResource(R.drawable.ic_action_up);
                mNetWorthAmountTextView.setTextColor(getResources().getColor(R.color.colorGreen));
            }
        }
    }
}
