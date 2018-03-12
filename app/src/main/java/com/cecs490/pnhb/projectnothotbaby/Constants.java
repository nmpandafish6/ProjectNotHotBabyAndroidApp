package com.cecs490.pnhb.projectnothotbaby;

/**
 * Created by Nicolas on 2/5/2018.
 */

public class Constants {

    public static final String ADAPTIVE_HUMIDITY_STATE_KEY = "Adaptive Humidity State";
    public static final String BLUETOOTH_MAC_KEY = "Bluetooth MAC Address";
    public static final String TEMPERATURE_THRESHOLD_KEY = "Temperature Threshold";
    public static final String DEW_POINT_THRESHOLD_KEY = "Dew Point Threshold";
    public static final String VIBRATE_MODE_KEY = "Vibrate Mode";
    public static final String SOUND_MODE_KEY = "Sound Mode";
    public static final String SENSITIVITY_KEY = "Sensitivity";

    public static final double[] SCALE_CONSTANTS = {0.9, 1.0, 1.25, 1.5, 2.0};
    public static final String[] SCALE_ALT_NAMES = {"MIN","LOW","MED","HIGH","MAX"};


    public static final long[] VIBRATE = {0,100,200,300};
}
