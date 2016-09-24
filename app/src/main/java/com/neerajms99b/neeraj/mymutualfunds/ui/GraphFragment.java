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
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
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
            postExecute(data);
            populateChart();

        } else {
            Intent intentService = new Intent(getContext(), FundsIntentService.class);
            intentService.putExtra("tag", getString(R.string.tag_fetch_graph_data));
            intentService.putExtra(getString(R.string.key_scode), mScode);
            getActivity().startService(intentService);
        }
    }

    BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras().containsKey(getString(R.string.key_graph_fetched))) {
                restartLoader();
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
        while (i >= 1) {
            if (monthInt == 0) {
                monthInt = 12;
            }
            switch (monthInt) {
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
            String key = "q" + String.valueOf(i);
            if (data.getString(data.getColumnIndex(key)) != null) {
                mEntriesString.add(j, data.getString(data.getColumnIndex(keyQ + String.valueOf(i))));
                mLabels.add(j, "Q" + String.valueOf(currentQuarter) + " " + String.valueOf(yearInt).substring(2, 4));
                j++;
            }
            monthInt = monthInt - 3;
            i--;
        }
        int l = 0;
        for (int k = mLabels.size() - 1; k >= 0; k--) {
            mEntries.add(new Entry(l, Float.parseFloat(mEntriesString.get(k))));
            l++;
            Log.d(mLabels.get(k), mEntriesString.get(k));
        }
    }

    public void populateChart() {
        LineDataSet lineDataSet = new LineDataSet(mEntries,
                getString(R.string.stock_values));
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawFilled(false);
//        lineDataSet.setFillColor(getResources().getColor(android.R.color.holo_blue_light));
        lineDataSet.setColor(getResources().getColor(android.R.color.holo_blue_light), 220);
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
                return mLabels.get((mLabels.size() - 1) - (int) value);
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
}
