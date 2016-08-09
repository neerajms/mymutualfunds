package com.neerajms99b.neeraj.mymutualfunds.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;

import com.neerajms99b.neeraj.mymutualfunds.R;
import com.neerajms99b.neeraj.mymutualfunds.service.FundsIntentService;

public class SearchActivity extends AppCompatActivity {
    private final String TAG = SearchActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Search Funds");
        final Context context = this;
        final SearchView search = (SearchView) findViewById(R.id.search);
        search.setIconified(false);
        search.setLayoutParams(new Toolbar.LayoutParams(Gravity.RIGHT));
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intentService = new Intent(context, FundsIntentService.class);
                intentService.putExtra("tag", "force");
                intentService.putExtra("fund", query);
                startService(intentService);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
