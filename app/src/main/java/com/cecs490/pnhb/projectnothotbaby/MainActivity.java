package com.cecs490.pnhb.projectnothotbaby;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.ViewFlipper;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private PendingIntent alarmIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ResourceMaster.init(this, this);
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


        final FetchExternalResourceTask rssFeedFetcher = new FetchExternalResourceTask(ResourceMaster.m_context);
        rssFeedFetcher.execute("https://nothotbabycecs490.tumblr.com/rss");
        Thread waitForRSSReadyThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!rssFeedFetcher.isCompleted());
                new NewsHTTPRequest(ResourceMaster.m_context).execute();
            }
        });
        waitForRSSReadyThread.start();


        AlarmManager alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction("com.cecs490.pnhb.projectnothotbaby");
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+10000,
                AlarmManager.INTERVAL_DAY, alarmIntent);



        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        ResourceMaster.mBuilder.setContentIntent(resultPendingIntent);
        ResourceMaster.mNotificationManager.cancel(1);


        setSupportActionBar(toolbar);



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
            public void onClick(View v) {
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
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
            if(device.getAddress().equals(ResourceMaster.preferences.getString(Constants.BLUETOOTH_MAC_KEY, "Dummy MAC Addr")))
                index = i;
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
        super.onDestroy();
    }

}