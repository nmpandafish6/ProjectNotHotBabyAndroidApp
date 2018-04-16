package com.cecs490.pnhb.projectnothotbaby;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

/**
 * Created by Nicolas on 3/11/2018.
 */

public class AlarmReceiver extends BroadcastReceiver{
    public AlarmReceiver(){
        Log.e("ALARM_TAG", "MADE RECEIVER");
    }
    @Override
    public void onReceive(Context context, Intent intent){
        Log.e("ALARM_TAG","ALARM RECEIVED");
        try {
            LocationManager lm = (LocationManager) ResourceMaster.m_context.getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            Log.e("GPS_TAG", latitude + "," + longitude);
            new WeatherHTTPRequest(ResourceMaster.m_context).execute();
        }catch (SecurityException e){
            Log.e("GPS_TAG", "I TRIED");
        }catch (Exception e){
            Log.e("ALARM_TAG", "I TRIED");
        }
    }
}
