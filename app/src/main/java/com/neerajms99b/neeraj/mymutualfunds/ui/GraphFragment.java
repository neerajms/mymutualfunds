package com.neerajms99b.neeraj.mymutualfunds.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.neerajms99b.neeraj.mymutualfunds.R;
import com.neerajms99b.neeraj.mymutualfunds.data.FundsContentProvider;
import com.neerajms99b.neeraj.mymutualfunds.service.FundsIntentService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class GraphFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private final String TAG = GraphFragment.class.getSimpleName();
    private String mScode;
    public int CURSOR_LOADER_ID = 0;
    private Context mContext;
    private ArrayList<String> mLabels;
    private ArrayList<String> mEntriesString;
    private ArrayList<Entry> mEntries;
    private LineChart mChart;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mScode = bundle.getString(getString(R.string.key_scode));
        mContext = getContext();
        mEntriesString = new ArrayList<String>();
        mLabels = new ArrayList<String>();
        mEntries = new ArrayList<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_graph, container, false);
        mChart = (LineChart) rootView.findViewById(R.id.chart);
        // Inflate the layout for this fragment
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
        return rootView;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Uri uri = Uri.parse(FundsContentProvider.mUriHistorical.toString() + "/" + mScode);
        return new CursorLoader(getActivity(), uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        if (data.moveToFirst()) {
//            Log.d(TAG, data.getString(data.getColumnIndex(FundsContentProvider.FUND_SCODE)) + "\n" +
//                    data.getString(data.getColumnIndex(FundsContentProvider.NAV_Q12)) + "\n" +
//                    data.getString(data.getColumnIndex(FundsContentProvider.NAV_Q11)) + "\n" +
//                    data.getString(data.getColumnIndex(FundsContentProvider.NAV_Q10)) + "\n" +
//                    data.getString(data.getColumnIndex(FundsContentProvider.NAV_Q9)) + "\n" +
//                    data.getString(data.getColumnIndex(FundsContentProvider.NAV_Q8)) + "\n" +
//                    data.getString(data.getColumnIndex(FundsContentProvider.NAV_Q7)) + "\n" +
//                    data.getString(data.getColumnIndex(FundsContentProvider.NAV_Q6)) + "\n" +
//                    data.getString(data.getColumnIndex(FundsContentProvider.NAV_Q5)) + "\n" +
//                    data.getString(data.getColumnIndex(FundsContentProvider.NAV_Q4)) + "\n" +
//                    data.getString(data.getColumnIndex(FundsContentProvider.NAV_Q3)) + "\n" +
//                    data.getString(data.getColumnIndex(FundsContentProvider.NAV_Q2)) + "\n" +
//                    data.getString(data.getColumnIndex(FundsContentProvider.NAV_Q1)) + "\n");
            postExecute(data);
            populateChart();

        } else {
            Intent intentService = new Intent(getContext(), FundsIntentService.class);
            intentService.putExtra("tag", getString(R.string.tag_fetch_graph_data));
            intentService.putExtra(getString(R.string.key_scode), mScode);
            getActivity().startService(intentService);
//            getLoaderManager().restartLoader(CURSOR_LOADER_ID,null,this);
        }
    }

    BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Log.d(TAG,"broadcast received");
//            Bundle bundle = intent.getExtras();
            if (intent.getExtras().containsKey(getString(R.string.key_graph_fetched))) {
//                if (bundle.getBoolean(getString(R.string.key_graph_fetched))){
                restartLoader();
//                }
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                mMessageReceiver, new IntentFilter(getResources().getString(R.string.gcmtask_intent)));
    }

    public void restartLoader() {
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
        Log.d(TAG, "loader restarted");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mMessageReceiver);
    }

    public void postExecute(Cursor data) {
        data.moveToFirst();
        String keyQ = "q";
        Calendar date = Calendar.getInstance();
        SimpleDateFormat dateFormatYear = new SimpleDateFormat("yyyy");
        SimpleDateFormat dateFormatMonth = new SimpleDateFormat("MM");
        String yearString = dateFormatYear.format(date.getTime());
        String monthString = dateFormatMonth.format(date.getTime());
        int currentQuarter = Integer.valueOf(monthString) / 4;
        int monthInt = 0;
        int yearInt = Integer.valueOf(yearString);
        Log.d(TAG, String.valueOf(currentQuarter));
        switch (currentQuarter) {
            case 0:
                monthInt = 12;
                yearInt = yearInt - 1;
                break;
            case 1:
                monthInt = 3;
                break;
            case 2:
                monthInt = 6;
                break;
            default:
                monthInt = 9;
        }
        int i = 12;
        int j = 0;
//        if (currentQuarter != 0) {
//            currentQuarter = currentQuarter - 1;
//        }
        while (i >= 1) {
//            while (monthInt >= 3) {
//                String value = data.getString(data.getColumnIndex(keyQ + String.valueOf(i)));
            if (monthInt == 0){
                monthInt = 12;
            }
            switch (monthInt){
                case 12:
                    currentQuarter = 3;
                    break;
                case 9:
                    currentQuarter = 2;
                    break;
                case 6:
                    currentQuarter = 1;
                    break;
                case 3:
                    currentQuarter = 4;
                    yearInt--;
                    break;
            }

//                data.moveToFirst();
            String key = "q" + String.valueOf(i);
            if (data.getString(data.getColumnIndex(key)) != null) {
                mEntriesString.add(j, data.getString(data.getColumnIndex(keyQ + String.valueOf(i))));
                mLabels.add(j, String.valueOf(currentQuarter) + "." + String.valueOf(yearInt));
                j++;
            }
            monthInt = monthInt - 3;
//            currentQuarter--;
//            }

//            monthInt = 12;
            i--;
        }
        int l = 0;
        for (int k = mLabels.size() - 1; k >= 0; k--) {
            mEntries.add(new Entry(l,Float.parseFloat(mEntriesString.get(k))));
            l++;
            Log.d(mLabels.get(k), mEntriesString.get(k));
        }
    }

    public void populateChart() {
        LineDataSet lineDataSet = new LineDataSet(mEntries,
                getString(R.string.stock_values));
        lineDataSet.setDrawCircles(false);
//        lineDataSet.setDrawCubic(true);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillColor(getResources().getColor(android.R.color.holo_blue_light));
        lineDataSet.setColor(getResources().getColor(android.R.color.holo_blue_light), 220);
        lineDataSet.setFillAlpha(220);
        lineDataSet.setDrawValues(false);
//
        YAxis yAxisLeft = mChart.getAxisLeft();
        yAxisLeft.setTextColor(getResources().getColor(android.R.color.black));
//
        YAxis yAxisRight = mChart.getAxisRight();
        yAxisRight.setTextColor(getResources().getColor(android.R.color.black));
//
        XAxis xAxis = mChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setAvoidFirstLastClipping(true);
//        xAxis.setSpaceBetweenLabels(0);
        xAxis.setTextColor(getResources().getColor(android.R.color.black));
//        xAxis.setSpaceBetweenLabels(2);
//
        LineData data = new LineData(lineDataSet);
        mChart.setDescription(getString(R.string.chart_description));
        mChart.setData(data);
        mChart.animateY(0);
    }
}
