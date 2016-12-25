package com.neerajms99b.neeraj.mymutualfunds.adapter;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.neerajms99b.neeraj.mymutualfunds.R;
import com.neerajms99b.neeraj.mymutualfunds.data.FundsContentProvider;
import com.neerajms99b.neeraj.mymutualfunds.ui.SearchActivity;

/**
 * Created by neeraj on 25/12/16.
 */

public class SearchResultsListAdapter extends CursorRecyclerViewAdapter<SearchResultsListAdapter.ViewHolder> {
    private SearchActivity mCallBack;
    public SearchResultsListAdapter(SearchActivity searchActivity, Cursor cursor) {
        super(searchActivity, cursor);
        mCallBack = searchActivity;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout mLinearLayout;
        TextView mFundNameTextView;

        public ViewHolder(LinearLayout linearLayout, TextView fundNameTextView) {
            super(linearLayout);
            mLinearLayout = linearLayout;
            mFundNameTextView = fundNameTextView;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor, int position) {
        viewHolder.mFundNameTextView.setText(
                cursor.getString(cursor.getColumnIndex(FundsContentProvider.FUND_NAME)));
        viewHolder.mFundNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_results_list_item, parent, false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.setLayoutParams(layoutParams);
        TextView fundName = (TextView) linearLayout.findViewById(R.id.list_item);
        ViewHolder viewHolder = new ViewHolder(linearLayout, fundName);
        return viewHolder;
    }
}
