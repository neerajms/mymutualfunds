package com.neerajms99b.neeraj.mymutualfunds.ui;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.os.ResultReceiver;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.PointTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.neerajms99b.neeraj.mymutualfunds.R;
import com.neerajms99b.neeraj.mymutualfunds.adapter.PagerAdapter;
import com.neerajms99b.neeraj.mymutualfunds.service.Alarm;
import com.neerajms99b.neeraj.mymutualfunds.service.FetchFundsTask;
import com.neerajms99b.neeraj.mymutualfunds.service.FundsIntentService;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private Context mContext;
    private ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private final String TAG = MainActivity.class.getSimpleName();
    private Alarm mAlarmManager;
    private SharedPreferences mSharedPreferences;
    private TabLayout mTabLayout;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //to dismiss notification once the app is opened
        dismissNotification();

        mSharedPreferences = getSharedPreferences(
                getString(R.string.key_shared_prefs_funds_list), MODE_PRIVATE);
        mAlarmManager = new Alarm();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            //Set the update alarm on first run
//            mAlarmManager.setAlarm(this);
            return;
        }
        mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.tab_my_stats)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.tab_my_funds)));
        mTabLayout.addOnTabSelectedListener(this);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mTabLayout.getTabAt(position).select();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mContext = this;
        SharedPreferences sharedPreferences =
                getSharedPreferences(getString(R.string.key_shared_prefs_main_activity), MODE_PRIVATE);
        if (sharedPreferences.getBoolean(getString(R.string.key_is_firstrun), true)) {
            startProgressDialog();
            Intent intent = new Intent(mContext, FundsIntentService.class);
            intent.putExtra(getString(R.string.key_tag), getString(R.string.tag_download_data));
            intent.putExtra(getString(R.string.key_download_progress_receiver),
                    new DownloadProgressReceiver(new Handler()));
            startService(intent);
            sharedPreferences.edit().putBoolean(getString(R.string.key_is_firstrun), false).commit();
        }
    }

    private class DownloadProgressReceiver extends ResultReceiver {
        public DownloadProgressReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == FetchFundsTask.DOWNLOAD_PROGRESS) {
                int progress = resultData.getInt(getString(R.string.key_download_progress));
                Log.d(TAG, "Progress = " + progress);
                mProgressDialog.setProgress(progress);
                if (progress == 100) {
                    mProgressDialog.dismiss();
                }
            }
        }
    }

    public void startProgressDialog() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage(getString(R.string.message_downloading));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgress(0);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        mViewPager.setCurrentItem(tab.getPosition());
        if (tab.getPosition() == 1) {
            if (mSharedPreferences.getBoolean(getString(R.string.key_is_firstrun), true)) {
                Display display = getWindowManager().getDefaultDisplay();
                Point dispSize = new Point();
                display.getSize(dispSize);
                int maxX = dispSize.x;
                new ShowcaseView.Builder(this)
                        .withMaterialShowcase()
                        .setStyle(R.style.CustomShowcaseTheme)
                        .setTarget(new PointTarget(maxX - 20, 60))
                        .setContentTitle(getString(R.string.add_more_funds_showcase_title))
                        .setContentText(getString(R.string.add_more_funds_showcase_content))
                        .hideOnTouchOutside()
                        .build();
                mSharedPreferences.edit().putBoolean(getString(R.string.key_is_firstrun), false).apply();
            }
        }
        mPagerAdapter.notifyDataSetChanged();
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
        intent.putExtra(getString(R.string.key_fundname), fundName);
        intent.putExtra(getString(R.string.key_fund_nav), fundNav);
        intent.putExtra(getString(R.string.key_units_in_hand), units);
        startActivity(intent);
    }

    public void dismissNotification() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Integer.parseInt(getString(R.string.notification_id)));
    }
}
