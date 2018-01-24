package com.cecs490.pnhb.projectnothotbaby;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private BluetoothSocket ClientSocket;
    private BluetoothDevice Client;
    private ConnectedThread Writer;
    private BluetoothAdapter mblue;
    ArrayAdapter<String> arrayAdapter;

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            /*

            //Log.i("TAG", action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(spinnerControl != null) {
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerControl.getAdapter();
                    adapter.add(device.getName());
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(),
                            "FOUND " + device.getName() + "," + device.getAddress(), Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),
                            "FOUND NULL" + device.getName() + "," + device.getAddress(), Toast.LENGTH_SHORT).show();
                }

                try {

                } catch (IOException e) {
                    e.printStackTrace();
                }
                /*
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.radiobutton_dialog);
                final RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.radio_group);
                RadioButton rb=new RadioButton(MainActivity.this); // dynamically creating RadioButton and adding to RadioGroup.
                rb.setText(device.getName());
                rb.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER);
                rg.addView(rb);
                rg.setGravity(Gravity.CENTER_HORIZONTAL);
                dialog.show();
                */
                //Log.i("TAG", device.getName());
            /*
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //discovery starts, we can show progress dialog or perform other tasks
                //Log.i("TAG", "ST");
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismis progress dialog
                //Log.i("TAG", "ED");
            }
            */

        }
    };

    private void showRadioButtonDialog() {



    }

    Spinner spinnerControl;

    public void start(View sender)
    {

        Writer.write("HELLO".getBytes());

    }

    public void send(View sender)
    {
        ConnectThread con = new ConnectThread(Client);
        con.start();
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();
            mmDevice = blueAdapter.getRemoteDevice("A0:88:69:30:CB:1C");;
            UUID myuuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = mmDevice.createRfcommSocketToServiceRecord(myuuid);

            } catch (IOException e) {}
            ClientSocket = mmSocket = tmp;
            Toast.makeText(getApplicationContext(),
                    "Made Connect Thread", Toast.LENGTH_SHORT).show();


        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mblue.cancelDiscovery();
            Log.e("TAG","TRYING TO CONNECT");
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                Log.e("TAG","I GOT AN ERROR IN CONNECT");
                try {
                    mmSocket.close();
                } catch (IOException closeException) {}
                return;
            }
            Writer = new ConnectedThread(ClientSocket);

            // Do work to manage the connection (in a separate thread)
            //manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {}
        }
    }



    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            Log.e("TAG","I AM CONNECTED = " + mmSocket.isConnected());
            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e("TAG","IO ERROR " + e.getMessage());
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            Log.e("TAG","Made Connected Thread");
            Log.e("TAG",(mmOutStream == null) + "");
            if(mmOutStream != null) {
                Log.e("TAG","WRITING HELLO");
                try {
                    //while(true) {
                        mmOutStream.write("HELLO".getBytes());
                    //}
                }catch (Exception e){
                    Log.e("TAG",e.getMessage());
                }
            }
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    // mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                    //         .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */

        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

                //mmOutStream.
            } catch (IOException e)
            {

            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mblue = BluetoothAdapter.getDefaultAdapter();

        spinnerControl = (Spinner) findViewById(R.id.spinner_control);
        // custom dialog
        //ArrayList<String> colors = (ArrayList<String>) Arrays.asList(colorArray);
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        List<String> s = new ArrayList<String>();
        for(BluetoothDevice bt : pairedDevices)
            s.add(bt.getName());

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);


        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerControl.setAdapter(arrayAdapter);
        for(String name : s){
            arrayAdapter.add(name);
        }
        arrayAdapter.notifyDataSetChanged();

        BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice Client = blueAdapter.getRemoteDevice("A0:88:69:30:CB:1C");
        while(Client == null){
            Log.e("TAG","GETTING DEVICE");
        }
        send(null);

        //start(null);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        Button settings_apply = (Button) findViewById(R.id.settings_apply);
        settings_apply.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {

                        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        try {
                            BluetoothHelper.init(getApplicationContext(), null);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //Toast.makeText(getApplicationContext(),
                        //        "" + mReceiver, Toast.LENGTH_SHORT).show();
                        IntentFilter filter = new IntentFilter();
                        filter.addAction(BluetoothDevice.ACTION_FOUND);
                        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);


                        if (mBluetoothAdapter.isDiscovering()) {
                            mBluetoothAdapter.cancelDiscovery();
                            Toast.makeText(getApplicationContext(),
                                    "Canceling", Toast.LENGTH_SHORT).show();
                        }


                        //Log.i("TAG", "STARTED DISCO");
                        registerReceiver(mReceiver, filter);
                        mBluetoothAdapter.startDiscovery();
                        //Toast.makeText(getApplicationContext(),
                        //        "" + success, Toast.LENGTH_SHORT).show();
                        /*
                        try {
                            BluetoothHelper.init(getApplicationContext());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        */
                    }
                }
        );
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        ViewFlipper vf = (ViewFlipper) findViewById(R.id.content_viewFlipper);
        if (id == R.id.nav_home) {
            vf.setDisplayedChild(1);
        } else if (id == R.id.nav_conditions) {
            vf.setDisplayedChild(2);
        } else if (id == R.id.nav_news) {
            vf.setDisplayedChild(3);
        } else if (id == R.id.nav_stats) {
            vf.setDisplayedChild(4);
        } else if (id == R.id.nav_settings) {
            vf.setDisplayedChild(5);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }
}