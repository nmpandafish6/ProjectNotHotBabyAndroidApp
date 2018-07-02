package com.cecs490.pnhb.projectnothotbabyfinal;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Nicolas on 4/2/2018.
 */

public class BluetoothNewDeviceDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(ResourceMaster.m_activity);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setTitle("Select your Bluetooth Device");

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                DialogFragment newDialog = new BluetoothDeviceDialog();
                newDialog.show(ResourceMaster.m_activity.getFragmentManager(), "bluetooth_init");
            }
        });
        builder.setAdapter(BluetoothHelper.discoverDevices(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // The 'which' argument contains the index position
                // of the selected item
                BluetoothHelper.mblue.cancelDiscovery();
                String device = BluetoothHelper.getDiscoveredDevices().getItem(which);
                device = device.substring(device.lastIndexOf('[')+1,device.lastIndexOf(']'));
                ResourceMaster.preferenceEditor.putString(Constants.BLUETOOTH_MAC_KEY,device);
                ResourceMaster.preferenceEditor.commit();
                Log.e("BLUETOOTH",device);
                BluetoothHelper.pairDevice(BluetoothHelper.mblue.getRemoteDevice(device));
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
