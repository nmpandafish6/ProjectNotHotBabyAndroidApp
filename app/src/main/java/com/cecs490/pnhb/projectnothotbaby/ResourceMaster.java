package com.cecs490.pnhb.projectnothotbaby;

import android.content.SharedPreferences;
import android.os.Bundle;

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
}
