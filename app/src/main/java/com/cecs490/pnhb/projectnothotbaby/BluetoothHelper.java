package com.cecs490.pnhb.projectnothotbaby;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import com.cecs490.pnhb.projectnothotbaby.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Nicolas on 10/16/2017.
 */

public class BluetoothHelper {

    private static OutputStream outputStream;
    private static InputStream inStream;

    private static final UUID MY_UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

    private static BluetoothSocket mmSocket;

    public static void init(Context applicationContext, BluetoothDevice device) throws IOException {
        BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();
        device = blueAdapter.getRemoteDevice("A0:88:69:30:CB:1C");
        if (blueAdapter != null) {
            if (blueAdapter.isEnabled()) {
                ParcelUuid[] uuids = device.getUuids();
                Toast.makeText(applicationContext, "PAIRING IWTH " + device.getName() + uuids.length + uuids[0], Toast.LENGTH_SHORT).show();
                //pairDevice(device);

                Toast.makeText(applicationContext, uuids.toString(), Toast.LENGTH_SHORT).show();
                BluetoothSocket socket = null;
                try {
                    socket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                } catch (Exception e){
                    Toast.makeText(applicationContext, "COULD NOT MAKE SOCKET", Toast.LENGTH_SHORT).show();
                }
                mmSocket = socket;
                mmSocket.connect();
                Toast.makeText(applicationContext, "IS CONNECTED " + mmSocket.isConnected(), Toast.LENGTH_SHORT).show();
                outputStream = mmSocket.getOutputStream();
                inStream = mmSocket.getInputStream();
                outputStream.write("Hello".getBytes());

                Log.e("error", "No appropriate paired devices.");
            } else {
                Log.e("error", "Bluetooth is disabled.");
            }
        }
    }

    private static void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void write(String s) throws IOException {
        outputStream.write(s.getBytes());
    }

    public static void run() {
        final int BUFFER_SIZE = 1024;
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytes = 0;
        int b = BUFFER_SIZE;

        while (true) {
            try {
                bytes = inStream.read(buffer, bytes, BUFFER_SIZE - bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
