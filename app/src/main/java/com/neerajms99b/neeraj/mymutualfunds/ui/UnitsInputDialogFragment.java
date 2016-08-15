package com.neerajms99b.neeraj.mymutualfunds.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.neerajms99b.neeraj.mymutualfunds.R;

/**
 * Created by neeraj on 14/8/16.
 */
public class UnitsInputDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        return super.onCreateDialog(savedInstanceState);
        // Use the Builder class for convenient dialog construction
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.units_input_alert_dialog,null);
        final EditText editText =(EditText) view.findViewById(R.id.units_edittext);
        final MainActivityFragment callBack = (MainActivityFragment) getTargetFragment();
        Bundle bundle = getArguments();
        final String scode = bundle.getString(getString(R.string.key_scode));
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.units_title)
                .setView(view)
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String units = editText.getText().toString();
                        callBack.unitsInput(units,scode);
                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
