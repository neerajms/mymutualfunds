package com.neerajms99b.neeraj.mymutualfunds.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by neeraj on 19/9/16.
 */
public class FundInfo implements Parcelable{
    public String mScode;
    public String mFundName;
    public String mNav;
    public String mUnits;
    public String mChangeValue;
    public String mChangePercent;
    public FundInfo(){}

    public FundInfo(String scode,
                    String fundName,
                    String nav,
                    String units,
                    String changeValue,
                    String changePercent){
        mScode = scode;
        mFundName = fundName;
        mNav = nav;
        mUnits = units;
        mChangeValue = changeValue;
        mChangePercent = changePercent;
    }

    private FundInfo(Parcel in){
        mScode = in.readString();
        mFundName = in.readString();
        mNav = in.readString();
        mUnits = in.readString();
        mChangeValue = in.readString();
        mChangePercent = in.readString();
    }
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("mScode", mScode);
        result.put("mFundName", mFundName);
        result.put("mNav", mNav);
        result.put("mUnits", mUnits);
        result.put("mChangeValue",mChangeValue);
        result.put("mChangePercent",mChangePercent);
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
    public String getChangeValue(){
        return mChangeValue;
    }
    public String getChangePercent(){
        return mChangePercent;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel destination, int i) {
        destination.writeString(mScode);
        destination.writeString(mFundName);
        destination.writeString(mNav);
        destination.writeString(mUnits);
        destination.writeString(mChangeValue);
        destination.writeString(mChangePercent);
    }

    public static final Parcelable.Creator<FundInfo> CREATOR
            = new Parcelable.Creator<FundInfo>() {

        @Override
        public FundInfo createFromParcel(Parcel in) {
            return new FundInfo(in);
        }

        @Override
        public FundInfo[] newArray(int size) {
            return new FundInfo[size];
        }
    };
}
