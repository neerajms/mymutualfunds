package com.neerajms99b.neeraj.mymutualfunds.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.neerajms99b.neeraj.mymutualfunds.R;
import com.neerajms99b.neeraj.mymutualfunds.service.FundsIntentService;

import java.util.ArrayList;
import java.util.Arrays;

public class SearchActivity extends AppCompatActivity {
    private final String TAG = SearchActivity.class.getSimpleName();
    private ArrayList<String> mArrayList;
    private ArrayAdapter<String> mFundsListAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private final String KEY_ARRAYLIST = "arraylist";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Search Funds");

        final Context context = this;
        final SearchView search = (SearchView) findViewById(R.id.search);
        search.setIconified(false);
        search.setLayoutParams(new Toolbar.LayoutParams(Gravity.RIGHT));
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mFundsListAdapter.clear();
                Intent intentService = new Intent(context, FundsIntentService.class);
                intentService.putExtra("tag", "force");
                intentService.putExtra("fund", query);
                startService(intentService);
                mSwipeRefreshLayout.setRefreshing(true);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_search_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(null);
        mSwipeRefreshLayout.setEnabled(false);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        mArrayList = new ArrayList<String>();
        mFundsListAdapter = new ArrayAdapter<String>(this, R.layout.search_results_list_item,
                R.id.list_item, mArrayList);
        ListView fundsListView = (ListView) findViewById(R.id.funds_listview);
        fundsListView.setAdapter(mFundsListAdapter);
        if (savedInstanceState!=null && savedInstanceState.containsKey(KEY_ARRAYLIST)){
            mFundsListAdapter.clear();
            mArrayList = savedInstanceState.getStringArrayList(KEY_ARRAYLIST);
            mFundsListAdapter.addAll(mArrayList);
        }else{
            String[] list = {"Your search results will appear here"};
            mArrayList = new ArrayList<String>(Arrays.asList(list));
            mFundsListAdapter.addAll(mArrayList);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(getResources().getString(R.string.search_data_intent))) {
                mArrayList = intent.getExtras().getBundle(getString(R.string.search_data_bundle))
                        .getStringArrayList(getString(R.string.search_results_array_list));
                Log.d(TAG, mArrayList.get(1));
                showList();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(KEY_ARRAYLIST,mArrayList);

    }

    private void showList() {
        mFundsListAdapter.clear();
        mFundsListAdapter.addAll(mArrayList);
        mFundsListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(getResources().getString(R.string.search_data_intent)));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }
}
