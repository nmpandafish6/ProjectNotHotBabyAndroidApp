package com.cecs490.pnhb.projectnothotbaby;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nicolas on 2/5/2018.
 */

public class ResourceMaster {

    public static SharedPreferences preferences;
    public static SharedPreferences.Editor preferenceEditor;

    public static SharedPreferences tempPreferences;
    public static SharedPreferences.Editor tempPreferenceEditor;

    public static List<String> pairedDeviceList;

    public static Context m_context;
    public static Activity m_activity;
    public static TextView conditions_currentTemperature;
    public static TextView conditions_currentHumidity;
    public static TextView conditions_isOccupied;

    public static TextView home_currentTemperature;
    public static TextView home_currentHumidity;
    public static TextView home_isOccupied;

    public static TextView statistics_high_temperature;
    public static TextView statistics_average_temperature;
    public static TextView statistics_low_temperature;
    public static TextView statistics_high_dewpoint;
    public static TextView statistics_average_dewpoint;
    public static TextView statistics_low_dewpoint;
    public static TextView statistics_high_mttr;
    public static TextView statistics_average_mttr;
    public static TextView statistics_low_mttr;

    public static NotificationManager mNotificationManager;
    public static NotificationCompat.Builder mBuilder;

    public static double lastTemperature = -1000;
    public static double lastHumidity = -1000;
    public static double lastDew = -1000;
    public static boolean lastOccupied = false;
    public static double lastSeverity = 0;

    public static BluetoothHelper myDevice;

    public static List<RssFeedModel> mFeedModelList = new ArrayList<>();
    public static RssFeedListAdapter news_adapter;


    public static void init(Context context, Activity activity){
        m_context = context;
        m_activity = activity;
        ResourceMaster.preferences = m_context.getSharedPreferences("Preferences", 0);
        ResourceMaster.preferenceEditor = ResourceMaster.preferences.edit();

        ResourceMaster.tempPreferences = m_context.getSharedPreferences("Temp Preferences", 0);
        ResourceMaster.tempPreferenceEditor = ResourceMaster.tempPreferences.edit();

        ResourceMaster.conditions_currentTemperature = m_activity.findViewById(R.id.conditions_currentTemperature);
        ResourceMaster.conditions_currentHumidity = m_activity.findViewById(R.id.conditions_currentHumidity);
        ResourceMaster.conditions_isOccupied = m_activity.findViewById(R.id.conditions_isOccupied);

        ResourceMaster.home_currentTemperature = m_activity.findViewById(R.id.home_currentTemperature);
        ResourceMaster.home_currentHumidity = m_activity.findViewById(R.id.home_currentHumidity);
        ResourceMaster.home_isOccupied = m_activity.findViewById(R.id.home_isOccupied);

        ResourceMaster.statistics_high_temperature = m_activity.findViewById(R.id.statistics_high_temp);
        ResourceMaster.statistics_average_temperature = m_activity.findViewById(R.id.statistics_average_temp);
        ResourceMaster.statistics_low_temperature = m_activity.findViewById(R.id.statistics_low_temp);
        ResourceMaster.statistics_high_dewpoint = m_activity.findViewById(R.id.statistics_high_dewpoint);
        ResourceMaster.statistics_average_dewpoint = m_activity.findViewById(R.id.statistics_average_dewpoint);
        ResourceMaster.statistics_low_dewpoint = m_activity.findViewById(R.id.statistics_low_dewpoint);
        ResourceMaster.statistics_high_mttr = m_activity.findViewById(R.id.statistics_high_mttr);
        ResourceMaster.statistics_average_mttr = m_activity.findViewById(R.id.statistics_average_mttr);
        ResourceMaster.statistics_low_mttr = m_activity.findViewById(R.id.statistics_low_mttr);

        ResourceMaster.mBuilder = new NotificationCompat.Builder(m_activity.getApplicationContext())
                .setSmallIcon(R.drawable.ic_child_care_black_24dp)
                .setContentTitle("Hot Baby Alert!!!")
                .setContentText("Please retrieve your child and ensure their safety. :D");

        ResourceMaster.mNotificationManager = (NotificationManager) m_activity.getSystemService(Context.NOTIFICATION_SERVICE);

        ResourceMaster.news_adapter = new RssFeedListAdapter(mFeedModelList, ResourceMaster.m_context);

        BlueInstantiation.init();
    }
}
