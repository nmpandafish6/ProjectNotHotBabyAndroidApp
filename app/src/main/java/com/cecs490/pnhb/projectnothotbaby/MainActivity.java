package com.cecs490.pnhb.projectnothotbaby;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

import static com.cecs490.pnhb.projectnothotbaby.ResourceMaster.news_adapter;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ResourceMaster.init(this, this);



        RecyclerView mRecyclerView = (RecyclerView) (ResourceMaster.m_activity).findViewById(R.id.news_feed);
        mRecyclerView.setAdapter(news_adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        RecyclerView mRecyclerView2 = (RecyclerView) (ResourceMaster.m_activity).findViewById(R.id.home_feed);
        mRecyclerView2.setAdapter(news_adapter);
        mRecyclerView2.setLayoutManager(new LinearLayoutManager(this));

        TextView news_location = (TextView) (ResourceMaster.m_activity).findViewById(R.id.news_location);
        TextView news_weather = (TextView)  (ResourceMaster.m_activity).findViewById(R.id.news_weather);
        TextView news_weather_condtion = (TextView)  (ResourceMaster.m_activity).findViewById(R.id.news_weather_conditions);
        ImageView news_weather_icon = (ImageView) (ResourceMaster.m_activity).findViewById(R.id.news_weather_icon);
        news_location.setText( ResourceMaster.preferences.getString(Constants.LAST_WEATHER_LOCATION_KEY, ""));
        news_weather.setText( ResourceMaster.preferences.getString(Constants.LAST_WEATHER_KEY, ""));
        news_weather_condtion.setText( ResourceMaster.preferences.getString(Constants.LAST_WEATHER_CONDITION_KEY, ""));
        TextView home_location = (TextView) (ResourceMaster.m_activity).findViewById(R.id.home_location);
        TextView home_weather = (TextView)  (ResourceMaster.m_activity).findViewById(R.id.home_weather);
        TextView home_weather_condtion = (TextView)  (ResourceMaster.m_activity).findViewById(R.id.home_weather_conditions);
        ImageView home_weather_icon = (ImageView) (ResourceMaster.m_activity).findViewById(R.id.home_weather_icon);
        home_location.setText( ResourceMaster.preferences.getString(Constants.LAST_WEATHER_LOCATION_KEY, ""));
        home_weather.setText( ResourceMaster.preferences.getString(Constants.LAST_WEATHER_KEY, ""));
        home_weather_condtion.setText( ResourceMaster.preferences.getString(Constants.LAST_WEATHER_CONDITION_KEY, ""));
        String iconURI = ResourceMaster.preferences.getString(Constants.LAST_ICON_URI_KEY, "INVALID");
        if(!iconURI.equals("INVALID")){
            int urlStart = Math.max(iconURI.indexOf("http://") + "http://".length(),
                    iconURI.indexOf("https://") + "https://".length());
            String fileName = iconURI.substring(urlStart);
            fileName = fileName.replaceAll("/", "_");
            Log.e("IMAGE_GETTER_TAG", fileName);
            File file = new File(ResourceMaster.m_context.getFilesDir(), fileName);
            Log.e("IMAGE_GETTER_TAG", file.getAbsolutePath());
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),bmOptions);
            news_weather_icon.setImageBitmap(bitmap);
            home_weather_icon.setImageBitmap(bitmap);
        }


        if(!ResourceMaster.preferences.getBoolean(Constants.INITIAL_CONTENT_DOWNLOADED_KEY, false)){
            DialogFragment dialog = new InitializationDialog();
            dialog.show(ResourceMaster.m_activity.getFragmentManager(), "tag_init");
        }


        PermissionRequestor.request();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        new NewsHTTPRequest(ResourceMaster.m_context).execute();
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
        new WeatherHTTPRequest(ResourceMaster.m_context).execute();


        AlarmManager alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction("com.cecs490.pnhb.projectnothotbaby");
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

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


        StatisticsHelper.StatisticsPackage stats = StatisticsHelper.getTemperatureStatistics();
        ResourceMaster.statistics_high_temperature.setText(String.format("%1.1f °F",stats.max));
        ResourceMaster.statistics_average_temperature.setText(String.format("%1.1f °F",stats.mean));
        ResourceMaster.statistics_low_temperature.setText(String.format("%1.1f °F",stats.min));
        stats = StatisticsHelper.getTemperatureStatistics();
        ResourceMaster.statistics_high_dewpoint.setText(String.format("%1.1f °F",stats.max));
        ResourceMaster.statistics_average_dewpoint.setText(String.format("%1.1f °F",stats.mean));
        ResourceMaster.statistics_low_dewpoint.setText(String.format("%1.1f °F",stats.min));
        stats = StatisticsHelper.getTemperatureStatistics();
        ResourceMaster.statistics_high_mttr.setText(String.format("%1.1f °F",stats.max));
        ResourceMaster.statistics_average_mttr.setText(String.format("%1.1f °F",stats.mean));
        ResourceMaster.statistics_low_mttr.setText(String.format("%1.1f °F",stats.min));


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
                BlueInstantiation.init();
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
            ViewFlipper vf = (ViewFlipper) findViewById(R.id.content_viewFlipper);
            setupSettingsView();
            vf.setDisplayedChild(4);
            return true;
        }else if(id == android.R.id.home){
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.openDrawer(GravityCompat.START);
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
            vf.setDisplayedChild(0);
        } else if (id == R.id.nav_conditions) {
            vf.setDisplayedChild(1);
        } else if (id == R.id.nav_news) {
            vf.setDisplayedChild(2);
        } else if (id == R.id.nav_stats) {
            vf.setDisplayedChild(3);
        } else if (id == R.id.nav_settings) {
            setupSettingsView();
            vf.setDisplayedChild(4);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.MY_PERMISSIONS_REQUEST_COURSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}