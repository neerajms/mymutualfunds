package com.neerajms99b.neeraj.mymutualfunds.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by neeraj on 19/9/16.
 */
public class FundInfo {
    public String mScode;
    public String mFundName;
    public String mNav;
    public String mUnits;
    public FundInfo(){}

    public FundInfo(String scode, String fundName, String nav, String units){
        mScode = scode;
        mFundName = fundName;
        mNav = nav;
        mUnits = units;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("mScode", mScode);
        result.put("mFundName", mFundName);
        result.put("mNav", mNav);
        result.put("mUnits", mUnits);
        return result;
    }
    public String getScode(){
        return mScode;
    }
    public String getFundName(){
        return mFundName;
    }
    public String getNav(){
        return mNav;
    }
    public String getUnits(){
        return mUnits;
    }
}
