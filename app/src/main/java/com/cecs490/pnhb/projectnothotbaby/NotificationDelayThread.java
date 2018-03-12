package com.cecs490.pnhb.projectnothotbaby;

import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;

/**
 * Created by Nicolas on 3/11/2018.
 */

public class NotificationDelayThread extends Thread {
    private long m_delayTime = 0;
    private boolean notify = true;
    private static long notificationEnd = Long.MAX_VALUE;
    private static NotificationDelayThread notificationDelayThread = null;

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
        notificationDelayThread = this;
        try {
            Thread.sleep(m_delayTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (notify) {
            ResourceMaster.mBuilder = new NotificationCompat.Builder(ResourceMaster.m_context.getApplicationContext())
                    .setSmallIcon(R.drawable.ic_child_care_black_24dp)
                    .setContentTitle("Hot Baby Alert!!!")
                    .setContentText("Please retrieve your child and ensure their safety. :D");
            if(ResourceMaster.preferences.getBoolean(Constants.SOUND_MODE_KEY, false))
                ResourceMaster.mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            if(ResourceMaster.preferences.getBoolean(Constants.VIBRATE_MODE_KEY, false))
                ResourceMaster.mBuilder.setVibrate(Constants.VIBRATE);
            ResourceMaster.mNotificationManager.notify(1, ResourceMaster.mBuilder.build());
            notificationEnd = Long.MAX_VALUE;
        }
    }
}