package com.example.musicapp;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import com.example.musicapp.adapters.SongsRecyclerViewAdapter;
import com.example.musicapp.models.CurrentSong;

import java.io.IOException;

public class MusicService extends Service {

    private static final int PREVIOUS_REQUEST_CODE = 2;
    private static final int NEXT_REQUEST_CODE = 3;
    private static final int PAUSE_RESUME_REQUEST_CODE = 4;

    private MusicBridgeServiceActivity mBridge;
    private IBinder mBinder = new LocalBinder();
    private MediaPlayer mPlayer;

    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // the notification buttons controller
        switch (intent.getIntExtra("music_control", -1)) {
            case NEXT_REQUEST_CODE:
                // play a next song
                nextSong(mBridge.getCurrentSong(), mBridge.getAdapter());
                break;

            case PREVIOUS_REQUEST_CODE:
                // play a previous song
                previousSong(mBridge.getCurrentSong(), mBridge.getAdapter());
                break;

            case PAUSE_RESUME_REQUEST_CODE:
                RemoteViews remoteViews =
                        new RemoteViews(getPackageName(), R.layout.partial_music_notification);

                if (mPlayer.isPlaying()) {
                    // pause
                    pauseSong();
                    remoteViews.setImageViewResource(
                            R.id.image_play_stop_notification,
                            R.drawable.ic_play_song);
                } else {
                    // resume
                    resumeSong();
                    remoteViews.setImageViewResource(
                            R.id.image_play_stop_notification,
                            R.drawable.ic_stop_song);
                }
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public MediaPlayer getPlayer() {
        return mPlayer;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        mPlayer = mediaPlayer;
    }

    public void playSong(CurrentSong currentSong) {
        try {
            // turn the song on
            mPlayer = new MediaPlayer();
            mPlayer.setDataSource(currentSong.getUrl());
            mPlayer.prepare();
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    mBridge.playSongPreparedPlayer();
                }
            });

            mBridge.playSong();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pauseSong() {
        mPlayer.pause();
        mBridge.pauseSong();
    }

    public void resumeSong() {
        mPlayer.start();
        mBridge.resumeSong();
    }

    public void destroySong() {
        mPlayer.stop();
        mPlayer.reset();
        mPlayer.release();
        mPlayer = null;
        mBridge.destroySong();
    }

    public void nextSong(CurrentSong currentSong, SongsRecyclerViewAdapter adapter) {
        destroySong();

        int position;
        if (currentSong.getPosition() == (adapter.getItemCount() - 1)) {
            // play the first song
            position = 0;
        } else {
            // play the next song
            position = currentSong.getPosition() + 1;
        }

        playSong(mBridge.nextSong(position));
    }

    public void previousSong(CurrentSong currentSong, SongsRecyclerViewAdapter adapter) {
        destroySong();

        int position;
        if (currentSong.getPosition() == 0) {
            // play the last song
            position = adapter.getItemCount() - 1;
        } else {
            // play the previous song
            position = currentSong.getPosition() - 1;
        }

        playSong(mBridge.previousSong(position));
    }

    public void seekTo(int time) {
        mPlayer.seekTo(time);
    }

    public void setMusicServiceBridge(MusicBridgeServiceActivity musicBridgeServiceActivity) {
        mBridge = musicBridgeServiceActivity;
    }

    public interface MusicBridgeServiceActivity {
        void playSongPreparedPlayer();

        void playSong();

        void pauseSong();

        void resumeSong();

        void destroySong();

        CurrentSong nextSong(int position);

        CurrentSong previousSong(int position);

        CurrentSong getCurrentSong();

        SongsRecyclerViewAdapter getAdapter();
    }

}
