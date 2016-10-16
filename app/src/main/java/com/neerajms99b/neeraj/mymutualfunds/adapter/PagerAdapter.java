package com.neerajms99b.neeraj.mymutualfunds.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.neerajms99b.neeraj.mymutualfunds.ui.FundsListFragment;
import com.neerajms99b.neeraj.mymutualfunds.ui.MyStatsFragment;

/**
 * Created by neeraj on 27/8/16.
 */

public class PagerAdapter extends FragmentPagerAdapter {
    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = null;
        if (i == 0) {
            fragment = new MyStatsFragment();
        } else if (i == 1) {
            fragment = new FundsListFragment();
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public int getItemPosition(Object object) {
        if (object instanceof UpdateFragment) {
            ((UpdateFragment) object).updateNetWorth();
        }
        return super.getItemPosition(object);
    }
}

