package com.neerajms99b.neeraj.mymutualfunds.adapter;

import android.database.Cursor;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.neerajms99b.neeraj.mymutualfunds.R;
import com.neerajms99b.neeraj.mymutualfunds.data.FundsContentProvider;
import com.neerajms99b.neeraj.mymutualfunds.ui.MainActivityFragment;

/**
 * Created by neeraj on 9/8/16.
 */
public class FundsListAdapter extends CursorRecyclerViewAdapter<FundsListAdapter.ViewHolder>
        implements ItemTouchHelperAdapter {
    private Cursor mCursor;
    private MainActivityFragment mCallBack;

    public FundsListAdapter(Cursor cursor, MainActivityFragment mainActivityFragment) {
        super(mainActivityFragment.getContext(), cursor);
        mCursor = cursor;
        mCallBack = mainActivityFragment;
    }

    @Override
    public void onItemDismissed(int position) {
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        String[] selectionArgs = {cursor.getString(cursor.getColumnIndex(FundsContentProvider.FUND_SCODE))};
        mCallBack.getContext().getContentResolver().delete(
                FundsContentProvider.mUri, FundsContentProvider.FUND_SCODE, selectionArgs);
        notifyItemRemoved(position);
        mCallBack.restartLoader();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CardView mFundCardView;
        public TextView mFundName;
        public TextView mFundNAV;
        public TextView mUnits;
        public ImageButton mEditButton;

        public ViewHolder(CardView cardView, TextView fundNameTextView,
                          TextView fundNAVTextView, TextView units, ImageButton editButton) {
            super(cardView);
            mFundCardView = cardView;
            mFundName = fundNameTextView;
            mFundNAV = fundNAVTextView;
            mUnits = units;
            mEditButton = editButton;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        float density = parent.getResources().getDisplayMetrics().density;
        final int leftMargin = 8;
        final int topMargin = 4;
        final int rightMargin = 8;
        final int bottomMargin = 4;
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.main_activity_list_item, parent, false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins((int) (leftMargin * density), (int) (topMargin * density),
                (int) (rightMargin * density), (int) (bottomMargin * density));
        cardView.setLayoutParams(layoutParams);
        TextView fundNameTextView = (TextView) cardView.findViewById(R.id.fund_name);
        TextView fundNAVTextView = (TextView) cardView.findViewById(R.id.fund_nav);
        TextView units = (TextView) cardView.findViewById(R.id.units);
        ImageButton editButton = (ImageButton) cardView.findViewById(R.id.edit_units);
        ViewHolder viewHolder = new ViewHolder(cardView, fundNameTextView, fundNAVTextView, units, editButton);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final Cursor cursor, final int position) {
        holder.mFundName.setText(cursor.getString(cursor.getColumnIndex(FundsContentProvider.FUND_NAME)));
        holder.mFundNAV.setText(String.format("%.2f", cursor.getDouble(cursor.getColumnIndex(FundsContentProvider.FUND_NAV))));
        if (cursor.getString(cursor.getColumnIndex(FundsContentProvider.UNITS_OWNED)) != null) {
            holder.mUnits.setText("Units in hand:" + cursor.getString(cursor.getColumnIndex(FundsContentProvider.UNITS_OWNED)));
        } else {
            holder.mUnits.setText("Units in hand:" + "0");
        }
        holder.mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cursor.moveToPosition(position);
                mCallBack.editClicked(cursor.getString(cursor.getColumnIndex(FundsContentProvider.FUND_SCODE)));
            }
        });
    }
}
