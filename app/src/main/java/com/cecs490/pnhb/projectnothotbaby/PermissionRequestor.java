package com.cecs490.pnhb.projectnothotbaby;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by Nicolas on 4/2/2018.
 */

public class PermissionRequestor {

    public static void request(){
        if (ContextCompat.checkSelfPermission(ResourceMaster.m_activity,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(ResourceMaster.m_activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                ActivityCompat.requestPermissions(ResourceMaster.m_activity,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        Constants.MY_PERMISSIONS_REQUEST_COURSE_LOCATION);
            } else {
                ActivityCompat.requestPermissions(ResourceMaster.m_activity,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        Constants.MY_PERMISSIONS_REQUEST_COURSE_LOCATION);
            }
        } else {
        }
    }

}
