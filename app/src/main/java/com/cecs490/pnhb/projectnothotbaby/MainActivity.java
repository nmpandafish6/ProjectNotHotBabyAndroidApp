package com.cecs490.pnhb.projectnothotbaby;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.app.NotificationCompat;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private int debug_state = 0;
    private static BluetoothHelper myDevice;

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private static long notificationEnd = Long.MAX_VALUE;
    private static long notificationTime = 100000;
    private static NotificationDelayThread notificationDelayThread = null;
    private class NotificationDelayThread extends Thread {
        public long m_delayTime = 0;
        public boolean notify = true;
        public NotificationDelayThread(long delayTime) {

            m_delayTime = delayTime;
            long notificationEndTemp = System.currentTimeMillis() + m_delayTime;
            if(notificationEndTemp < notificationEnd) {
                if(notificationDelayThread != null){
                    notificationDelayThread.notify = false;
                }
                this.start();
                notificationEnd = notificationEndTemp;
            }
            Date date = new Date(notificationEnd);
            DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
            String dateFormatted = formatter.format(date);
            Log.e("TAG", "NOTIFY @ " + dateFormatted);
        }

        public void run() {
            try {
                Thread.sleep(m_delayTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(notify) {
                mNotificationManager.notify(1, mBuilder.build());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ResourceMaster.preferences = getSharedPreferences("Preferences",0);
        ResourceMaster.preferenceEditor = ResourceMaster.preferences.edit();

        ResourceMaster.tempPreferences = getSharedPreferences("Temp Preferences",0);
        ResourceMaster.tempPreferenceEditor = ResourceMaster.tempPreferences.edit();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // The id of the channel.
        String CHANNEL_ID = "my_channel_01";
        Uri notificationSound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long[] vibrate = {0,100,200,300};
        mBuilder =
                new NotificationCompat.Builder(this.getApplicationContext())
                        .setSmallIcon(R.drawable.ic_child_care_black_24dp)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!")
                        .setVibrate(vibrate)
                        .setSound(notificationSound);
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent resultIntent = new Intent(this, MainActivity.class);
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your app to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);


        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        mNotificationManager.cancel(1);


        setSupportActionBar(toolbar);
        BluetoothHelper.initBluetooth();
        // CPU MAC "A0:88:69:30:CB:1C"
        final String bluetoothMAC = ResourceMaster.preferences.getString(Constants.BLUETOOTH_MAC_KEY,"20:16:12:12:80:68");
        final BluetoothDevice blueDevice = BluetoothHelper.mblue.getRemoteDevice(bluetoothMAC);
        final BluetoothHelper.BluetoothSocketBehavior blueBehavior  = new BluetoothHelper.BluetoothSocketBehavior() {
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
                    boolean occupied = json.getBoolean("OCCUPIED");
                    Toast.makeText(getApplicationContext(),
                            "Temperature: " + temperature + "\nHumidity: " + humidity, Toast.LENGTH_SHORT).show();
                    TextView conditions_currentTemperature = (TextView) findViewById(R.id.conditions_currentTemperature);
                    TextView conditions_currentHumidity = (TextView) findViewById(R.id.conditions_currentHumidity);
                    TextView conditions_isOccupied = (TextView) findViewById(R.id.conditions_isOccupied);
                    conditions_currentTemperature.setText(temperature + " °F");
                    conditions_currentHumidity.setText(humidity + "  %");
                    conditions_isOccupied.setText("" + occupied);
                    tempInputData = "";
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void connected() {
                notificationDelayThread = new NotificationDelayThread(notificationTime);
                notificationTime /= 2;
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
                Log.e("TAG", "INSIDE NONE");
                try {
                    myDevice.close();
                    myDevice.reconnect();
                }catch (Exception e){
                }
            }
        };
        try {
            myDevice = new BluetoothHelper(blueDevice, blueBehavior);
        } catch (IOException e) {
            e.printStackTrace();
        }


        final TextView settings_temp_lower = (TextView) findViewById(R.id.settings_temp_lower);
        final TextView settings_dew_point_lower = (TextView) findViewById(R.id.settings_dew_point_lower);

        settings_temp_lower.setText("" + ResourceMaster.preferences.getInt(Constants.TEMPERATURE_THRESHOLD_KEY, 0));
        settings_dew_point_lower.setText("" + ResourceMaster.preferences.getInt(Constants.DEW_POINT_THRESHOLD_KEY, 0));

        final SeekBar humiditySeekBar = (SeekBar) findViewById(R.id.dewPointSeekBar);
        humiditySeekBar.setProgress(ResourceMaster.preferences.getInt(Constants.DEW_POINT_THRESHOLD_KEY, 0));

        humiditySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    settings_dew_point_lower.setText("" + progress);
                    ResourceMaster.tempPreferenceEditor.putInt(Constants.DEW_POINT_THRESHOLD_KEY, progress);
                    ResourceMaster.tempPreferenceEditor.commit();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        final SeekBar temperatureSeekBar = (SeekBar) findViewById(R.id.temperature_seekbar);
        temperatureSeekBar.setProgress(ResourceMaster.preferences.getInt(Constants.TEMPERATURE_THRESHOLD_KEY, 0));
        temperatureSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    settings_temp_lower.setText("" + progress);
                    ResourceMaster.tempPreferenceEditor.putInt(Constants.TEMPERATURE_THRESHOLD_KEY, progress);
                    ResourceMaster.tempPreferenceEditor.commit();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final Switch adaptiveHumiditySwitch = (Switch)findViewById(R.id.adaptiveHumidityMode);
        adaptiveHumiditySwitch.setChecked(ResourceMaster.preferences.getBoolean(Constants.ADAPTIVE_HUMIDITY_STATE_KEY, false));
        adaptiveHumiditySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Toast.makeText(getApplicationContext(), "" + isChecked, Toast.LENGTH_SHORT).show();
                humiditySeekBar.setEnabled(isChecked);
                ResourceMaster.tempPreferenceEditor.putBoolean(Constants.ADAPTIVE_HUMIDITY_STATE_KEY, isChecked);
                ResourceMaster.tempPreferenceEditor.commit();
            }
        });

        final Switch soundModeSwitch = (Switch)findViewById(R.id.sound_mode);
        soundModeSwitch.setChecked(ResourceMaster.preferences.getBoolean(Constants.SOUND_MODE_KEY, false));
        soundModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ResourceMaster.tempPreferenceEditor.putBoolean(Constants.SOUND_MODE_KEY, isChecked);
                ResourceMaster.tempPreferenceEditor.commit();
            }
        });

        final Switch vibrationModeSwitch = (Switch)findViewById(R.id.vibrate_mode);
        vibrationModeSwitch.setChecked(ResourceMaster.preferences.getBoolean(Constants.VIBRATE_MODE_KEY, false));
        vibrationModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ResourceMaster.tempPreferenceEditor.putBoolean(Constants.VIBRATE_MODE_KEY, isChecked);
                ResourceMaster.tempPreferenceEditor.commit();
            }
        });

        final Button settings_apply = (Button) findViewById(R.id.settings_apply);
        settings_apply.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                ResourceMaster.preferenceEditor.putBoolean(Constants.ADAPTIVE_HUMIDITY_STATE_KEY,
                        ResourceMaster.tempPreferences.getBoolean(Constants.ADAPTIVE_HUMIDITY_STATE_KEY, false));
                ResourceMaster.preferenceEditor.putString(Constants.BLUETOOTH_MAC_KEY,
                        ResourceMaster.tempPreferences.getString(Constants.BLUETOOTH_MAC_KEY, "Dummy MAC Addr"));
                ResourceMaster.preferenceEditor.putInt(Constants.TEMPERATURE_THRESHOLD_KEY,
                        ResourceMaster.tempPreferences.getInt(Constants.TEMPERATURE_THRESHOLD_KEY, 80));
                ResourceMaster.preferenceEditor.putInt(Constants.DEW_POINT_THRESHOLD_KEY,
                        ResourceMaster.tempPreferences.getInt(Constants.DEW_POINT_THRESHOLD_KEY, 80));
                ResourceMaster.preferenceEditor.putBoolean(Constants.VIBRATE_MODE_KEY,
                        ResourceMaster.tempPreferences.getBoolean(Constants.VIBRATE_MODE_KEY, false));
                ResourceMaster.preferenceEditor.putBoolean(Constants.SOUND_MODE_KEY,
                        ResourceMaster.tempPreferences.getBoolean(Constants.SOUND_MODE_KEY, false));
                ResourceMaster.preferenceEditor.commit();

                myDevice.close();
                final String newBluetoothMAC = ResourceMaster.preferences.getString(Constants.BLUETOOTH_MAC_KEY,"20:16:12:12:80:68");
                final BluetoothDevice newBlueDevice = BluetoothHelper.mblue.getRemoteDevice(newBluetoothMAC);
                try {
                    myDevice = new BluetoothHelper(newBlueDevice, blueBehavior);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final JSONObject json = new JSONObject();
                try {
                    json.put("TEMP MIN", ResourceMaster.preferences.getInt(Constants.TEMPERATURE_THRESHOLD_KEY,0));
                    json.put("DEW POINT MIN", ResourceMaster.preferences.getInt(Constants.DEW_POINT_THRESHOLD_KEY,0));
                    json.put("ADAPTIVE HUMIDITY", ResourceMaster.preferences.getBoolean(Constants.ADAPTIVE_HUMIDITY_STATE_KEY, false));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Thread thread = new Thread() {
                    public void run() {
                        boolean success = false;
                        while(!success) {
                            try {
                                myDevice.write(json.toString());
                                success = true;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                thread.start();
            }
        });

        final Spinner bluetoothDeviceSpinner = (Spinner)findViewById(R.id.device_spinner);

        bluetoothDeviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String device = ResourceMaster.pairedDeviceList.get(position);
                String macAddr = device.substring(device.lastIndexOf('[')+1,device.lastIndexOf(']'));
                ResourceMaster.tempPreferenceEditor.putString(Constants.BLUETOOTH_MAC_KEY, macAddr);
                ResourceMaster.tempPreferenceEditor.commit();
                Toast.makeText(getApplicationContext(),
                        macAddr, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


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

    public void setupSettingsView(){
        Set<BluetoothDevice> pairedDevices
                = BluetoothHelper.mblue.getBondedDevices();
        Spinner bluetoothDeviceSpinner = (Spinner)findViewById(R.id.device_spinner);
        ResourceMaster.pairedDeviceList = new ArrayList();
        int i = 0; int index = 0;
        for(BluetoothDevice device: pairedDevices){
            if(device.getAddress().equals(ResourceMaster.preferences.getString(Constants.BLUETOOTH_MAC_KEY, "Dummy MAC Addr"))){
                index = i;
            }
            ResourceMaster.pairedDeviceList.add(device.getName() + " [" + device.getAddress() + "]");
            i++;
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,ResourceMaster.pairedDeviceList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bluetoothDeviceSpinner.setAdapter(spinnerAdapter);
        bluetoothDeviceSpinner.setSelection(index);
        spinnerAdapter.notifyDataSetChanged();

        Switch adaptiveHumiditySwitch = (Switch)findViewById(R.id.adaptiveHumidityMode);
        boolean isAdaptiveHumidityMode = adaptiveHumiditySwitch.isChecked();
        SeekBar humiditySeekBar = (SeekBar) findViewById(R.id.dewPointSeekBar);
        humiditySeekBar.setEnabled(isAdaptiveHumidityMode);
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
            setupSettingsView();
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