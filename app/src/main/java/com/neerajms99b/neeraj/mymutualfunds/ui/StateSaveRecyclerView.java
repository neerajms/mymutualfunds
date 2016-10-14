package com.neerajms99b.neeraj.mymutualfunds.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by neeraj on 14/10/16.
 */

public class StateSaveRecyclerView extends RecyclerView {
    private static final String SAVED_LAYOUT_MANAGER = "saved_layout_manager";
    private static final String SAVED_SUPER_STATE = "saved_super_state";
    private Parcelable mLayoutManagerSavedState;

    public StateSaveRecyclerView(Context context) {
        super(context);
    }

    public StateSaveRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public StateSaveRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(SAVED_SUPER_STATE, super.onSaveInstanceState());
        bundle.putParcelable(SAVED_LAYOUT_MANAGER, getLayoutManager().onSaveInstanceState());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mLayoutManagerSavedState = bundle.getParcelable(SAVED_LAYOUT_MANAGER);
            state = bundle.getParcelable(SAVED_SUPER_STATE);
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
    }

    public void restorePostion() {
        if (mLayoutManagerSavedState != null) {
            getLayoutManager().onRestoreInstanceState(mLayoutManagerSavedState);
            mLayoutManagerSavedState = null;
        }
    }
}
