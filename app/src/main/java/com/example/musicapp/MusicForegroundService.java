package com.example.musicapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

public class MusicForegroundService extends Service {

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "channel_id_01";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music foreground service",
                    NotificationManager.IMPORTANCE_LOW);
            notificationChannel.enableVibration(false);
            notificationChannel.enableLights(false);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        Intent notificationIntent = new Intent();
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(
                this,
                1,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this,
                CHANNEL_ID);
        builder.setAutoCancel(false)
                .setContentTitle(intent.getStringExtra("artist"))
                .setContentText(intent.getStringExtra("title"))
                .setTicker(getString(R.string.app_name))
                .setContentIntent(notificationPendingIntent)
                .setSmallIcon(R.drawable.ic_stat_notify_music);

        startForeground(NOTIFICATION_ID, builder.build());

        return START_REDELIVER_INTENT;
    }

}
