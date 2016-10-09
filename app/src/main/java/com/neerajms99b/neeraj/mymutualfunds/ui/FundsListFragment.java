package com.neerajms99b.neeraj.mymutualfunds.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.neerajms99b.neeraj.mymutualfunds.service.FundsIntentService;

/**
 * A placeholder fragment containing a simple view.
 */
public class FundsListFragment extends Fragment{
    private RecyclerView mRecyclerView;
    public int CURSOR_LOADER_ID = 0;
    private TextView mNetWorthAmount;
    private String mNetWorth;
    private ItemTouchHelper mItemTouchHelper;
    private ItemTouchHelper.Callback mItemTouchCallBack;
    private MainActivity mCallBack;
    public final String ARG_OBJECT = "funds_list";
    private FloatingActionButton mFab;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private Query mQuery;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mMyRef;
    private FirebaseAdapter mFirebaseAdapter;
    private final String TAG = FundsListFragment.class.getSimpleName();
    private Menu mMenu;

    public FundsListFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        getFirebaseData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mCallBack = (MainActivity) getActivity();
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.funds_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mFirebaseAdapter);

        mItemTouchCallBack = new SimpleItemTouchHelper(mFirebaseAdapter);
        mItemTouchHelper = new ItemTouchHelper(mItemTouchCallBack);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        mFab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSearchActivity();
            }
        });
        mFab.hide();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_fund) {
            startSearchActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    public void editClicked(String scode) {
        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.key_scode), scode);
        UnitsInputDialogFragment unitsInputDialogFragment = new UnitsInputDialogFragment();
        unitsInputDialogFragment.setArguments(bundle);
        unitsInputDialogFragment.setTargetFragment(FundsListFragment.this, 0);
        unitsInputDialogFragment.show(getFragmentManager(), null);
    }

    public void unitsInput(String units, String scode) {
        mMyRef = mDatabase.getReference(mFirebaseUser.getUid()).child(getString(R.string.firebase_child_funds));
        mMyRef.child(scode).child(getString(R.string.key_units_in_hand)).setValue(units);
    }

    public void startSearchActivity() {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        startActivity(intent);
    }

    public void deleteFirebaseNode(int position) {
        mMyRef = mDatabase.getReference(mFirebaseUser.getUid()).child(getString(R.string.firebase_child_funds));
        String scode = mFirebaseAdapter.getItem(position).getScode();
        mMyRef.child(scode).removeValue();
        Uri uri = Uri.parse(FundsContentProvider.mUriHistorical.toString() + "/" + scode);
        getActivity().getContentResolver().delete(uri, null, null);
    }

    public void getFirebaseData() {
        mMyRef = mDatabase.getReference(mFirebaseUser.getUid()).child(getString(R.string.firebase_child_funds));
        Query query = mMyRef;
        mFirebaseAdapter = new FirebaseAdapter(FundInfo.class,
                R.layout.main_activity_list_item, FirebaseAdapter.FundHolder.class, query, this);
    }

    public void showGraph(String scode, String fundName, String fundNav, String units) {
        Log.d(TAG, scode);
        mCallBack.launchGraphActivity(scode, fundName, fundNav, units);
    }
    public void addScodeToDatabase(String scode){
        Intent intent = new Intent(getContext(), FundsIntentService.class);
        intent.putExtra(getString(R.string.key_tag),getString(R.string.tag_insert_scodes));
        intent.putExtra(getString(R.string.key_scode),scode);
        getContext().startService(intent);
    }
}
