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
//        Bundle args = new Bundle();
//        // Our object is just an integer :-P
//        args.putInt(FundsListFragment.ARG_OBJECT, i + 1);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "My Stats";
        } else if (position == 1) {
            return "My Funds";
        }
        return null;
    }
}

