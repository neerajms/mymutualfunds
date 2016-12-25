package com.neerajms99b.neeraj.mymutualfunds.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.neerajms99b.neeraj.mymutualfunds.R;

/**
 * Created by neeraj on 25/12/16.
 */

public class ProgressDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog.Builder dialogBuilder = new ProgressDialog.Builder(getContext());
        dialogBuilder.setMessage(getString(R.string.message_downloading));
        return dialogBuilder.create();
    }
}
