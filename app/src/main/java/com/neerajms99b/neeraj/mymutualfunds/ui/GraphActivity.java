package com.neerajms99b.neeraj.mymutualfunds.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.neerajms99b.neeraj.mymutualfunds.R;

public class GraphActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        Intent intent = getIntent();
        String scode = intent.getExtras().getString(getString(R.string.key_scode));
        GraphFragment fragment = new GraphFragment();
        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.key_scode),scode);
        fragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.graph_fragment,fragment);
//        transaction.addToBackStack(null);
        transaction.commit();
    }
}
