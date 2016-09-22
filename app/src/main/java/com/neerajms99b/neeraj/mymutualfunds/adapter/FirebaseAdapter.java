package com.neerajms99b.neeraj.mymutualfunds.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;
import com.neerajms99b.neeraj.mymutualfunds.R;
import com.neerajms99b.neeraj.mymutualfunds.data.FundInfo;
import com.neerajms99b.neeraj.mymutualfunds.ui.FundsListFragment;

/**
 * Created by neeraj on 20/9/16.
 */

public class FirebaseAdapter extends FirebaseRecyclerAdapter<FundInfo, FirebaseAdapter.FundHolder>
        implements ItemTouchHelperAdapter {
    private final int FIRST_CARD = 1;
    private FundsListFragment mCallBack;
    private int mLastPosition = -1;

    public FirebaseAdapter(Class modelClass, int modelLayout, Class viewHolderClass, Query ref, FundsListFragment fundsListFragment) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        mCallBack = fundsListFragment;
    }

    @Override
    public void onItemDismissed(int position) {
        mCallBack.deleteFirebaseNode(position);
    }


    public class FundHolder extends RecyclerView.ViewHolder {
        public CardView mFundCardView;
        public TextView mFundName;
        public TextView mFundNAV;
        public TextView mUnits;
        public TextView mChange;
        public ImageView mArrow;
        public ImageButton mEditButton;

        public FundHolder(CardView cardView, TextView fundNameTextView,
                          TextView fundNAVTextView, TextView units, TextView change,
                          ImageButton editButton, ImageView arrow) {
            super(cardView);
            mFundCardView = cardView;
            mFundName = fundNameTextView;
            mFundNAV = fundNAVTextView;
            mUnits = units;
            mEditButton = editButton;
            mChange = change;
            mArrow = arrow;
        }

        public void setFundName(String fundName) {
            mFundName.setText(fundName);
        }

        public void setFundNAV(String fundNav,boolean isNegative) {
            if (isNegative){
                mFundNAV.setTextColor(mCallBack.getResources().getColor(R.color.colorRed));
            }else {
                mFundNAV.setTextColor(mCallBack.getResources().getColor(R.color.colorGreen));
            }
            String nav = String.format("%.2f",Double.valueOf(fundNav));
            mFundNAV.setText(nav);
        }

        public void setUnits(String units) {
            mUnits.setText(units);
        }

        public void setChange(double changeValue,double changePercent,boolean isNegative){
            if (isNegative){
                mChange.setTextColor(mCallBack.getResources().getColor(R.color.colorRed));
                mArrow.setImageResource(R.drawable.ic_action_down);
                mArrow.setVisibility(View.VISIBLE);
            }else {
                mChange.setTextColor(mCallBack.getResources().getColor(R.color.colorGreen));
                mArrow.setImageResource(R.drawable.ic_action_up);
                mArrow.setVisibility(View.VISIBLE);
            }
            String change = String.valueOf(Math.abs(changeValue))+
                    "("+String.valueOf(Math.abs(changePercent))+"%"+")";
            mChange.setText(change);
        }

        public ImageButton getEditButton() {
            return mEditButton;
        }

        public CardView getFundCardView(){
            return mFundCardView;
        }
    }

    @Override
    protected void populateViewHolder(FundHolder viewHolder, final FundInfo model, int position) {
        double changeValue = Double.valueOf(model.getChangeValue());
        double changePercent = Double.valueOf(model.getChangePercent().substring(0,model.getChangePercent().length()-1));
        boolean isNegative = false;
        if (changeValue < 0.00d){
            isNegative = true;
        }
        viewHolder.setFundName(model.getFundName());
        viewHolder.setFundNAV(model.getNav(),isNegative);
        viewHolder.setUnits(model.getUnits());
        viewHolder.setChange(changeValue,changePercent,isNegative);
        viewHolder.getEditButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallBack.editClicked(model.getScode());
            }
        });
        viewHolder.getFundCardView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallBack.showGraph(model.getScode());
            }
        });
    }

    @Override
    public FundHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        float density = parent.getResources().getDisplayMetrics().density;
        int leftMargin = 8;
        int topMargin = 0;
        int rightMargin = 8;
        int bottomMargin = 8;
        if (viewType == FIRST_CARD) {
            topMargin = 8;
        }
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
        TextView change = (TextView) cardView.findViewById(R.id.nav_change);
        ImageView arrow = (ImageView) cardView.findViewById(R.id.arrow);
        arrow.setVisibility(View.INVISIBLE);
        ImageButton editButton = (ImageButton) cardView.findViewById(R.id.edit_units);
        FundHolder viewHolder = new FundHolder(cardView, fundNameTextView, fundNAVTextView,
                units, change, editButton,arrow);
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return FIRST_CARD;
        }
        return 0;
    }
}
