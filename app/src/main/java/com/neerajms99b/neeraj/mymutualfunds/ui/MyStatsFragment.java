package com.neerajms99b.neeraj.mymutualfunds.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.neerajms99b.neeraj.mymutualfunds.R;
import com.neerajms99b.neeraj.mymutualfunds.adapter.UpdateFragment;
import com.neerajms99b.neeraj.mymutualfunds.models.FundInfo;

import java.util.ArrayList;

/**
 * Created by neeraj on 27/8/16.
 */
public class MyStatsFragment extends Fragment implements UpdateFragment {
    private MainActivity mCallBack;
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private float mNetWorth;
    private String TAG = MyStatsFragment.class.getSimpleName();
    private ArrayList<FundInfo> mFundsArrayList;
    private TextView mNetWorthAmountTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCallBack = (MainActivity) getActivity();
        mFundsArrayList = new ArrayList<FundInfo>();
        mDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
//        if (savedInstanceState != null && savedInstanceState.containsKey(getString(R.string.net_worth))) {
//            mNetWorth = savedInstanceState.getFloat(getString(R.string.net_worth));
//        } else {
//            mNetWorth = 0.0f;
//        }
//        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
//                getString(R.string.shared_prefs_file_key), Context.MODE_PRIVATE);
//        mNetWorth = sharedPreferences.getFloat(getString(R.string.key_net_worth), 0.0f);
//        setNetWorth();
//        Log.e(TAG, String.valueOf(mNetWorth));
        if (savedInstanceState != null && savedInstanceState.containsKey(getString(R.string.funds_array_list))){
            mFundsArrayList = savedInstanceState.getParcelableArrayList(getString(R.string.funds_array_list));
            calculateNetWorth();
        }
        readFirebaseFundsList();
        setHasOptionsMenu(false);
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        Log.e(TAG,"Started");
//    }
//
    @Override
    public void onResume() {
        super.onResume();
//        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
//                getString(R.string.shared_prefs_file_key), Context.MODE_PRIVATE);
//        mNetWorth = sharedPreferences.getFloat(getString(R.string.key_net_worth), 0.0f);
//        Log.e(TAG,"Resumed");
//        setNetWorth();
    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
////        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
////                getString(R.string.shared_prefs_file_key), Context.MODE_PRIVATE);
////        mNetWorth = sharedPreferences.getFloat(getString(R.string.key_net_worth), 0.0f);
////
////        setNetWorth();
//        Log.e(TAG,"Attached");
//
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
//                getString(R.string.shared_prefs_file_key), Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putFloat(getString(R.string.key_net_worth), mNetWorth);
//        editor.commit();
//        Log.e(TAG,"Detached");
//    }

//    @Override
//    public void onPause() {
//        super.onPause();
//        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
//                getString(R.string.shared_prefs_file_key), Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putFloat(getString(R.string.key_net_worth), mNetWorth);
//        editor.commit();
//        Log.e(TAG,"Paused");
//    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stats, container, false);
        mNetWorthAmountTextView = (TextView) rootView.findViewById(R.id.net_worth_amount);
        Log.e(TAG,"oncreateview");
        setNetWorth();
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(getString(R.string.funds_array_list),mFundsArrayList);
    }

    public void readFirebaseFundsList() {
        DatabaseReference fundsListReference =
                mDatabase.getReference(mFirebaseUser.getUid()).child(getString(R.string.firebase_child_funds));
        fundsListReference.keepSynced(true);
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                FundInfo fundInfo = dataSnapshot.getValue(FundInfo.class);
                mFundsArrayList.add(fundInfo);
//                calculateNetWorth();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                FundInfo fundInfo = dataSnapshot.getValue(FundInfo.class);
                reflectChange(fundInfo);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        fundsListReference.addChildEventListener(childEventListener);
    }

    public void reflectChange(FundInfo fundInfoNew) {
        for (int index = 0; index < mFundsArrayList.size(); index++) {
            if (mFundsArrayList.get(index).getScode().equals(fundInfoNew.getScode())) {
//                FundInfo fundInfoOld = mFundsArrayList.get(index);
//                float previousNav = Float.parseFloat(fundInfoOld.getNav());
//                float previousUnits = Float.parseFloat(fundInfoOld.getUnits());
//                mNetWorth = mNetWorth - (previousNav * previousUnits);
                mFundsArrayList.remove(index);
                mFundsArrayList.add(index, fundInfoNew);
//                mNetWorth = mNetWorth + (Float.parseFloat(fundInfoNew.getNav()) *
//                        Float.parseFloat(fundInfoNew.getUnits()));
                break;
            }
        }
//        setNetWorth();
//        storeNetWorth();
//        Log.e(TAG, String.valueOf(mNetWorth));
    }

    public void calculateNetWorth() {
        mNetWorth = 0;
        for (int index = 0; index < mFundsArrayList.size(); index++) {
            FundInfo fundInfo = mFundsArrayList.get(index);
            mNetWorth = mNetWorth + (Float.parseFloat(fundInfo.getNav())
                    * Float.parseFloat(fundInfo.getUnits()));
        }

//        setNetWorth();
//        storeNetWorth();
        Log.e(TAG, String.valueOf(mNetWorth));
    }

    public void setNetWorth() {
        String netWorthStr = getString(R.string.rupee_symbol) + String.format("%.2f", mNetWorth);
        mNetWorthAmountTextView.setText(netWorthStr);
    }

    public void storeNetWorth() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
                getString(R.string.shared_prefs_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(getString(R.string.key_net_worth), mNetWorth);
        editor.commit();
    }

    @Override
    public void updateNetWorth() {
        calculateNetWorth();
        setNetWorth();
    }
}
