package com.neerajms99b.neeraj.mymutualfunds.models;

import com.github.mikephil.charting.data.Entry;

import java.util.Date;

/**
 * Created by neeraj on 1/10/16.
 */

public class NetWorthGraphModel extends Entry implements Comparable<NetWorthGraphModel> {
    private Date mDate;
    private String mNetworth;

    public NetWorthGraphModel(Date date, String netWorth){
        this.mDate = date;
        this.mNetworth = netWorth;
    }

    public Date getDate(){
        return mDate;
    }

    public String getNetworth(){
        return mNetworth;
    }

    @Override
    public int compareTo(NetWorthGraphModel date2) {
        return getDate().compareTo(date2.getDate());
    }
}
