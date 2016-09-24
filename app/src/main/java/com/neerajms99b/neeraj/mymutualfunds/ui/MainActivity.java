package com.neerajms99b.neeraj.mymutualfunds.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.neerajms99b.neeraj.mymutualfunds.R;
import com.neerajms99b.neeraj.mymutualfunds.adapter.PagerAdapter;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {
    //    private FloatingActionButton mFab;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private Context mContext;
    private ViewPager mViewPager;
    private final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tab_my_stats)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tab_my_funds)));
        tabLayout.addOnTabSelectedListener(this);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tabLayout.getTabAt(position).select();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mContext = this;
//        mFab = (FloatingActionButton) findViewById(R.id.fab);
//        mFab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startSearchActivity();
//            }
//        });
//        hideFab();
    }

//    public void startSearchActivity() {
//        Intent intent = new Intent(mContext, SearchActivity.class);
//        startActivity(intent);
//    }

//    public void hideFab() {
//        mFab.hide();
//    }
//
//    public void showFab() {
//        mFab.show();
//    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    public void launchGraphActivity(String scode, String fundName, String fundNav, String units) {
        Intent intent = new Intent(this, GraphActivity.class);
        intent.putExtra(getString(R.string.key_scode), scode);
        intent.putExtra(getString(R.string.key_fundname),fundName);
        intent.putExtra(getString(R.string.key_fund_nav),fundNav);
        intent.putExtra(getString(R.string.key_units_in_hand),units);
        startActivity(intent);
    }
}
