package com.neerajms99b.neeraj.mymutualfunds.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
    private String mFundName;
    private String mFundNav;
    private String mUnits;
    private String mTotalValue;
    public int CURSOR_LOADER_ID = 0;
    private Context mContext;
    private ArrayList<String> mLabels;
    private ArrayList<String> mEntriesString;
    private ArrayList<Entry> mEntries;
    private LineChart mChart;
    private int mCurrentQuarter;
    private int mCurrentYear;
    private ProgressBar mProgressBarNavGraph;
    private NetworkReceiver mNetworkReceiver;
    private boolean mTriggerFetch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mScode = bundle.getString(getString(R.string.key_scode));
        mFundName = bundle.getString(getString(R.string.key_fundname));
        mFundNav = bundle.getString(getString(R.string.key_fund_nav));
        mUnits = bundle.getString(getString(R.string.key_units_in_hand));
        mTotalValue = getString(R.string.rupee_symbol) +
                String.format("%.2f", Float.parseFloat(mFundNav) * Float.parseFloat(mUnits));

        mContext = getContext();
        mEntriesString = new ArrayList<String>();
        mLabels = new ArrayList<String>();
        mEntries = new ArrayList<>();

        findCurrentQuarterAndYear();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_graph, container, false);
        mChart = (LineChart) rootView.findViewById(R.id.chart);
        mChart.setVisibility(View.INVISIBLE);
        mProgressBarNavGraph = (ProgressBar) rootView.findViewById(R.id.progress_bar_nav_graph);
        TextView fundNameTextView = (TextView) rootView.findViewById(R.id.fund_name);
        TextView totalValueTextView = (TextView) rootView.findViewById(R.id.total_value);
        fundNameTextView.setText(mFundName);
        totalValueTextView.setText(mTotalValue);
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
        //If the NAV values are not available in the DB then fetch data
        if (data.moveToFirst() &&
                data.getString(data.getColumnIndex(FundsContentProvider.LAST_UPDATED)) != null) {
            String lastUpdated = data.getString(data.getColumnIndex(FundsContentProvider.LAST_UPDATED));
            String[] words = lastUpdated.split("-");
            int lastUpdatedQuarter = Integer.parseInt(words[0]);
            int lastUpdatedYear = Integer.parseInt(words[1]);
            /*If the values were last updated in one of the previous quarters then fetch data,
            * otherwise show data from DB*/
            if (lastUpdatedYear == mCurrentYear) {
                if (mCurrentQuarter > lastUpdatedQuarter) {
                    triggerDataFetch();
                } else {
                    postExecute(data);
                    populateChart();
                }
            } else if (mCurrentYear > lastUpdatedYear) {
                triggerDataFetch();
            }
        } else {
            triggerDataFetch();
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

    public void triggerDataFetch() {
        if (isInternetOn(mContext)) {
            mProgressBarNavGraph.setVisibility(View.VISIBLE);
            Intent intentService = new Intent(getContext(), FundsIntentService.class);
            intentService.putExtra(getString(R.string.key_tag), getString(R.string.tag_fetch_graph_data));
            intentService.putExtra(getString(R.string.key_scode), mScode);
            getActivity().startService(intentService);
        } else {
            mProgressBarNavGraph.setVisibility(View.INVISIBLE);
            messageNotConnected();
            mTriggerFetch = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mNetworkReceiver = new NetworkReceiver();
        mContext.registerReceiver(mNetworkReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
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
        mContext.unregisterReceiver(mNetworkReceiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mMessageReceiver);
    }

    public void findCurrentQuarterAndYear() {
        Calendar date = Calendar.getInstance();
        SimpleDateFormat dateFormatYear = new SimpleDateFormat("yyyy");
        SimpleDateFormat dateFormatMonth = new SimpleDateFormat("MM");
        String yearString = dateFormatYear.format(date.getTime());
        String monthString = dateFormatMonth.format(date.getTime());
        mCurrentQuarter = Integer.valueOf(monthString) / 4;
        mCurrentYear = Integer.parseInt(yearString);
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
                getString(R.string.nav_chart_values));
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawFilled(false);
        if (Build.VERSION.SDK_INT >= 23) {
            lineDataSet.setColor(ContextCompat.getColor(getContext(), R.color.colorAccent), 220);
        } else {
            lineDataSet.setColor(getResources().getColor(R.color.colorAccent), 220);
        }
        lineDataSet.setDrawValues(false);
        lineDataSet.setLineWidth(3.5f);

        YAxis yAxisLeft = mChart.getAxisLeft();
        if (Build.VERSION.SDK_INT >= 23) {
            yAxisLeft.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
        } else {
            yAxisLeft.setTextColor(getResources().getColor(android.R.color.black));
        }
        yAxisLeft.setDrawGridLines(false);

        YAxis yAxisRight = mChart.getAxisRight();
        yAxisRight.setDrawLabels(false);
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
        if (Build.VERSION.SDK_INT >= 23) {
            xAxis.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
        } else {
            xAxis.setTextColor(getResources().getColor(android.R.color.black));
        }
        xAxis.setValueFormatter(formatter);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        LineData data = new LineData(lineDataSet);
        mChart.setDescription(getString(R.string.nav_chart_description));
        mChart.setData(data);
        mChart.animateY(0);
        mProgressBarNavGraph.setVisibility(View.INVISIBLE);
        mChart.setVisibility(View.VISIBLE);
    }

    public class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isInternetOn(context)) {
                if (mTriggerFetch) {
                    triggerDataFetch();
                    mTriggerFetch = false;
                }
            } else {
                mProgressBarNavGraph.setVisibility(View.INVISIBLE);
                messageNotConnected();
            }
        }
    }

    public boolean isInternetOn(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public void messageNotConnected() {
        Toast.makeText(mContext, getString(R.string.message_not_connected), Toast.LENGTH_SHORT).show();
    }
}
