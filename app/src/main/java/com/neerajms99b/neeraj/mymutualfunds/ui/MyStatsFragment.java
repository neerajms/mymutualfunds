package com.neerajms99b.neeraj.mymutualfunds.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private String mTodaysDate;
    private ArrayList<String> mLabels;
    private ArrayList<String> mEntriesString;
    private ArrayList<Entry> mEntries;
    private LineChart mChart;
    private int mLabelIndex;
    private ArrayList<NetWorthGraphModel> mGraphList;

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
        getTodaysDate();

        if (savedInstanceState != null && savedInstanceState.containsKey(getString(R.string.funds_array_list))) {
            mFundsArrayList = savedInstanceState.getParcelableArrayList(getString(R.string.funds_array_list));
            calculateNetWorth();
        } else {

        }
        readFirebaseFundsList();

        setHasOptionsMenu(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stats, container, false);
        mChart = (LineChart) rootView.findViewById(R.id.chart_networth);
        readFirebaseNetworthList();
        mNetWorthAmountTextView = (TextView) rootView.findViewById(R.id.net_worth_amount);
        Log.e(TAG, "oncreateview");
        setNetWorth();
//        populateChart();
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(getString(R.string.funds_array_list), mFundsArrayList);
    }

    public void readFirebaseFundsList() {
        DatabaseReference fundsListReference =
                mDatabase.getReference(mFirebaseUser.getUid()).child(getString(R.string.firebase_child_funds));
        fundsListReference.keepSynced(true);
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                FundInfo fundInfo = dataSnapshot.getValue(FundInfo.class);
                if (!alreadyPresent(fundInfo)) {
                    mFundsArrayList.add(fundInfo);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                FundInfo fundInfo = dataSnapshot.getValue(FundInfo.class);
                reflectChange(fundInfo);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                FundInfo fundInfo = dataSnapshot.getValue(FundInfo.class);
                reflectRemoval(fundInfo);
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
        for (int i = 0; i < mFundsArrayList.size(); i++) {
            if (mFundsArrayList.get(i).getScode().equals(fundInfo.getScode())) {
                mFundsArrayList.remove(i);
                mFundsArrayList.add(i, fundInfo);
                return true;
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
    }

    public void calculateNetWorth() {
        mNetWorth = 0;
        for (int index = 0; index < mFundsArrayList.size(); index++) {
            FundInfo fundInfo = mFundsArrayList.get(index);
            mNetWorth = mNetWorth + (Float.parseFloat(fundInfo.getNav())
                    * Float.parseFloat(fundInfo.getUnits()));
        }
        storeNetWorthInFirebase();
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
        calculateNetWorth();
        setNetWorth();
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
//                SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
                Object value = dataSnapshot.getValue();
                String date = dataSnapshot.getKey();
                String netWorth = String.valueOf(value);
//                mEntries.add(mLabelIndex, new Entry(mLabelIndex, Float.valueOf(netWorth)));
//                mLabels.add(mLabelIndex, date.substring(0, 5));
                if (date.equals(mTodaysDate)) {
                    netWorth = getString(R.string.rupee_symbol) + netWorth;
                    mNetWorthAmountTextView.setText(netWorth);
                }

//                mChart.notifyDataSetChanged();
//                populateChart();
//                mLabelIndex++;
//                Log.e(TAG, "networth count:" + String.valueOf(mEntries.size()));
                processGraphData(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//                String netWorth = dataSnapshot.getValue().toString();
                changeNetworthList(dataSnapshot);
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

    public void getTodaysDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        mTodaysDate = format.format(calendar.getTime());
    }

    public void populateChart() {
        LineDataSet lineDataSet = new LineDataSet(mEntries,
                getString(R.string.stock_values));
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
        mChart.setDescription(getString(R.string.chart_description));
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
            mEntries.add(index,new Entry(index,Float.valueOf(mGraphList.get(index).getNetworth())));
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
//            mEntries.addAll(mGraphList);
        } catch (ParseException pe) {
            Log.e(TAG, pe.toString());
        }
        mEntries.clear();
        mLabels.clear();
        for (int i = 0; i < mGraphList.size(); i++) {

//            Log.e(TAG,mGraphList.get(i).getDate().toString());
//            Log.e(TAG,"entries:"+mGraphList.get(i).getDate());
            mEntries.add(i, new Entry(i, Float.valueOf(mGraphList.get(i).getNetworth())));
            mLabels.add(i, mGraphList.get(i).getDate().toString().substring(4, 10));
        }
        populateChart();
    }
}
