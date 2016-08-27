package com.neerajms99b.neeraj.mymutualfunds.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

/**
 * Created by neeraj on 27/8/16.
 */
public class MyStatsFragment extends Fragment{
    private MainActivity mCallBack;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCallBack = (MainActivity) getActivity();
//        mCallBack.hi
        setHasOptionsMenu(false);
    }
}
