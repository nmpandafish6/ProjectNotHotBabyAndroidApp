package com.cecs490.pnhb.projectnothotbabyfinal;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
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
            if(ResourceMaster.lastSeverity == 0) {
                notify = false;
                if(ResourceMaster.preferences.getLong(Constants.NOTIFIED_TIME, -1) > 0){
                    long delta = System.currentTimeMillis() - ResourceMaster.preferences.getLong(Constants.NOTIFIED_TIME, -1);
                    double mttr = ((double)delta) / (1000.0 *60.0);
                    Log.e("MTTR_TAG", "MTTR : " + mttr);
                    Log.e("MTTR_TAG", "DELTA : " + delta);
                    DataWriter.writeMTTR(mttr);
                }else{
                    DataWriter.writeMTTR(0);
                }
                ResourceMaster.preferenceEditor.putLong(Constants.NOTIFIED_TIME, -1);
                ResourceMaster.preferenceEditor.commit();
            }
        } catch (InterruptedException e) {
            Log.e("NOTIFY_TAG", e.getMessage());
            e.printStackTrace();
        }

        if (notify) {
            Intent intent = new Intent(ResourceMaster.m_context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(ResourceMaster.m_context, 0, intent, 0);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ResourceMaster.m_context.getApplicationContext(), "1")
                    .setSmallIcon(R.drawable.ic_child_care_black_24dp)
                    .setContentTitle("Hot Baby Alert!!!")
                    .setContentText("Please retrieve your child and ensure their safety. :D")
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
            Intent resultIntent = new Intent(ResourceMaster.m_context, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(ResourceMaster.m_context);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);

            if(ResourceMaster.preferences.getBoolean(Constants.SOUND_MODE_KEY, false)) {
                mBuilder.setChannelId(ResourceMaster.m_channel_low.getId());
            } else {
                mBuilder.setChannelId(ResourceMaster.m_channel_min.getId());
            }
            if(ResourceMaster.preferences.getBoolean(Constants.VIBRATE_MODE_KEY, false)) {
                Vibrator vibrator = (Vibrator)ResourceMaster.m_context.getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(VibrationEffect.createWaveform(Constants.VIBRATE, -1));
            }
            ResourceMaster.mNotificationManager.notify(1, mBuilder.build());
            Log.e("NOTIFY_TAG", "NOTIFYIED :)" );
            if(ResourceMaster.preferences.getLong(Constants.NOTIFIED_TIME, -1) <= 0) {
                ResourceMaster.preferenceEditor.putLong(Constants.NOTIFIED_TIME, System.currentTimeMillis());
                ResourceMaster.preferenceEditor.commit();
            }
            notificationEnd = Long.MAX_VALUE;

            if(ResourceMaster.lastSeverity > 0) {
                long notificationTime = (long) ((double)(m_delayTime / 2));
                notificationTime = Math.max(notificationTime, 15000);
                new NotificationDelayThread(notificationTime);
            }
        }
    }
}