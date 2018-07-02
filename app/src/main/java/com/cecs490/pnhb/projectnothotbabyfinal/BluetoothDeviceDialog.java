package com.cecs490.pnhb.projectnothotbabyfinal;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

/**
 * Created by Nicolas on 4/2/2018.
 */

public class BluetoothDeviceDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(ResourceMaster.m_activity);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setTitle("Select your Bluetooth Device");
        ArrayList<CharSequence> arraylist = new ArrayList<>();
        for(BluetoothDevice device: BluetoothHelper.getPairedDevices()){
            arraylist.add(device.getName() + " [" + device.getAddress() + "]");
        }
        CharSequence[] array = arraylist.toArray(new CharSequence[arraylist.size()]);

        builder.setNeutralButton("Help", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id){
            BluetoothHelper.discoverDevices();
            DialogFragment newDialog = new BluetoothNewDeviceDialog();
                newDialog.show(ResourceMaster.m_activity.getFragmentManager(), "bluetooth_discovery");
            }
        });
        /*
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ResourceMaster.m_activity.finish();
            }
        });
        */

        builder.setItems(array, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // The 'which' argument contains the index position
                // of the selected item
                String device = BluetoothHelper.getPairedDevices()[which].getAddress();
                ResourceMaster.preferenceEditor.putString(Constants.BLUETOOTH_MAC_KEY,device);
                ResourceMaster.preferenceEditor.commit();
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
