package com.neerajms99b.neeraj.mymutualfunds.ui;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.neerajms99b.neeraj.mymutualfunds.R;
import com.neerajms99b.neeraj.mymutualfunds.adapter.FundsListAdapter;
import com.neerajms99b.neeraj.mymutualfunds.data.FundsContentProvider;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private FundsListAdapter mFundsListAdapter;
    private RecyclerView mRecyclerView;
    private int CURSOR_LOADER_ID = 0;
    private TextView mNetWorthAmount;
    private String mNetWorth;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mNetWorthAmount = (TextView) rootView.findViewById(R.id.net_worth_amount);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.funds_recycler_view);
        mRecyclerView.setAdapter(mFundsListAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), FundsContentProvider.mUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mFundsListAdapter = new FundsListAdapter(data, this);
        mRecyclerView.setAdapter(mFundsListAdapter);
        mFundsListAdapter.notifyDataSetChanged();
        setNetWorthAmount(data);
    }

    public void setNetWorthAmount(Cursor data) {
        int units = 0;
        double nav = 0.0d;
        double netWorth = 0.0d;
        data.moveToFirst();
        do{
            Log.d("inside","while");
            units = 0;
            nav = 0.0d;
            if (data.getString(data.getColumnIndex(FundsContentProvider.UNITS_OWNED)) != null) {
                units = data.getInt(data.getColumnIndex(FundsContentProvider.UNITS_OWNED));
                nav = data.getDouble(data.getColumnIndex(FundsContentProvider.FUND_NAV));
                netWorth = netWorth + units * nav;
            }
        }while (data.moveToNext());
        mNetWorth = String.format("%.2f", netWorth);
        mNetWorthAmount.setText("₹" + mNetWorth);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void editClicked(String scode) {
        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.key_scode), scode);
        UnitsInputDialogFragment unitsInputDialogFragment = new UnitsInputDialogFragment();
        unitsInputDialogFragment.setArguments(bundle);
        unitsInputDialogFragment.setTargetFragment(MainActivityFragment.this, 0);
        unitsInputDialogFragment.show(getFragmentManager(), null);
    }

    public void unitsInput(String units, String scode) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(FundsContentProvider.UNITS_OWNED, units);
        String[] selectionArgs = {scode};
        getContext().getContentResolver().update(FundsContentProvider.mUri, contentValues, FundsContentProvider.KEY_ID, selectionArgs);
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
    }
}
