package com.neerajms99b.neeraj.mymutualfunds.ui;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by neeraj on 21/9/16.
 */

public class MyFirebaseApp extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
