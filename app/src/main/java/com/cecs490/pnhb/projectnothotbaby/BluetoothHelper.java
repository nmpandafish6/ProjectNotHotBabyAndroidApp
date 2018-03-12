package com.cecs490.pnhb.projectnothotbaby;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
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
    private boolean okay = true;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String NAME = "PNHB";

    public static void initBluetooth() {
        mblue = BluetoothAdapter.getDefaultAdapter();
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
            if(okay && mBehavior != null) {
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
                                if (wasConnected)
                                    mBehavior.none();
                                break;
                        }
                        break;
                    case BluetoothHelper.MESSAGE_WRITE:
                        mBehavior.write();
                        break;
                    case BluetoothHelper.MESSAGE_READ:
                        mBehavior.read();
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
            } catch (Exception e) {
                Log.e("BLUETOOTH_TAG", "ACCEPT ERROR" + e.getMessage());
            }
        }

        public void run() {
            try {
                mmSocket = mmServerSocket.accept();
                m_connectedThread = new ConnectedThread();
            } catch (Exception e) {
                Log.e("BLUETOOTH_TAG", "ACCEPT FAIL: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothServerSocket serverSocket;
        public ConnectThread() {
            try {
                mmSocket = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
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
            } catch (Exception connectException) {
                // Unable to connect; close the socket and get out
                Log.e("BLUETOOTH_TAG", "CONNECT FAILED: " + connectException.getMessage());
                connectException.printStackTrace();
                return;
            }

            m_connectedThread = new ConnectedThread();
        }
    }

    private class ConnectedThread extends Thread {
        private String inputString = null;
        private boolean running = true;
        public ConnectedThread() {
            isConnected = true;
            mHandler.obtainMessage(BluetoothHelper.MESSAGE_STATE_CHANGE, BluetoothHelper.STATE_CONNECTED, -1).sendToTarget();
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

        }

        public synchronized void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            // Keep listening to the InputStream until an exception occurs
            while (running) {
                try {
                    // Read from the InputStream
                    int bytes = m_inStream.read(buffer);
                    String tmp = new String(buffer, 0, bytes);
                    if(inputString != null){
                        inputString += tmp;
                    }else{
                        inputString = tmp;
                    }
                    mHandler.obtainMessage(BluetoothHelper.MESSAGE_READ, -1, -1).sendToTarget();
                } catch (Exception e) {
                    isConnected = false;
                    mHandler.obtainMessage(BluetoothHelper.MESSAGE_STATE_CHANGE, BluetoothHelper.STATE_NONE, -1).sendToTarget();
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
