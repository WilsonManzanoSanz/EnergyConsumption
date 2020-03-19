package com.example.energyconsumption.Fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.energyconsumption.Activity.GraphActivity;
import com.example.energyconsumption.R;

import java.util.Objects;



public class AddKwhPriceFragment extends DialogFragment {

    private EditText editKwhPrice;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {


        // Get the layout inflater
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        final View inflator = layoutInflater.inflate(R.layout.add_kwh_fragment, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        editKwhPrice = (EditText) inflator.findViewById(R.id.edit_kwh_price);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflator)
                // Add action buttons
                .setPositiveButton("Añadir", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        if ((!Objects.equals(editKwhPrice.getText().toString(), ""))) {

                            ((GraphActivity) Objects.requireNonNull(getActivity())).saveKWhPrice(Float.parseFloat(editKwhPrice.getText().toString()));
                        }


                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddKwhPriceFragment.this.getDialog().cancel();
                        AddKwhPriceFragment.this.getDialog().dismiss();
                        Log.i("AddKwhFragment", "cancel");
                    }
                });

        return builder.create();
    }
}
