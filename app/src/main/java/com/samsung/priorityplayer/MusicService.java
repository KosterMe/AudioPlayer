package com.samsung.priorityplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.List;

public class MusicService extends Service {
    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_PREV = "ACTION_PREV";
    public static final String ACTION_SEEK = "ACTION_SEEK";
    public static final String ACTION_INIT = "ACTION_INIT";
    public static final String ACTION_UPDATE_UI = "ACTION_UPDATE_UI";
    public static final String ACTION_SONG_CHANGED = "ACTION_SONG_CHANGED";

    private static final String CHANNEL_ID = "MusicPlaybackChannel";

    private MediaSessionCompat mediaSession;
    private Player player;
    private List<Song> songList;
    private int currentIndex;
    private NotificationManager notificationManager;

    private AudioManager audioManager;
    private AudioManager.OnAudioFocusChangeListener afChangeListener;

    private BroadcastReceiver noisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                player.pause();
                updatePlaybackState(PlaybackStateCompat.STATE_PAUSED);
                updateNotification();
                unregisterNoisyReceiver();
            }
        }
    };

    private void registerNoisyReceiver() {
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(noisyReceiver, filter);
    }

    private void unregisterNoisyReceiver() {
        try {
            unregisterReceiver(noisyReceiver);
        } catch (IllegalArgumentException e) {
            Log.w("MusicService", "NoisyReceiver not registered.");
        }
    }

    private final Handler uiHandler = new Handler();
    private final Runnable updateUiRunnable = new Runnable() {
        @Override
        public void run() {
            if (player != null && player.isPlaying()) {
                int position = player.getCurrentPosition();
                int duration = player.getSong().getDuration();
                if (position + 1500 >= duration) {
                    Log.i("MyTag","skippingDelay");
                    player.playNext();
                    updatePlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT);
                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
                    MusicDataHolder.setCurrentIndex(player.getCurrentIndex());
                    updateMetadata(player.getSong());
                    sendBroadcast(new Intent(ACTION_SONG_CHANGED).setPackage(getPackageName()));
                    uiHandler.post(updateUiRunnable);
                    updateNotification();
                }
                Intent intent = new Intent(ACTION_UPDATE_UI);
                intent.putExtra("position", position);
                intent.putExtra("duration", duration);
                intent.setPackage(getPackageName());
                sendBroadcast(intent);


                updateNotification(); // обновляем прогресс в уведомлении

                uiHandler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mediaSession = new MediaSessionCompat(this, "MusicService");
        player = new Player();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_LOSS:
                        // Долгосрочная потеря фокуса: ставим на паузу
                        player.pause();
                        updatePlaybackState(PlaybackStateCompat.STATE_PAUSED);
                        updateNotification();
                        unregisterNoisyReceiver();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        // Кратковременная потеря (например звонок): ставим на паузу
                        player.pause();
                        updatePlaybackState(PlaybackStateCompat.STATE_PAUSED);
                        updateNotification();
                        unregisterNoisyReceiver();
                        break;
                }
            }
        };
        initMediaSession();
    }
    private boolean requestAudioFocus() {
        int result = audioManager.requestAudioFocus(
                afChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
        );
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) return START_NOT_STICKY;

        switch (intent.getAction()) {
            case ACTION_INIT:
                if (requestAudioFocus()) {
                    songList = MusicDataHolder.getSongs();
                    currentIndex = MusicDataHolder.getCurrentIndex();
                    player.setSongs(songList);
                    player.play(currentIndex);
                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
                    updateMetadata(player.getSong());
                    sendBroadcast(new Intent(ACTION_SONG_CHANGED).setPackage(getPackageName()));
                    updateNotification();
                    uiHandler.post(updateUiRunnable);
                    registerNoisyReceiver();
                }
                break;


            case ACTION_PLAY:
                if (requestAudioFocus()){
                    player.resume();
                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
                    MusicDataHolder.setCurrentIndex(player.getCurrentIndex());
                    updateNotification();
                    uiHandler.post(updateUiRunnable);
                    sendBroadcast(new Intent(ACTION_SONG_CHANGED).setPackage(getPackageName()));
                    registerNoisyReceiver();
                    registerNoisyReceiver();

                }
                break;

            case ACTION_PAUSE:
                player.pause();
                updatePlaybackState(PlaybackStateCompat.STATE_PAUSED);
                MusicDataHolder.setCurrentIndex(player.getCurrentIndex());
                updateNotification();
                uiHandler.post(updateUiRunnable);
                sendBroadcast(new Intent(ACTION_SONG_CHANGED).setPackage(getPackageName()));
                unregisterNoisyReceiver();
                break;

            case ACTION_NEXT:
                player.playNext();
                updatePlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT);
                updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
                MusicDataHolder.setCurrentIndex(player.getCurrentIndex());
                updateMetadata(player.getSong());
                sendBroadcast(new Intent(ACTION_SONG_CHANGED).setPackage(getPackageName()));
                uiHandler.post(updateUiRunnable);
                updateNotification();
                unregisterNoisyReceiver();
                registerNoisyReceiver();
                break;

            case ACTION_PREV:
                player.playPrevious();
                updatePlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS);
                updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
                MusicDataHolder.setCurrentIndex(player.getCurrentIndex());
                updateMetadata(player.getSong());
                sendBroadcast(new Intent(ACTION_SONG_CHANGED).setPackage(getPackageName()));
                uiHandler.post(updateUiRunnable);
                updateNotification();
                unregisterNoisyReceiver();
                registerNoisyReceiver();
                break;

            case ACTION_SEEK:
                int pos = intent.getIntExtra("seek_to", 0);
                player.seekTo(pos);
                updatePlaybackState(player.isPlaying()
                        ? PlaybackStateCompat.STATE_PLAYING
                        : PlaybackStateCompat.STATE_PAUSED);
        }

        return START_STICKY;
    }

    private void initMediaSession() {
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                if (requestAudioFocus()){
                    player.resume();
                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
                    MusicDataHolder.setCurrentIndex(player.getCurrentIndex());
                    updateNotification();
                    uiHandler.post(updateUiRunnable);
                    sendBroadcast(new Intent(ACTION_SONG_CHANGED).setPackage(getPackageName()));
                    registerNoisyReceiver();
                }
            }

            @Override
            public void onPause() {
                player.pause();
                updatePlaybackState(PlaybackStateCompat.STATE_PAUSED);
                MusicDataHolder.setCurrentIndex(player.getCurrentIndex());
                updateNotification();
                uiHandler.post(updateUiRunnable);
                sendBroadcast(new Intent(ACTION_SONG_CHANGED).setPackage(getPackageName()));
                unregisterNoisyReceiver();
            }

            @Override
            public void onSkipToNext() {
                player.playNext();
                updatePlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT);
                updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
                MusicDataHolder.setCurrentIndex(player.getCurrentIndex());
                updateMetadata(player.getSong());
                sendBroadcast(new Intent(ACTION_SONG_CHANGED).setPackage(getPackageName()));
                uiHandler.post(updateUiRunnable);
                updateNotification();
                unregisterNoisyReceiver();
                registerNoisyReceiver();
            }

            @Override
            public void onSkipToPrevious() {
                player.playPrevious();
                updatePlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS);
                updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
                MusicDataHolder.setCurrentIndex(player.getCurrentIndex());
                updateMetadata(player.getSong());
                sendBroadcast(new Intent(ACTION_SONG_CHANGED).setPackage(getPackageName()));
                uiHandler.post(updateUiRunnable);
                updateNotification();
                unregisterNoisyReceiver();
                registerNoisyReceiver();
            }

            @Override
            public void onSeekTo(long pos) {
                player.seekTo((int) pos);
                if (pos + 1000 >= player.getDuration()) onSkipToNext();
                updatePlaybackState(player.isPlaying()
                        ? PlaybackStateCompat.STATE_PLAYING
                        : PlaybackStateCompat.STATE_PAUSED);
            }

        });
        mediaSession.setActive(true);
    }

    private void updateMetadata(Song song) {
        MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.getName())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.getArtist())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.getDuration())
                .build();

        mediaSession.setMetadata(metadata);
    }

    private void updatePlaybackState(int state) {
        PlaybackStateCompat playbackState = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SEEK_TO
                )
                .setState(state, player.getCurrentPosition(), 1.0f)
                .build();
        mediaSession.setPlaybackState(playbackState);
    }

    private void updateNotification() {
        createNotificationChannel();

        Song currentSong = player.getSong();
        int currentPosition = player.getCurrentPosition();
        int duration = currentSong.getDuration();

        Intent playIntent = new Intent(this, MusicService.class);
        playIntent.setAction(ACTION_PLAY);
        PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent pauseIntent = new Intent(this, MusicService.class);
        pauseIntent.setAction(ACTION_PAUSE);
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent nextIntent = new Intent(this, MusicService.class);
        nextIntent.setAction(ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getService(this, 2, nextIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent prevIntent = new Intent(this, MusicService.class);
        prevIntent.setAction(ACTION_PREV);
        PendingIntent prevPendingIntent = PendingIntent.getService(this, 3, prevIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(currentSong.getName())
                .setContentText(currentSong.getArtist())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(currentSong.getAlbumBitmap() != null ? currentSong.getAlbumBitmap() : null)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .addAction(R.drawable.baseline_skip_previous_24, "Prev", prevPendingIntent)
                .addAction(
                        Player.isPlayingStatic() ? R.drawable.baseline_stop_24 : R.drawable.baseline_play_arrow_24,
                        Player.isPlayingStatic() ? "Pause" : "Play",
                        Player.isPlayingStatic() ? pausePendingIntent : playPendingIntent)
                .addAction(R.drawable.baseline_skip_next_24, "Next", nextPendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))
                .setOngoing(Player.isPlayingStatic())
                .setProgress(duration, currentPosition, false);

        Notification notification = builder.build();
        if (Player.isPlayingStatic()) {
            startForeground(1, notification);
        } else {
            notificationManager.notify(1, notification);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Playback Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        audioManager.abandonAudioFocus(afChangeListener);
        unregisterNoisyReceiver();
        player.release();
        mediaSession.release();
    }

}
