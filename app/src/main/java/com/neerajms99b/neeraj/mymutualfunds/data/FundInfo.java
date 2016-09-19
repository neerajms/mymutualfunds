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
        result.put("scode", mScode);
        result.put("fundName", mFundName);
        result.put("nav", mNav);
        result.put("units", mUnits);
        return result;
    }

}
