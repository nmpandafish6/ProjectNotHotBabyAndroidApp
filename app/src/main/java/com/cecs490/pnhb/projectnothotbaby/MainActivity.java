package com.cecs490.pnhb.projectnothotbaby;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static BluetoothHelper myDevice;
    private static BluetoothHelper newMyDevice;

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private static long notificationEnd = Long.MAX_VALUE;
    private static long notificationTime = 100000;
    private static NotificationDelayThread notificationDelayThread = null;
    private static double lastTemperature = -1000;
    private static double lastHumidity = -1000;
    private static boolean lastOccupied = false;
    private static double lastSeverity = 0;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private static Location location;
    private static Context m_context;
    private static BluetoothHelper.BluetoothSocketBehavior newBlueBehavior;
    private static BluetoothHelper.BluetoothSocketBehavior blueBehavior;
    private final Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    private final long[] vibrate = {0,100,200,300};

    private class NotificationDelayThread extends Thread {
        private long m_delayTime = 0;
        private boolean notify = true;

        public NotificationDelayThread(long delayTime) {
            m_delayTime = delayTime;
            long notificationEndTemp = System.currentTimeMillis() + m_delayTime;
            if (notificationEndTemp < notificationEnd) {
                if (notificationDelayThread != null)
                    notificationDelayThread.notify = false;
                this.start();
                notificationEnd = notificationEndTemp;
            }
        }

        public void run() {
            try {
                Thread.sleep(m_delayTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (notify) {
                mBuilder =
                        new NotificationCompat.Builder(getApplicationContext())
                                .setSmallIcon(R.drawable.ic_child_care_black_24dp)
                                .setContentTitle("Hot Baby Alert!!!")
                                .setContentText("Please retrieve your child and ensure their safety. :D");

                if(ResourceMaster.preferences.getBoolean(Constants.SOUND_MODE_KEY, false)){
                    mBuilder.setSound(notificationSound);
                }
                if(ResourceMaster.preferences.getBoolean(Constants.VIBRATE_MODE_KEY, false)){
                    mBuilder.setVibrate(vibrate);
                }

                mNotificationManager.notify(1, mBuilder.build());
                notificationEnd = Long.MAX_VALUE;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ResourceMaster.preferences = getSharedPreferences("Preferences", 0);
        ResourceMaster.preferenceEditor = ResourceMaster.preferences.edit();

        ResourceMaster.tempPreferences = getSharedPreferences("Temp Preferences", 0);
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

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.news_feed);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        m_context = this;

        final FetchExternalResourceTask rssFeedFetcher = new FetchExternalResourceTask(m_context);
        rssFeedFetcher.execute("https://nothotbabycecs490.tumblr.com/rss");
        Thread waitForRSSReadyThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!rssFeedFetcher.isCompleted());
                new NewsHTTPRequest(m_context).execute();
            }
        });
        waitForRSSReadyThread.start();


        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }else {
            location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction("com.cecs490.pnhb.projectnothotbaby");
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+10000,
                AlarmManager.INTERVAL_DAY, alarmIntent);

        mBuilder = new NotificationCompat.Builder(this.getApplicationContext())
                        .setSmallIcon(R.drawable.ic_child_care_black_24dp)
                        .setContentTitle("Hot Baby Alert!!!")
                        .setContentText("Please retrieve your child and ensure their safety. :D");
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager.cancel(1);


        setSupportActionBar(toolbar);
        BluetoothHelper.initBluetooth();
        // CPU MAC "A0:88:69:30:CB:1C"
        final String bluetoothMAC = ResourceMaster.preferences.getString(Constants.BLUETOOTH_MAC_KEY,"20:16:12:12:80:68");
        final BluetoothDevice blueDevice = BluetoothHelper.mblue.getRemoteDevice(bluetoothMAC);
        blueBehavior  = new BluetoothHelper.BluetoothSocketBehavior() {
            String tempInputData = "";
            @Override
            public void write() {
            }

            @Override
            public void read() {
                try {
                    String tmp = myDevice.read();
                    if(tmp == null) return;
                    tempInputData += tmp;
                    String jsonString = "";
                    if(tempInputData.contains("}") && tempInputData.contains("{")){
                        jsonString = tempInputData.substring(tempInputData.lastIndexOf("{"),
                                tempInputData.lastIndexOf("}") + 1);
                    }
                    JSONObject json = new JSONObject(jsonString);
                    try {
                        double temperature = json.getDouble("TEMP");
                        lastTemperature = temperature;
                    }catch(Exception e){}
                    try {
                        double humidity = json.getDouble("HUMIDITY");
                        lastHumidity = humidity;
                    }catch(Exception e){}
                    try {
                        boolean occupied = json.getBoolean("OCCUPIED");
                        lastOccupied = occupied;
                    }catch(Exception e){}
                    try {
                        double severity = json.getDouble("SEVERITY");
                        lastSeverity = severity;
                    }catch(Exception e){}

                    TextView conditions_currentTemperature = (TextView) findViewById(R.id.conditions_currentTemperature);
                    TextView conditions_currentHumidity = (TextView) findViewById(R.id.conditions_currentHumidity);
                    TextView conditions_isOccupied = (TextView) findViewById(R.id.conditions_isOccupied);
                    if(lastTemperature > -1000) conditions_currentTemperature.setText(lastTemperature + " °F");
                    if(lastHumidity > -1000) conditions_currentHumidity.setText(lastHumidity + "  %");
                    conditions_isOccupied.setText("" + lastOccupied);
                    int scalePos = ResourceMaster.preferences.getInt(Constants.SENSITIVITY_KEY,1);
                    double scale = Constants.SCALE_CONSTANTS[scalePos];
                    notificationTime = (long) (Math.max((lastSeverity * -10000/37 + 11110000/37)/scale,1));
                    Log.e("NOTIFY_TAG", "" + notificationTime);
                    if(lastSeverity > 0) {
                        notificationDelayThread = new NotificationDelayThread(notificationTime);
                    }
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
                        "Connecting:" + ResourceMaster.preferences.getString(Constants.BLUETOOTH_MAC_KEY,"20:16:12:12:80:68"),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void none() {
                Log.e("BLUETOOTH_TAG", "INSIDE NONE");
                try {
                }catch (Exception e){
                }
            }
        };
        try {
            if (myDevice == null || !myDevice.getIsConnected()) {
                Log.e("BLUETOOTH_TAG", "Connecting to " + bluetoothMAC);
                myDevice = new BluetoothHelper(blueDevice, blueBehavior);
            }else{
                Log.e("BLUETOOTH_TAG", "Already Connected to :" + bluetoothMAC);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        final TextView sensitivity_setting = (TextView) findViewById(R.id.sensitivity_setting);

        sensitivity_setting.setText("" + Constants.SCALE_ALT_NAMES[ResourceMaster.preferences.getInt(Constants.SENSITIVITY_KEY, 0)]);
        final SeekBar sensitivity_seekbar = (SeekBar) findViewById(R.id.sensitivity_seekbar);
        sensitivity_seekbar.setProgress(ResourceMaster.preferences.getInt(Constants.SENSITIVITY_KEY, 0));
        sensitivity_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    sensitivity_setting.setText("" + Constants.SCALE_ALT_NAMES[progress]);
                    ResourceMaster.tempPreferenceEditor.putInt(Constants.SENSITIVITY_KEY, progress);
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
                ResourceMaster.preferenceEditor.putInt(Constants.SENSITIVITY_KEY,
                        ResourceMaster.tempPreferences.getInt(Constants.SENSITIVITY_KEY, 0));
                ResourceMaster.preferenceEditor.putInt(Constants.DEW_POINT_THRESHOLD_KEY,
                        ResourceMaster.tempPreferences.getInt(Constants.DEW_POINT_THRESHOLD_KEY, 80));
                ResourceMaster.preferenceEditor.putBoolean(Constants.VIBRATE_MODE_KEY,
                        ResourceMaster.tempPreferences.getBoolean(Constants.VIBRATE_MODE_KEY, false));
                ResourceMaster.preferenceEditor.putBoolean(Constants.SOUND_MODE_KEY,
                        ResourceMaster.tempPreferences.getBoolean(Constants.SOUND_MODE_KEY, false));
                ResourceMaster.preferenceEditor.commit();

                try {
                    if(myDevice.getIsConnected() && myDevice.mDevice.getAddress() != ResourceMaster.preferences.getString(Constants.BLUETOOTH_MAC_KEY,"20:16:12:12:80:68")) {
                        final String newBluetoothMAC = ResourceMaster.preferences.getString(Constants.BLUETOOTH_MAC_KEY, "20:16:12:12:80:68");
                        final BluetoothDevice newBlueDevice = BluetoothHelper.mblue.getRemoteDevice(newBluetoothMAC);

                        newMyDevice = null;
                        newBlueBehavior = null;
                        newBlueBehavior = new BluetoothHelper.BluetoothSocketBehavior() {
                            String tempInputData = "";
                            @Override
                            public void write() {
                            }

                            @Override
                            public void read() {
                                try {
                                    String tmp = newMyDevice.read();
                                    if(tmp == null) return;
                                    tempInputData += tmp;
                                    String jsonString = "";
                                    if(tempInputData.contains("}") && tempInputData.contains("{")){
                                        jsonString = tempInputData.substring(tempInputData.lastIndexOf("{"),
                                                tempInputData.lastIndexOf("}") + 1);
                                    }
                                    JSONObject json = new JSONObject(jsonString);
                                    try {
                                        double temperature = json.getDouble("TEMP");
                                        lastTemperature = temperature;
                                    }catch(Exception e){}
                                    try {
                                        double humidity = json.getDouble("HUMIDITY");
                                        lastHumidity = humidity;
                                    }catch(Exception e){}
                                    try {
                                        boolean occupied = json.getBoolean("OCCUPIED");
                                        lastOccupied = occupied;
                                    }catch(Exception e){}
                                    try {
                                        double severity = json.getDouble("SEVERITY");
                                        lastSeverity = severity;
                                    }catch(Exception e){}

                                    TextView conditions_currentTemperature = (TextView) findViewById(R.id.conditions_currentTemperature);
                                    TextView conditions_currentHumidity = (TextView) findViewById(R.id.conditions_currentHumidity);
                                    TextView conditions_isOccupied = (TextView) findViewById(R.id.conditions_isOccupied);
                                    if(lastTemperature > -1000) conditions_currentTemperature.setText(lastTemperature + " °F");
                                    if(lastHumidity > -1000) conditions_currentHumidity.setText(lastHumidity + "  %");
                                    conditions_isOccupied.setText("" + lastOccupied);
                                    int scalePos = ResourceMaster.preferences.getInt(Constants.SENSITIVITY_KEY,1);
                                    double scale = Constants.SCALE_CONSTANTS[scalePos];
                                    notificationTime = (long) (Math.max((lastSeverity * -10000/37 + 11110000/37)/scale,1));
                                    Log.e("NOTIFY_TAG", "" + notificationTime);
                                    if(lastSeverity > 0) {
                                        notificationDelayThread = new NotificationDelayThread(notificationTime);
                                    }
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
                                        "Connecting:" + ResourceMaster.preferences.getString(Constants.BLUETOOTH_MAC_KEY,"20:16:12:12:80:68"),
                                        Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void none() {
                                Log.e("BLUETOOTH_TAG", "INSIDE NONE");
                            }
                        };
                        try {

                            Log.e("BLUETOOTH_TAG","Connecting to " + newBluetoothMAC);
                            newMyDevice = new BluetoothHelper(newBlueDevice, newBlueBehavior);
                            myDevice = newMyDevice;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                    Log.e("EXCEPTION", e.getMessage());
                }
                final JSONObject json = new JSONObject();
                try {
                    json.put("SENSITIVITY", Constants.SCALE_CONSTANTS[ResourceMaster.preferences.getInt(Constants.SENSITIVITY_KEY, 0)]);
                    json.put("ADAPTIVE HUMIDITY", ResourceMaster.preferences.getBoolean(Constants.ADAPTIVE_HUMIDITY_STATE_KEY, false));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Thread thread = new Thread() {
                    private String mac = ResourceMaster.preferences.getString(Constants.BLUETOOTH_MAC_KEY, "20:16:12:12:80:68");
                    public void run() {
                        boolean success = false;
                        while(!success) {
                            try {
                                myDevice.write(json.toString());
                                success = true;
                            } catch (Exception e) {
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
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
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

    public static class AlarmReceiver extends BroadcastReceiver {

        public AlarmReceiver(){
            Log.e("ALARM_TAG", "MADE RECEIVER");
        }
        @Override
        public void onReceive(Context context, Intent intent){
            Log.e("ALARM_TAG","ALARM RECEIVED");

            try {
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
                Log.e("GPS_TAG", latitude + "," + longitude);
                new WeatherHTTPRequest(m_context).execute(context);
            }catch (Exception e){
                Log.e("ALARM_TAG", "I TRIED");
            }
        }
    }
}