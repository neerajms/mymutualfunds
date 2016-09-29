package com.neerajms99b.neeraj.mymutualfunds.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by neeraj on 10/8/16.
 */
public class BasicFundInfoParcelable implements Parcelable {
    public String mScode;
    public String mFundName;

    public BasicFundInfoParcelable(String scode,
                                   String fundName) {
        mScode = scode;
        mFundName = fundName;
    }

    private BasicFundInfoParcelable(Parcel in) {
        mScode = in.readString();
        mFundName = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel destination, int i) {
        destination.writeString(mScode);
        destination.writeString(mFundName);
    }

    public static final Parcelable.Creator<BasicFundInfoParcelable> CREATOR
            = new Parcelable.Creator<BasicFundInfoParcelable>() {

        @Override
        public BasicFundInfoParcelable createFromParcel(Parcel in) {
            return new BasicFundInfoParcelable(in);
        }

        @Override
        public BasicFundInfoParcelable[] newArray(int size) {
            return new BasicFundInfoParcelable[size];
        }
    };
}
