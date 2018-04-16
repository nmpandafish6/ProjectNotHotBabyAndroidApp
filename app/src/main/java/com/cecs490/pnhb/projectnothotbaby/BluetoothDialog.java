package com.cecs490.pnhb.projectnothotbaby;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Nicolas on 4/2/2018.
 */

public class BluetoothDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(ResourceMaster.m_activity);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage("Bluetooth is required to use this app")
                .setTitle("Enable Bluetooth");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (!BluetoothHelper.mblue.isEnabled()) {
                    BluetoothHelper.mblue.enable();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ResourceMaster.m_activity.finish();
            }
        });

        // 3. Get the AlertDialog from create()
        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        ResourceMaster.m_activity.finish();
    }
}
