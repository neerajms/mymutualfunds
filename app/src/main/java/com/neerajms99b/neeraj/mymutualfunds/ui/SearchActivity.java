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
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.neerajms99b.neeraj.mymutualfunds.R;
import com.neerajms99b.neeraj.mymutualfunds.data.BasicFundInfoParcelable;
import com.neerajms99b.neeraj.mymutualfunds.service.FundsIntentService;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    private final String TAG = SearchActivity.class.getSimpleName();
    private ArrayList<String> mArrayList;
    private ArrayList<String> mScodesList;
    private ArrayAdapter<String> mFundsListAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private final String KEY_ARRAYLIST = "arraylist";
    private final String KEY_SCODESLIST = "scodeslist";
    private UnitsInputDialogFragment mUnitsInputDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(null);

        final Context context = this;
        final SearchView search = (SearchView) findViewById(R.id.search);
        search.setIconified(false);
        search.setLayoutParams(new Toolbar.LayoutParams(Gravity.RIGHT));
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

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
        mScodesList = new ArrayList<String>();
        mFundsListAdapter = new ArrayAdapter<String>(this, R.layout.search_results_list_item,
                R.id.list_item, mArrayList);
        ListView fundsListView = (ListView) findViewById(R.id.funds_listview);
        fundsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intentService = new Intent(context, FundsIntentService.class);
                intentService.putExtra("tag", getString(R.string.tag_search_scode));
                intentService.putExtra(getString(R.string.key_scode), mScodesList.get(i));
                startService(intentService);
            }
        });
        fundsListView.setAdapter(mFundsListAdapter);
        if (savedInstanceState != null
                && savedInstanceState.containsKey(KEY_ARRAYLIST)
                && savedInstanceState.containsKey(KEY_SCODESLIST)) {
            mFundsListAdapter.clear();
            mArrayList = savedInstanceState.getStringArrayList(KEY_ARRAYLIST);
            mFundsListAdapter.addAll(mArrayList);
            mScodesList = savedInstanceState.getStringArrayList(KEY_SCODESLIST);
        } else {
            mArrayList = new ArrayList<String>();
            mFundsListAdapter.addAll(mArrayList);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras().containsKey(getString(R.string.search_data_bundle))) {
                ArrayList<BasicFundInfoParcelable> arrayList = new ArrayList<BasicFundInfoParcelable>();
                arrayList = intent.getExtras().getBundle(getString(R.string.search_data_bundle))
                        .getParcelableArrayList(getString(R.string.basic_search_results_parcelable));
                populateArrayList(arrayList);
                showList();
                mSwipeRefreshLayout.setRefreshing(false);
            } else if (intent.getExtras().containsKey(getString(R.string.key_toast_message))) {
                Toast.makeText(context,
                        intent.getExtras().getString(getString(R.string.key_toast_message)),
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void populateArrayList(ArrayList<BasicFundInfoParcelable> arrayList) {
        mArrayList.clear();
        mScodesList.clear();
        for (int index = 0; index < arrayList.size(); index++) {
            mArrayList.add(arrayList.get(index).mFundName);
            mScodesList.add(arrayList.get(index).mScode);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(KEY_ARRAYLIST, mArrayList);
        outState.putStringArrayList(KEY_SCODESLIST, mScodesList);

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
                mMessageReceiver, new IntentFilter(getResources().getString(R.string.gcmtask_intent)));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }
}
