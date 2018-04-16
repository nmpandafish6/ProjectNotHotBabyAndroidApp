package com.cecs490.pnhb.projectnothotbaby;

import android.media.RingtoneManager;
import android.util.Log;

/**
 * Created by Nicolas on 3/11/2018.
 */

public class NotificationDelayThread extends Thread {
    private long m_delayTime = 0;
    private boolean notify = true;
    private static long notificationEnd = Long.MAX_VALUE;
    private static NotificationDelayThread notificationDelayThread = null;

    public NotificationDelayThread(long delayTime) {
        Log.e("NOTIFY_TAG", "CREATING THREAD WITH : " + delayTime);
        m_delayTime = delayTime;
        long notificationEndTemp = System.currentTimeMillis() + m_delayTime;
        if (notificationEndTemp < notificationEnd) {
            if (notificationDelayThread != null)
                notificationDelayThread.notify = false;
            Log.e("NOTIFY_TAG", "About to start");
            this.start();
            notificationEnd = notificationEndTemp;
        }
    }

    public void run() {
        notificationDelayThread = this;
        try {
            Log.e("NOTIFY_TAG", "SLEEPING : " + m_delayTime);
            Thread.sleep(m_delayTime);
        } catch (InterruptedException e) {
            Log.e("NOTIFY_TAG", e.getMessage());
            e.printStackTrace();
        }
        if (notify) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ResourceMaster.m_context.getApplicationContext(), "1");
             //       .setSmallIcon(R.drawable.ic_child_care_black_24dp)
             //       .setContentTitle("Hot Baby Alert!!!")
             //       .setContentText("Please retrieve your child and ensure their safety. :D");
            if(ResourceMaster.preferences.getBoolean(Constants.SOUND_MODE_KEY, false))
                ResourceMaster.mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            if(ResourceMaster.preferences.getBoolean(Constants.VIBRATE_MODE_KEY, false))
                ResourceMaster.mBuilder.setVibrate(Constants.VIBRATE);
            ResourceMaster.mNotificationManager.notify(1, ResourceMaster.mBuilder.build());
            Log.e("NOTIFY_TAG", "NOTIFYIED :)" );
            notificationEnd = Long.MAX_VALUE;
        }
    }
}