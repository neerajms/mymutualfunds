package com.neerajms99b.neeraj.mymutualfunds.adapter;

import android.database.Cursor;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.neerajms99b.neeraj.mymutualfunds.R;
import com.neerajms99b.neeraj.mymutualfunds.data.FundsContentProvider;

/**
 * Created by neeraj on 9/8/16.
 */
public class FundsListAdapter extends RecyclerView.Adapter<FundsListAdapter.ViewHolder> {
    private Cursor mCursor;

    public FundsListAdapter(Cursor cursor){
        mCursor = cursor;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public CardView mFundCardView;
        public TextView mFundName;
        public TextView mFundNAV;
        public ViewHolder(CardView cardView,TextView fundNameTextView, TextView fundNAVTextView) {
            super(cardView);
            mFundCardView = cardView;
            mFundName = fundNameTextView;
            mFundNAV = fundNAVTextView;
        }

        @Override
        public void onClick(View view) {

        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        float density = parent.getResources().getDisplayMetrics().density;
        final int leftMargin = 16;
        final int topMargin = 8;
        final int rightMargin = 16;
        final int bottomMargin = 8;
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.main_activity_list_item,parent,false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins((int)(leftMargin*density),(int)(topMargin*density),
                (int)(rightMargin*density),(int)(bottomMargin*density));
        cardView.setLayoutParams(layoutParams);
        TextView fundNameTextView = (TextView) cardView.findViewById(R.id.fund_name);
        TextView fundNAVTextView = (TextView) cardView.findViewById(R.id.fund_nav);
        ViewHolder viewHolder = new ViewHolder(cardView,fundNameTextView,fundNAVTextView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.mFundName.setText(mCursor.getString(mCursor.getColumnIndex(FundsContentProvider.FUND_NAME)));
        holder.mFundNAV.setText(mCursor.getString(mCursor.getColumnIndex(FundsContentProvider.FUND_NAV)));
    }


    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }
}
