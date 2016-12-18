package com.neerajms99b.neeraj.mymutualfunds.ui;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.neerajms99b.neeraj.mymutualfunds.R;
import com.neerajms99b.neeraj.mymutualfunds.adapter.SearchSuggestionsAdapter;
import com.neerajms99b.neeraj.mymutualfunds.data.FundsContentProvider;
import com.neerajms99b.neeraj.mymutualfunds.models.BasicFundInfoParcelable;
import com.neerajms99b.neeraj.mymutualfunds.service.FundsIntentService;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    private Context mContext;

    private final String TAG = SearchActivity.class.getSimpleName();
    private final String KEY_ARRAYLIST = "arraylist";
    private final String KEY_SCODESLIST = "scodeslist";

    private ArrayList<String> mArrayList;
    private ArrayList<String> mScodesList;
    private ArrayAdapter<String> mFundsListAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private SearchSuggestionsAdapter mAdapter;
    private String mRecentString;
    private Cursor mCursor;
    private NetworkReceiver mNetworkReceiver;
    private SearchView mSearchView;
    private TextView mDisconnectedIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(null);

        mArrayList = new ArrayList<>();
        mScodesList = new ArrayList<>();

        mRecentString = FundsContentProvider.mUriRecentSearch.toString() + "/";
        mContext = this;
        mAdapter = new SearchSuggestionsAdapter(this, mCursor, 0);

        mSearchView = (SearchView) findViewById(R.id.search);
        initializeSearchView();

        mDisconnectedIndicator = (TextView) findViewById(R.id.disconnected_indicator);
        mDisconnectedIndicator.setVisibility(View.INVISIBLE);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_search_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(null);
        mSwipeRefreshLayout.setEnabled(false);
        if (Build.VERSION.SDK_INT >= 23) {
            mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(mContext, R.color.colorAccent));
        } else {
            mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        }

        mFundsListAdapter = new ArrayAdapter<>(this, R.layout.search_results_list_item,
                R.id.list_item, mArrayList);
        ListView fundsListView = (ListView) findViewById(R.id.funds_listview);
        fundsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intentService = new Intent(mContext, FundsIntentService.class);
                intentService.putExtra(getString(R.string.key_tag), getString(R.string.tag_search_scode));
                intentService.putExtra(getString(R.string.key_scode), mScodesList.get(i));
                intentService.putExtra(getString(R.string.key_fundname),mArrayList.get(i));
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
            mArrayList = new ArrayList<>();
            mFundsListAdapter.addAll(mArrayList);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.clear) {
            int deleted = getContentResolver().delete(FundsContentProvider.mUriRecentSearch, null, null);
            if (deleted > 0) {
                Toast.makeText(mContext, getString(R.string.history_cleared_toast), Toast.LENGTH_SHORT).show();
            } else if (deleted == 0) {
                Toast.makeText(mContext, getString(R.string.nothing_to_clear_toast), Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNetworkReceiver = new NetworkReceiver();
        mContext.registerReceiver(mNetworkReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(getResources().getString(R.string.gcmtask_intent)));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mContext.unregisterReceiver(mNetworkReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(KEY_ARRAYLIST, mArrayList);
        outState.putStringArrayList(KEY_SCODESLIST, mScodesList);
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
                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
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

    private void showList() {
        mFundsListAdapter.clear();
        mFundsListAdapter.addAll(mArrayList);
        mFundsListAdapter.notifyDataSetChanged();
    }

    public void initializeSearchView() {
        mSearchView.setIconified(false);
        mSearchView.setLayoutParams(new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT));
        mSearchView.setSuggestionsAdapter(mAdapter);
        mSearchView.setQueryHint(getString(R.string.search_hint));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                onSubmission(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() >= 1) {
                    Uri uri = Uri.parse(mRecentString + newText);
                    mCursor = getContentResolver().query(uri, null, null, null, null);
                    if (mCursor != null && mCursor.moveToFirst()) {
                        mAdapter.changeCursor(mCursor);
                        mAdapter.notifyDataSetChanged();
                    } else {
                        mAdapter.changeCursor(null);
                    }
                }
                return false;
            }
        });
        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = mAdapter.getCursor();
                onSubmission(cursor.getString(
                        cursor.getColumnIndex(FundsContentProvider.SEARCH_WORD)));
                return true;
            }
        });
    }

    public void onSubmission(String query) {
        mSwipeRefreshLayout.setRefreshing(true);
        Intent intentService = new Intent(mContext, FundsIntentService.class);
        intentService.putExtra(mContext.getString(R.string.key_tag), getString(R.string.tag_search_fund));
        intentService.putExtra(mContext.getString(R.string.key_fund_search_word), query);
        startService(intentService);
        ContentValues contentValues = new ContentValues();
        contentValues.put(FundsContentProvider.SEARCH_WORD, query);
        getContentResolver().insert(FundsContentProvider.mUriRecentSearch, contentValues);
    }

    public class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isInternetOn(context)) {
                mSearchView.setVisibility(View.VISIBLE);
                mDisconnectedIndicator.setVisibility(View.INVISIBLE);
            } else {
                mSearchView.setVisibility(View.INVISIBLE);
                mDisconnectedIndicator.setVisibility(View.VISIBLE);
            }
        }
    }

    public boolean isInternetOn(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
