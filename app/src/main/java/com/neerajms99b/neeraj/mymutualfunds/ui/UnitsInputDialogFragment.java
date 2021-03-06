package com.neerajms99b.neeraj.mymutualfunds.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
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
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.units_input_alert_dialog, null);
        final EditText editText = (EditText) view.findViewById(R.id.units_edittext);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        final FundsListFragment callBack = (FundsListFragment) getTargetFragment();
        Bundle bundle = getArguments();
        final String scode = bundle.getString(getString(R.string.key_scode));
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.units_title)
                .setView(view)
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String units = editText.getText().toString();
                        if (!units.equals("")) {
                            callBack.unitsInput(units, scode);
                        }
                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }
}
