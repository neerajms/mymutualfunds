package com.neerajms99b.neeraj.mymutualfunds.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.SearchView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.neerajms99b.neeraj.mymutualfunds.R;
import com.neerajms99b.neeraj.mymutualfunds.data.FundsContentProvider;

/**
 * Created by neeraj on 25/9/16.
 */

public class SearchSuggestionsAdapter extends CursorAdapter {
    private Cursor mCursor;
    private TextView mTextView;

    public SearchSuggestionsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mCursor = c;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_suggestions_list_item, parent, false);
        SearchView.LayoutParams layoutParams = new SearchView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.setLayoutParams(layoutParams);
        mTextView = (TextView) linearLayout.findViewById(R.id.search_suggestion_text);
        mTextView.setGravity(Gravity.CENTER_VERTICAL);
        return linearLayout;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        mTextView.setText(cursor.getString(cursor.getColumnIndex(FundsContentProvider.FUND_NAME)));
    }
}
