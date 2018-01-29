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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private int debug_state = 0;
    private static BluetoothHelper myDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setSupportActionBar(toolbar);
        BluetoothHelper.initBluetooth();
        // CPU MAC "A0:88:69:30:CB:1C"
        final String bluetoothMAC = "20:16:12:12:80:68";
        BluetoothDevice blueDevice = BluetoothHelper.mblue.getRemoteDevice(bluetoothMAC);
        BluetoothHelper.BluetoothSocketBehavior behavior = new BluetoothHelper.BluetoothSocketBehavior() {
            String tempInputData = "";
            @Override
            public void write() {
                Toast.makeText(getApplicationContext(),
                        "Writing", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void read() {

                try {
                    String tmp = myDevice.read();
                    if(tmp == null) return;
                    tempInputData += tmp;
                    String jsonString = "";
                    if(tempInputData.contains("}") && tempInputData.contains("{")){
                        jsonString = tempInputData.substring(tempInputData.indexOf("{"),
                                tempInputData.indexOf("}") + 1);
                    }
                    JSONObject json = new JSONObject(jsonString);
                    double temperature = json.getDouble("TEMP");
                    double humidity = json.getDouble("HUMIDITY");
                    Toast.makeText(getApplicationContext(),
                            "Temperature: " + temperature + "\nHumidity: " + humidity, Toast.LENGTH_SHORT).show();
                    TextView conditions_currentTemperature = (TextView) findViewById(R.id.conditions_currentTemperature);
                    TextView conditions_currentHumidity = (TextView) findViewById(R.id.conditions_currentHumidity);
                    conditions_currentTemperature.setText(temperature + " °F");
                    conditions_currentHumidity.setText(humidity + "  %");
                    tempInputData = "";
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void connected() {
                Toast.makeText(getApplicationContext(),
                        "Connected", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void connecting() {
                Toast.makeText(getApplicationContext(),
                        "Connecting:" + bluetoothMAC, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void none() {
                Toast.makeText(getApplicationContext(),
                        "None", Toast.LENGTH_SHORT).show();
            }
        };

        Set<BluetoothDevice> pairedDevices
                = BluetoothHelper.mblue.getBondedDevices();
        for(BluetoothDevice device : pairedDevices) {
            Toast.makeText(getApplicationContext(),
                    device.getName() + device.getAddress(), Toast.LENGTH_SHORT).show();
        }
        try {
            myDevice = new BluetoothHelper(blueDevice, behavior);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        // TODO: Consider closing sockets
        super.onDestroy();
    }
}