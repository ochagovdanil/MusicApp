package com.example.musicapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

public class MusicForegroundService extends Service {

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "channel_id_01";

    // buttons notification control
    private static final int MUSIC_SERVICE_REQUEST_CODE = 1;
    private static final int PREVIOUS_REQUEST_CODE = 2;
    private static final int NEXT_REQUEST_CODE = 3;
    private static final int PAUSE_RESUME_REQUEST_CODE = 4;

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

        // listeners
        Intent notificationIntent = new Intent();
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(
                this,
                MUSIC_SERVICE_REQUEST_CODE,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextSongIntent = new Intent(this, MusicService.class);
        nextSongIntent.putExtra("music_control", NEXT_REQUEST_CODE);
        PendingIntent nextSongPendingIntent = PendingIntent.getService(
                this,
                NEXT_REQUEST_CODE,
                nextSongIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent previousSongIntent = new Intent(this, MusicService.class);
        previousSongIntent.putExtra("music_control", PREVIOUS_REQUEST_CODE);
        PendingIntent previousPendingIntent = PendingIntent.getService(
                this,
                PREVIOUS_REQUEST_CODE,
                previousSongIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseResumeSongIntent = new Intent(this, MusicService.class);
        pauseResumeSongIntent.putExtra("music_control", PAUSE_RESUME_REQUEST_CODE);
        PendingIntent pauseResumePendingIntent = PendingIntent.getService(
                this,
                PAUSE_RESUME_REQUEST_CODE,
                pauseResumeSongIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // add the custom layout
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.partial_music_notification);
        remoteViews.setTextViewText(R.id.text_title_notification, intent.getStringExtra("title"));
        remoteViews.setTextViewText(R.id.text_info_notification, intent.getStringExtra("artist"));

        remoteViews.setOnClickPendingIntent(R.id.image_next_notification, nextSongPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.image_previous_notification, previousPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.image_play_stop_notification, pauseResumePendingIntent);

        if (intent.getBooleanExtra("pause", false)) {
            remoteViews.setImageViewResource(
                    R.id.image_play_stop_notification,
                    R.drawable.ic_play_song);
        } else {
            remoteViews.setImageViewResource(
                    R.id.image_play_stop_notification,
                    R.drawable.ic_stop_song);
        }


        // builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this,
                CHANNEL_ID);
        builder.setAutoCancel(false)
                .setCustomContentView(remoteViews)
                .setTicker(getString(R.string.app_name))
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setContentIntent(notificationPendingIntent)
                .setSmallIcon(R.drawable.ic_stat_notify_music);

        startForeground(NOTIFICATION_ID, builder.build());

        return START_REDELIVER_INTENT;
    }

}
