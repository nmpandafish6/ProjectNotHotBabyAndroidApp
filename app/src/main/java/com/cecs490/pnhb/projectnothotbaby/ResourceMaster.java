package com.cecs490.pnhb.projectnothotbaby;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.TextView;

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
    public static TextView conditions_currentTemperature;
    public static TextView conditions_currentHumidity;
    public static TextView conditions_isOccupied;
    public static NotificationManager mNotificationManager;
    public static NotificationCompat.Builder mBuilder;

    public static double lastTemperature = -1000;
    public static double lastHumidity = -1000;
    public static boolean lastOccupied = false;
    public static double lastSeverity = 0;

    public static BluetoothHelper myDevice;

    public static void init(Context context, Activity activity){
        m_context = context;
        Activity m_activity = activity;
        ResourceMaster.preferences = m_context.getSharedPreferences("Preferences", 0);
        ResourceMaster.preferenceEditor = ResourceMaster.preferences.edit();

        ResourceMaster.tempPreferences = m_context.getSharedPreferences("Temp Preferences", 0);
        ResourceMaster.tempPreferenceEditor = ResourceMaster.tempPreferences.edit();

        ResourceMaster.conditions_currentTemperature = m_activity.findViewById(R.id.conditions_currentTemperature);
        ResourceMaster.conditions_currentHumidity = m_activity.findViewById(R.id.conditions_currentHumidity);
        ResourceMaster.conditions_isOccupied = m_activity.findViewById(R.id.conditions_isOccupied);

        ResourceMaster.mBuilder = new NotificationCompat.Builder(m_activity.getApplicationContext())
                .setSmallIcon(R.drawable.ic_child_care_black_24dp)
                .setContentTitle("Hot Baby Alert!!!")
                .setContentText("Please retrieve your child and ensure their safety. :D");

        ResourceMaster.mNotificationManager = (NotificationManager) m_activity.getSystemService(Context.NOTIFICATION_SERVICE);


        BlueInstantiation.init();
    }
}
