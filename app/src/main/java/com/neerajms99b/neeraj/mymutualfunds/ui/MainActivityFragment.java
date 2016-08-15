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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.neerajms99b.neeraj.mymutualfunds.R;
import com.neerajms99b.neeraj.mymutualfunds.adapter.FundsListAdapter;
import com.neerajms99b.neeraj.mymutualfunds.data.FundsContentProvider;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private FundsListAdapter mFundsListAdapter;
    private RecyclerView mRecyclerView;
    private int CURSOR_LOADER_ID = 0;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//        mFundsListAdapter = new FundsListAdapter(null);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.funds_recycler_view);
        mRecyclerView.setAdapter(mFundsListAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), FundsContentProvider.mUri,null,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mFundsListAdapter = new FundsListAdapter(data,this);
        mRecyclerView.setAdapter(mFundsListAdapter);
        mFundsListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void editClicked(String scode){
        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.key_scode),scode);
        UnitsInputDialogFragment unitsInputDialogFragment = new UnitsInputDialogFragment();
        unitsInputDialogFragment.setArguments(bundle);
        unitsInputDialogFragment.setTargetFragment(MainActivityFragment.this,0);
        unitsInputDialogFragment.show(getFragmentManager(),null);
    }
    public void unitsInput(String units, String scode){
        ContentValues contentValues = new ContentValues();
        contentValues.put(FundsContentProvider.UNITS_OWNED,units);
        String[] selectionArgs = {scode};
        getContext().getContentResolver().update(FundsContentProvider.mUri,contentValues,FundsContentProvider.KEY_ID,selectionArgs);
        getLoaderManager().restartLoader(CURSOR_LOADER_ID,null,this);
//        mFundsListAdapter.notifyDataSetChanged();
    }
}
