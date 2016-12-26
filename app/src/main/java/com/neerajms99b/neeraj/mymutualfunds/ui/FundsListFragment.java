package com.neerajms99b.neeraj.mymutualfunds.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.neerajms99b.neeraj.mymutualfunds.R;
import com.neerajms99b.neeraj.mymutualfunds.adapter.FirebaseAdapter;
import com.neerajms99b.neeraj.mymutualfunds.adapter.SimpleItemTouchHelper;
import com.neerajms99b.neeraj.mymutualfunds.data.FundsContentProvider;
import com.neerajms99b.neeraj.mymutualfunds.models.FundInfo;

/**
 * A placeholder fragment containing a simple view.
 */
public class FundsListFragment extends Fragment {
    private StateSaveRecyclerView mRecyclerView;
    private ItemTouchHelper mItemTouchHelper;
    private ItemTouchHelper.Callback mItemTouchCallBack;
    private MainActivity mCallBack;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mMyRef;
    private FirebaseAdapter mFirebaseAdapter;
    private final String TAG = FundsListFragment.class.getSimpleName();

    public FundsListFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        //Initialize the firebase adapter
        getFirebaseData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_funds_list, container, false);

        mCallBack = (MainActivity) getActivity();
        mRecyclerView = (StateSaveRecyclerView) rootView.findViewById(R.id.funds_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mFirebaseAdapter);

        mItemTouchCallBack = new SimpleItemTouchHelper(mFirebaseAdapter);
        mItemTouchHelper = new ItemTouchHelper(mItemTouchCallBack);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_fund) {
            startSearchActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    //Initialize firebase adapter
    public void getFirebaseData() {
        mMyRef = mDatabase.getReference(mFirebaseUser.getUid()).child(getString(R.string.firebase_child_funds));
        Query query = mMyRef;
        mFirebaseAdapter = new FirebaseAdapter(FundInfo.class,
                R.layout.main_activity_list_item, FirebaseAdapter.FundHolder.class, query, this);
    }

    public void startSearchActivity() {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        startActivity(intent);
    }

    //Show the input dialog when edit button is clicked
    public void editClicked(String scode) {
        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.key_scode), scode);
        UnitsInputDialogFragment unitsInputDialogFragment = new UnitsInputDialogFragment();
        unitsInputDialogFragment.setArguments(bundle);
        unitsInputDialogFragment.setTargetFragment(FundsListFragment.this, 0);
        unitsInputDialogFragment.show(getFragmentManager(), null);
    }

    //Edit the units is firebase
    public void unitsInput(String units, String scode) {
        mMyRef = mDatabase.getReference(mFirebaseUser.getUid()).child(getString(R.string.firebase_child_funds));
        mMyRef.child(scode).child(getString(R.string.key_units_in_hand)).setValue(units);
    }

    //Called on swipe to dismiss
    public void deleteFirebaseNode(int position) {
        mMyRef = mDatabase.getReference(mFirebaseUser.getUid()).child(getString(R.string.firebase_child_funds));
        String scode = mFirebaseAdapter.getItem(position).getScode();
        mMyRef.child(scode).removeValue();
        Uri uri = Uri.parse(FundsContentProvider.mUriPortfolio.toString() + "/" + scode);
        getActivity().getContentResolver().delete(uri, null, null);
    }

    /* Restores the recyclerview position */
    public void restoreRecyclerViewPosition() {
        mRecyclerView.restorePostion();
    }
}
