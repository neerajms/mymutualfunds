package com.neerajms99b.neeraj.mymutualfunds.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.neerajms99b.neeraj.mymutualfunds.R;
import com.neerajms99b.neeraj.mymutualfunds.adapter.FundsListAdapter;
import com.neerajms99b.neeraj.mymutualfunds.adapter.SimpleItemTouchHelper;
import com.neerajms99b.neeraj.mymutualfunds.data.FundsContentProvider;

/**
 * A placeholder fragment containing a simple view.
 */
public class FundsListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private FundsListAdapter mFundsListAdapter;
    private RecyclerView mRecyclerView;
    public int CURSOR_LOADER_ID = 0;
    private TextView mNetWorthAmount;
    private String mNetWorth;
    private ItemTouchHelper mItemTouchHelper;
    private ItemTouchHelper.Callback mItemTouchCallBack;
    private MainActivity mCallBack;
    public final String ARG_OBJECT = "funds_list";
    private FloatingActionButton mFab;

    public FundsListFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mCallBack = (MainActivity) getActivity();
//        mNetWorthAmount = (TextView) rootView.findViewById(R.id.net_worth_amount);
//        mNetWorthAmount.setText("₹0.00");
        mFundsListAdapter = new FundsListAdapter(null, this);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.funds_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mFundsListAdapter);
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        mItemTouchCallBack = new SimpleItemTouchHelper(mFundsListAdapter);
        mItemTouchHelper = new ItemTouchHelper(mItemTouchCallBack);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        mFab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSearchActivity();
            }
        });
        mFab.hide();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_fund) {
            startSearchActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), FundsContentProvider.mUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mFundsListAdapter.swapCursor(data);

        if (data.moveToFirst()) {
//            setNetWorthAmount(data);
            mFab.hide();
        } else {
            mFab.show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        restartLoader();
    }

    public void setNetWorthAmount(Cursor data) {
        int units = 0;
        double nav = 0.0d;
        double netWorth = 0.0d;
        do {
            Log.d("inside", "while");
            units = 0;
            nav = 0.0d;
            if (data.getString(data.getColumnIndex(FundsContentProvider.UNITS_OWNED)) != null) {
                units = data.getInt(data.getColumnIndex(FundsContentProvider.UNITS_OWNED));
                nav = data.getDouble(data.getColumnIndex(FundsContentProvider.FUND_NAV));
                netWorth = netWorth + units * nav;
            }
        } while (data.moveToNext());
        mNetWorth = String.format("%.2f", netWorth);
//        mNetWorthAmount.setText("₹" + mNetWorth);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mFundsListAdapter.swapCursor(null);
    }

    public void editClicked(String scode) {
        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.key_scode), scode);
        UnitsInputDialogFragment unitsInputDialogFragment = new UnitsInputDialogFragment();
        unitsInputDialogFragment.setArguments(bundle);
        unitsInputDialogFragment.setTargetFragment(FundsListFragment.this, 0);
        unitsInputDialogFragment.show(getFragmentManager(), null);
    }

    public void unitsInput(String units, String scode) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(FundsContentProvider.UNITS_OWNED, units);
        String[] selectionArgs = {scode};
        getContext().getContentResolver().update(FundsContentProvider.mUri, contentValues, FundsContentProvider.KEY_ID, selectionArgs);
        restartLoader();
    }

    public void startSearchActivity() {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        startActivity(intent);
    }

    public void restartLoader() {
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
    }
}
