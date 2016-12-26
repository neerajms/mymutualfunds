package com.neerajms99b.neeraj.mymutualfunds.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import com.neerajms99b.neeraj.mymutualfunds.R;
import com.neerajms99b.neeraj.mymutualfunds.adapter.SearchResultsListAdapter;
import com.neerajms99b.neeraj.mymutualfunds.data.FundsContentProvider;
import com.neerajms99b.neeraj.mymutualfunds.service.FundsIntentService;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    private Context mContext;

    private final String TAG = SearchActivity.class.getSimpleName();
    private final String KEY_ARRAYLIST = "arraylist";
    private final String KEY_SCODESLIST = "scodeslist";

    private ArrayList<String> mArrayList;
    private ArrayList<String> mScodesList;

    private SearchView mSearchView;

    private SearchResultsListAdapter mSearchResultsListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(null);

        mArrayList = new ArrayList<>();
        mScodesList = new ArrayList<>();

        mContext = this;

        mSearchView = (SearchView) findViewById(R.id.search);
        initializeSearchView();

        mSearchResultsListAdapter = new SearchResultsListAdapter(this, null);

        RecyclerView searchResultsRecyclerView = (RecyclerView) findViewById(R.id.search_results_recycler_view);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchResultsRecyclerView.setAdapter(mSearchResultsListAdapter);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(KEY_ARRAYLIST, mArrayList);
        outState.putStringArrayList(KEY_SCODESLIST, mScodesList);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras().containsKey(getString(R.string.key_toast_message))) {
                Toast.makeText(context,
                        intent.getExtras().getString(getString(R.string.key_toast_message)),
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void initializeSearchView() {
        mSearchView.setIconified(false);
        mSearchView.setLayoutParams(new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT));
        mSearchView.setQueryHint(getString(R.string.search_hint));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 1) {
                    Uri uri = Uri.parse(FundsContentProvider.mUriFullFundsList.toString() + "/" + newText);
                    new QueryAsyncTask().execute(uri);
                } else {
                    mSearchResultsListAdapter.swapCursor(null);
                }
                return false;
            }
        });
    }

    public class QueryAsyncTask extends AsyncTask<Uri, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Uri... uris) {
            return getContentResolver().query(uris[0], null, null, null, null);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            if (cursor != null && cursor.moveToFirst()) {
                mSearchResultsListAdapter.swapCursor(cursor);
            } else {
                mSearchResultsListAdapter.swapCursor(null);
            }
        }
    }

    public void onSelectFund(String scode) {
        Intent intent = new Intent(mContext, FundsIntentService.class);
        intent.putExtra(getString(R.string.key_tag), getString(R.string.tag_add_fund));
        intent.putExtra(getString(R.string.key_scode), scode);
        startService(intent);
    }
}
