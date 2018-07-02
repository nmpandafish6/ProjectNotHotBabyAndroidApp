package com.cecs490.pnhb.projectnothotbabyfinal;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.cecs490.pnhb.projectnothotbabyfinal.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Nicolas on 10/16/2017.
 */

public class BluetoothHelper {

    public interface BluetoothSocketBehavior {
        void write();
        void read();
        void connected();
        void connecting();
        void none();
    }
    public static final int MESSAGE_STATE_CHANGE = 0;
    public static final int MESSAGE_WRITE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_CONNECTING = 4;
    public static final int STATE_NONE = 5;

    private OutputStream m_outputStream;
    private InputStream m_inStream;
    public BluetoothDevice mDevice;
    private BluetoothServerSocket mmServerSocket;
    private BluetoothSocket mmSocket;
    private BluetoothSocketBehavior mBehavior;
    private ConnectedThread m_connectedThread;
    public static BluetoothAdapter mblue;
    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private boolean isConnected = false;
    private boolean wasConnected = false;
    private boolean disconnecting = false;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String NAME = "PNHB";

    public static void initBluetooth() {
        mblue = BluetoothAdapter.getDefaultAdapter();
        if(!mblue.isEnabled()) {
            DialogFragment dialog = new BluetoothDialog();
            dialog.show(ResourceMaster.m_activity.getFragmentManager(), "tag_bluetooth");
        }
    }

    public static BluetoothDevice[] getPairedDevices(){
        return mblue.getBondedDevices().toArray(new BluetoothDevice[mblue.getBondedDevices().size()]);
    }

    public static void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        ResourceMaster.m_activity.registerReceiver(mPairReceiver, intent);
    }

    private static final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state        = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState    = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    Toast.makeText(ResourceMaster.m_context.getApplicationContext(), "Paired", Toast.LENGTH_SHORT).show();
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    Toast.makeText(ResourceMaster.m_context.getApplicationContext(), "Unpaired", Toast.LENGTH_SHORT).show();
                }

            }
        }
    };

    public static ArrayAdapter<String> getDiscoveredDevices(){
        return discoveredDevices;
    }

    private static ArrayAdapter<String> discoveredDevices;
    public static ArrayAdapter<String> discoverDevices(){
        if(discoveredDevices == null) {
            discoveredDevices = new ArrayAdapter<>(
                    ResourceMaster.m_context, android.R.layout.select_dialog_singlechoice);
            if (BluetoothAdapter.getDefaultAdapter().isDiscovering()) {
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            }
            final BroadcastReceiver mReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    Log.e("BLUETOOTH", action);
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        // Discovery has found a device. Get the BluetoothDevice
                        // object and its info from the Intent.
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        String device_string = device.getName() + " [" + device.getAddress() + "]";
                        discoveredDevices.add(device_string);
                    }
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            ResourceMaster.m_activity.registerReceiver(mReceiver, filter);
            Log.e("BLUETOOTH", "REGISTERED RECEIVER");
            boolean success = BluetoothAdapter.getDefaultAdapter().startDiscovery();
            Log.e("BLUETOOTH_TAG", "STARTED DISCOVERY: " + success);
        }
        return discoveredDevices;
        //TODO: UNREGISTER RECEIVER
    }

    public BluetoothHelper(BluetoothDevice device, BluetoothSocketBehavior behavior) throws IOException {
        mBehavior = behavior;
        mDevice = device;
        acceptThread = new AcceptThread();
        acceptThread.start();
        connectThread = new ConnectThread();
        connectThread.start();
    }

    public void write(String s) throws IOException {
        if(m_connectedThread != null)
            m_connectedThread.write(s.getBytes());
        else {
            isConnected = false;
            mHandler.obtainMessage(BluetoothHelper.MESSAGE_STATE_CHANGE, BluetoothHelper.STATE_NONE, -1).sendToTarget();
            throw new IOException("NOT CONNECTED!!!");
        }
    }

    public void disconnect(){
        disconnecting = true;
        isConnected = false;
        wasConnected = false;
        m_connectedThread.running = false;
        connectThread.interrupt();
        acceptThread.interrupt();
        m_connectedThread.interrupt();
        try {
            mmServerSocket.close();
            Log.e("BLUETOOTH_TAG", "CLOSING SERVER SOCKET");
        } catch (IOException e) {

            e.printStackTrace();
        }
        mHandler.removeMessages(0);
        mHandler.removeMessages(1);
        mHandler.removeMessages(2);
        mHandler.removeMessages(3);
        mHandler.removeMessages(4);
        mHandler.removeMessages(5);
        try {
            if(m_inStream != null) m_inStream.close();
            m_inStream = null;
            Log.e("BLUETOOTH_TAG", "CLOSED INPUT STREAM");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("BLUETOOTH_TAG", e.getMessage());
        }
        try {
            if(m_outputStream != null) m_outputStream.close();
            m_outputStream = null;
            Log.e("BLUETOOTH_TAG", "CLOSED OUTPUT STREAM");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("BLUETOOTH_TAG", e.getMessage());
        }
        try {
            if(mmSocket != null) mmSocket.close();
            mmSocket = null;
            Log.e("BLUETOOTH_TAG", "CLOSED SOCKET STREAM");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("BLUETOOTH_TAG", e.getMessage());
        }
        disconnecting = false;
    }

    public synchronized String read() throws IOException {
        if(m_connectedThread != null) {
            String tmp = m_connectedThread.inputString;
            m_connectedThread.inputString = null;
            return tmp;
        }else{
            return null;
        }
    }


    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if(mBehavior != null) {
                switch (msg.what) {
                    case BluetoothHelper.MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case BluetoothHelper.STATE_CONNECTED:
                                mBehavior.connected();
                                break;
                            case BluetoothHelper.STATE_CONNECTING:
                                mBehavior.connecting();
                                break;
                            case BluetoothHelper.STATE_NONE:
                                if (wasConnected & !disconnecting)
                                    mBehavior.none();
                                break;
                        }
                        break;
                    case BluetoothHelper.MESSAGE_WRITE:
                        mBehavior.write();
                        break;
                    case BluetoothHelper.MESSAGE_READ:
                        try {
                            mBehavior.read();
                        }catch(Exception e){
                            Log.e("ERROR",e.getMessage());
                        }
                        break;
                }
            }
        }
    };

    public boolean getIsConnected(){
        return isConnected;
    }

    private class AcceptThread extends Thread {
        public AcceptThread() {
            try{
                mmServerSocket = mblue.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
                Log.e("BLUETOOTH_TAG", "MM SERVER SOCKET CREATED");
            } catch (Exception e) {
                Log.e("BLUETOOTH_TAG", "ACCEPT ERROR" + e.getMessage());
            }
        }

        public void run() {
            try {
                mmSocket = mmServerSocket.accept();
                if(Thread.currentThread().isInterrupted())
                    throw new InterruptedException();
                if(!Thread.currentThread().isInterrupted())
                    m_connectedThread = new ConnectedThread();
            } catch (InterruptedException e) {
                return;
            } catch (IOException e) {
                Log.e("BLUETOOTH_TAG", "ACCEPT FAIL: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }
    }

    private class ConnectThread extends Thread {
        public ConnectThread() {
            try {
                mmSocket = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
                Log.e("BLUETOOTH_TAG", "MM SOCKET CREATED");
            } catch (Exception e) {
                Log.e("BLUETOOTH_TAG", "listen() failed", e);
            }
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mblue.cancelDiscovery();
            mHandler.obtainMessage(BluetoothHelper.MESSAGE_STATE_CHANGE, BluetoothHelper.STATE_CONNECTING, -1).sendToTarget();
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
                if(Thread.currentThread().isInterrupted())
                    throw new InterruptedException();
                if(!Thread.currentThread().isInterrupted())
                    m_connectedThread = new ConnectedThread();
            } catch (InterruptedException e) {
                return;
            } catch (Exception connectException) {
                // Unable to connect; close the socket and get out
                Log.e("BLUETOOTH_TAG", "CONNECT FAILED: " + connectException.getMessage());
                connectException.printStackTrace();
                return;
            }
            if(m_connectedThread != null){
                m_connectedThread.running = false;
                Log.e("JSON_TAG", "KILLED RUNNING THREAD");
            }

            m_connectedThread = new ConnectedThread();
        }
    }

    private class ConnectedThread extends Thread {
        private String inputString = null;
        private boolean running = true;
        public ConnectedThread() {
            isConnected = true;

            try {
                if(mmSocket != null) {
                    m_inStream = mmSocket.getInputStream();
                }
                if(mmSocket != null) {
                    m_outputStream = mmSocket.getOutputStream();
                }
            } catch (Exception e) {
                Log.e("BLUETOOTH_TAG","IO ERROR " + e.getMessage());
            }
            this.start();
            wasConnected = true;
            mHandler.obtainMessage(BluetoothHelper.MESSAGE_STATE_CHANGE, BluetoothHelper.STATE_CONNECTED, -1).sendToTarget();
        }

        public synchronized void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            // Keep listening to the InputStream until an exception occurs
            while (running) {
                try {
                    // Read from the InputStream
                    int bytes = m_inStream.read(buffer);
                    if(bytes <= 0) continue;
                    String tmp = new String(buffer, 0, bytes);
                    Log.e("JSON_TAG", "GOT : " + tmp);
                    if(tmp == null) continue;
                    if(inputString != null){
                        inputString += tmp;
                    }else{
                        inputString = tmp;
                    }
                    Log.e("JSON_TAG", "INPUT STRING : " + inputString);
                    Thread.sleep(10);
                    mHandler.obtainMessage(BluetoothHelper.MESSAGE_READ, -1, -1).sendToTarget();
                } catch (Exception e) {
                    isConnected = false;
                    if(!disconnecting) {
                        mHandler.obtainMessage(BluetoothHelper.MESSAGE_STATE_CHANGE, BluetoothHelper.STATE_NONE, -1).sendToTarget();
                        acceptThread = new AcceptThread();
                        acceptThread.start();
                        connectThread = new ConnectThread();
                        connectThread.start();
                    }
                    running = false;
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            if (m_outputStream != null) {
                mHandler.obtainMessage(BluetoothHelper.MESSAGE_WRITE, -1, -1).sendToTarget();
                try {
                    m_outputStream.write(bytes);
                } catch (Exception e) {
                }
            }
        }
    }

}
