package com.example.musicapp.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musicapp.MusicForegroundService;
import com.example.musicapp.MusicService;
import com.example.musicapp.R;
import com.example.musicapp.adapters.SongsRecyclerViewAdapter;
import com.example.musicapp.helpers.PreferencesApp;
import com.example.musicapp.models.CurrentSong;
import com.example.musicapp.models.Song;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int READ_EXTERNAL_STORAGE_PERMISSION = 1;
    public static final int DELETE_SONG_REQUEST_CODE = 2;

    private MusicService mMusicService;
    private boolean isBound = false;
    private PreferencesApp mPreferences;
    private SongsRecyclerViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private TextView mTitle, mArtist, mCurrentTime, mDuration;
    private SeekBar mSeekBar;
    private Handler mHandler;
    private ImageView mPlayStopButton, mNextButton, mPreviousButton, mLogo, mButtonTop;
    private CurrentSong mCurrentSong; // save data of a current song

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // send the request
        ActivityCompat.requestPermissions(
                MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                READ_EXTERNAL_STORAGE_PERMISSION);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // run the music bound service
        Intent intent = new Intent(MainActivity.this, MusicService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isBound && mMusicService.getPlayer() != null && mCurrentSong != null) {
            mMusicService.getPlayer().stop();
            mMusicService.getPlayer().reset();
            mMusicService.getPlayer().release();
            mMusicService.setMediaPlayer(null);
            mCurrentSong = null;
            stopService(new Intent(MainActivity.this, MusicForegroundService.class));

            // stop the music bound service
            unbindService(mServiceConnection);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) initApp();
            else finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            if (resultCode == RESULT_OK) {
                // delete a current song
                if (requestCode == DELETE_SONG_REQUEST_CODE) {
                    if (data.getBooleanExtra("deleted", false)) {
                        // destroy a current song
                        int position = mCurrentSong.getPosition();

                        mAdapter.deleteSong(position);
                        mMusicService.destroySong();
                        Toast.makeText(
                                MainActivity.this,
                                R.string.delete_song_msg,
                                Toast.LENGTH_SHORT).show();

                        // play the next song
                        int newPosition = position;

                        if (position == (mAdapter.getItemCount())) {
                            // play the first song if we delete the last
                            newPosition = 0;
                        }

                        updateCurrentSong(newPosition);
                        scrollTo(newPosition);
                        mMusicService.playSong(mCurrentSong);
                    }
                }
            }
        }
    }

    private void initApp() {
        mPreferences = new PreferencesApp(MainActivity.this);
        mHandler = new Handler();
        mLogo = findViewById(R.id.image_logo);
        mButtonTop = findViewById(R.id.float_button_top);
        mArtist = findViewById(R.id.text_artist_song);
        mTitle = findViewById(R.id.text_title_song);
        mCurrentTime = findViewById(R.id.text_current_time);
        mDuration = findViewById(R.id.text_duration_time);
        mSeekBar = findViewById(R.id.seek_bar_song);
        mPlayStopButton = findViewById(R.id.button_play_stop);
        mNextButton = findViewById(R.id.button_next);
        mPreviousButton = findViewById(R.id.button_previous);

        loadListOfSongs();
        selectSongFromTheList();
        playMainButtonsBehavior();
        seekTo();
        scrollToTheTop();
        repeatSong();
        showDetails();
        showVolumeControl();
        controlMusicByHeadphones();
    }

    private void loadListOfSongs() {
        mAdapter = new SongsRecyclerViewAdapter(MainActivity.this);

        // get albums and search for songs by the albums
        List<Song> listSong = new ArrayList<>(); // use to sort songs by their titles
        Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        Cursor cursorAlbum = getContentResolver().query(
                uri,
                null,
                null,
                null,
                null);

        if (cursorAlbum != null && cursorAlbum.moveToFirst()) {
            do {
                String albumArt = cursorAlbum.getString(
                        cursorAlbum.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                int albumId = cursorAlbum.getInt(
                        cursorAlbum.getColumnIndex(MediaStore.Audio.Albums._ID));
                String albumName =
                        cursorAlbum.getString(
                                cursorAlbum.getColumnIndex(MediaStore.Audio.Albums.ALBUM));
                int firstYear =
                        cursorAlbum.getInt(
                                cursorAlbum.getColumnIndex(MediaStore.Audio.Albums.FIRST_YEAR));
                int lastYear =
                        cursorAlbum.getInt(
                                cursorAlbum.getColumnIndex(MediaStore.Audio.Albums.LAST_YEAR));
                String selection = "is_music != 0";

                if (albumId > 0) {
                    selection += " and album_id = " + albumId;
                }

                Uri uri1 = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                Cursor cursorSong = getContentResolver().query(
                        uri1,
                        null,
                        selection,
                        null,
                        null);

                if (cursorSong != null) {
                    if (cursorSong.moveToFirst()) {
                        do {
                            Song song = new Song(
                                cursorSong.getString(
                                        cursorSong.getColumnIndex(MediaStore.Audio.Media.DATA)),
                                cursorSong.getString(
                                        cursorSong.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                                cursorSong.getString(
                                        cursorSong.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                                albumName,
                                firstYear,
                                lastYear,
                                cursorSong.getLong(
                                        cursorSong.getColumnIndex(MediaStore.Audio.Media.DURATION)),
                                cursorSong.getLong(
                                        cursorSong.getColumnIndex(MediaStore.Audio.Media.SIZE)),
                                cursorSong.getString(
                                        cursorSong.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)));

                            if (albumArt == null) {
                                // set the default icon
                                song.setImage(BitmapFactory.decodeResource(
                                                            getResources(),
                                                            R.drawable.ic_song_cover));
                            } else {
                                // set an album icon
                                song.setImage(BitmapFactory.decodeFile(albumArt));
                            }

                            listSong.add(song);
                        } while (cursorSong.moveToNext());
                    }

                    cursorSong.close();
                }
            } while (cursorAlbum.moveToNext());

            cursorAlbum.close();
        }

        // sort songs by their titles
        if (listSong.size() > 0) {
            Collections.sort(listSong, new Comparator<Song>() {
                @Override
                public int compare(final Song object1, final Song object2) {
                    return object1.getTitle().compareTo(object2.getTitle());
                }
            });
        }

        for (int i = 0; i < listSong.size(); i++) {
            mAdapter.addSong(listSong.get(i));
        }

        // init RecyclerView
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView = findViewById(R.id.recycler_view_songs);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);

        // show or hide the button top
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (linearLayoutManager.findFirstCompletelyVisibleItemPosition() != 0) {
                    mButtonTop.setVisibility(View.VISIBLE);
                } else {
                    mButtonTop.setVisibility(View.GONE);
                }
            }
        });
    }

    private void selectSongFromTheList() {
        mAdapter.setOnItemClickListener(new SongsRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClickListener(Song song, int position) {
                mCurrentSong =
                        new CurrentSong(
                                position,
                                song.getImage(),
                                song.getUrl(),
                                song.getTitle(),
                                song.getArtist(),
                                song.getAlbum(),
                                song.getFirstYear(),
                                song.getLastYear(),
                                song.getDuration(),
                                song.getSize(),
                                song.getMimType());

                if (isBound) {
                    if (mMusicService.getPlayer() == null)
                        mMusicService.playSong(mCurrentSong); // play a selected song
                    else {
                        mMusicService.destroySong(); // stop the previous song and play again
                        mMusicService.playSong(mCurrentSong);
                    }
                }
            }
        });
    }

    private void playMainButtonsBehavior() {
        mPlayStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound && mMusicService.getPlayer() != null) {
                    if (mMusicService.getPlayer().isPlaying()) mMusicService.pauseSong();
                    else mMusicService.resumeSong();
                }
            }
        });
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound && mMusicService.getPlayer() != null) {
                    mMusicService.nextSong(mCurrentSong, mAdapter);
                }
            }
        });
        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound && mMusicService.getPlayer() != null) {
                    mMusicService.previousSong(mCurrentSong, mAdapter);
                }
            }
        });
    }

    private void scrollToTheTop() {
        mButtonTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.smoothScrollToPosition(0);
            }
        });
    }

    private void seekTo() {
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mMusicService.getPlayer() != null) {
                    mMusicService.seekTo(seekBar.getProgress());
                }
            }
        });
    }

    private void scrollTo(int position) {
        mRecyclerView.smoothScrollToPosition(position);
    }

    private String convertTime(long milliseconds) {
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private void repeatSong() {
        final ImageView imageView = findViewById(R.id.image_repeat);

        // init the image
        if (mPreferences.getRepeatedMode()) {
            imageView.setImageResource(R.drawable.ic_repeat);
        } else {
            imageView.setImageResource(R.drawable.ic_not_repeat);
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMusicService.getPlayer() != null) {
                    if (mPreferences.getRepeatedMode()) {
                        // turn off
                        imageView.setImageResource(R.drawable.ic_not_repeat);
                        mPreferences.setRepeatedMode(false);
                        Toast.makeText(
                                MainActivity.this,
                                R.string.not_repeat_msg,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // turn on
                        imageView.setImageResource(R.drawable.ic_repeat);
                        mPreferences.setRepeatedMode(true);
                        Toast.makeText(
                                MainActivity.this,
                                R.string.repeat_msg,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void showVolumeControl() {
        ImageView imageView = findViewById(R.id.image_volume);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                if (audioManager != null) {
                    audioManager.adjustStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_SAME,
                            AudioManager.FLAG_SHOW_UI);
                }
            }
        });
    }

    // update 'mCurrentSong' with a new model object from the list by an index
    private void updateCurrentSong(int position) {
        Bitmap img = mAdapter.getSong(position).getImage();
        String url = mAdapter.getSong(position).getUrl();
        String title = mAdapter.getSong(position).getTitle();
        String artist = mAdapter.getSong(position).getArtist();
        String album = mAdapter.getSong(position).getAlbum();
        int firstYear = mAdapter.getSong(position).getFirstYear();
        int lastYear = mAdapter.getSong(position).getLastYear();
        long duration = mAdapter.getSong(position).getDuration();
        long size = mAdapter.getSong(position).getSize();
        String mimType = mAdapter.getSong(position).getMimType();

        mCurrentSong = new CurrentSong(
                position,
                img,
                url,
                title,
                artist,
                album,
                firstYear,
                lastYear,
                duration,
                size,
                mimType);
    }

    private void showDetails() {
        ImageView imageView = findViewById(R.id.image_details);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound && mMusicService.getPlayer() != null && mCurrentSong != null) {
                    Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                    intent.putExtra("title", mCurrentSong.getTitle());
                    intent.putExtra("artist", mCurrentSong.getArtist());
                    intent.putExtra("album", mCurrentSong.getAlbum());
                    intent.putExtra("url", mCurrentSong.getUrl());
                    intent.putExtra("first_year", mCurrentSong.getFirstYear());
                    intent.putExtra("last_year", mCurrentSong.getLastYear());
                    intent.putExtra("duration", mCurrentSong.getDuration());
                    intent.putExtra("size", mCurrentSong.getSize());
                    intent.putExtra("mim_type", mCurrentSong.getMimType());
                    Bitmap b = mCurrentSong.getImage();
                    ByteArrayOutputStream bs = new ByteArrayOutputStream();
                    b.compress(Bitmap.CompressFormat.PNG, 50, bs);
                    intent.putExtra("image", bs.toByteArray());
                    startActivityForResult(intent, DELETE_SONG_REQUEST_CODE);
                }
            }
        });
    }

    // pause and resume a current song
    private void controlMusicByHeadphones() {
        MediaSession audioSession = new MediaSession(getApplicationContext(), "MusicTAG");
        audioSession.setCallback(new MediaSession.Callback() {
            @Override
            public boolean onMediaButtonEvent(final Intent mediaButtonIntent) {
                if (Intent.ACTION_MEDIA_BUTTON.equals(mediaButtonIntent.getAction())) {
                    KeyEvent event = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

                    if (event != null) {
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            if (mMusicService.getPlayer() != null) {
                                if (mMusicService.getPlayer().isPlaying()) {
                                    mMusicService.pauseSong();
                                }
                                else {
                                    mMusicService.resumeSong();
                                }
                            }
                        }
                    }
                }

                return true;
            }
        });

        PlaybackState state = new PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY_PAUSE)
                .setState(PlaybackState.STATE_PLAYING, 0, 0, 0)
                .build();
        audioSession.setPlaybackState(state);
        audioSession.setFlags(
                MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        audioSession.setActive(true);
    }

    // Music Service
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            mMusicService = binder.getService();

            isBound = true;

            setUpBridge();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    // this methods are called from Music Service
    private void setUpBridge() {
        mMusicService.setMusicServiceBridge(new MusicService.MusicBridgeServiceActivity() {
            @Override
            public void playSongPreparedPlayer() {
                mSeekBar.setMax(mMusicService.getPlayer().getDuration());
                mDuration.setText(convertTime(mMusicService.getPlayer().getDuration()));
            }

            @Override
            public void playSong() {
                mTitle.setText(mCurrentSong.getTitle());
                mArtist.setText(mCurrentSong.getArtist());
                mPlayStopButton.setImageResource(R.drawable.ic_stop_song);
                mLogo.setImageBitmap(mCurrentSong.getImage());
                mHandler.post(timeSong);

                Intent intent = new Intent(MainActivity.this, MusicForegroundService.class);
                intent.putExtra("title", mCurrentSong.getTitle());
                intent.putExtra("artist", mCurrentSong.getArtist());
                startService(intent);
            }

            @Override
            public void pauseSong() {
                mPlayStopButton.setImageResource(R.drawable.ic_play_song);
            }

            @Override
            public void resumeSong() {
                mPlayStopButton.setImageResource(R.drawable.ic_stop_song);
            }

            @Override
            public void destroySong() {
                mHandler.removeCallbacks(timeSong);
                mSeekBar.setProgress(0);
                stopService(new Intent(MainActivity.this, MusicForegroundService.class));
            }

            @Override
            public CurrentSong nextSong(int position) {
                updateCurrentSong(position);
                scrollTo(position);

                return mCurrentSong;
            }

            @Override
            public CurrentSong previousSong(int position) {
                updateCurrentSong(position);
                scrollTo(position);

                return mCurrentSong;
            }
        });
    }

    // SeekBar of a current song
    Runnable timeSong = new Runnable() {
        @Override
        public void run() {
            if (mMusicService.getPlayer() != null) {
                // set progress of SeekBar
                mSeekBar.setProgress(mMusicService.getPlayer().getCurrentPosition());
                mCurrentTime.setText(convertTime(mMusicService.getPlayer().getCurrentPosition()));

                // if a song ends
                if (mMusicService.getPlayer().getCurrentPosition()
                        >= mMusicService.getPlayer().getDuration()) {
                    // repeat a song
                    if (mPreferences.getRepeatedMode()) {
                        mMusicService.destroySong();
                        mMusicService.playSong(mCurrentSong);
                    } else mMusicService.nextSong(mCurrentSong, mAdapter);
                }

                mHandler.postDelayed(this, 100);
            }
        }
    };

}
