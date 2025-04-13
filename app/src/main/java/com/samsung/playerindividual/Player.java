package com.samsung.playerindividual;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.View;

import java.util.List;

public class Player {

    private MediaPlayer mediaPlayer;
    private int current;
    private Song song;
    private List<Song> Songs;

    public Player(int current, List<Song> songs) {
        this.current = current;
        this.song = songs.get(current);
        Songs = songs;
    }

    public void playAudio(Song song) {
        stopAudio();
        mediaPlayer = new MediaPlayer();
        String path = song.getPath();
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.start();
//            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mediaPlayer) {
//                    nextAudio();
//                }
//            });
        } catch (Exception e) {
        }
    }

    public void resumeAudio() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.start();
//                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                    @Override
//                    public void onCompletion(MediaPlayer mediaPlayer) {
//                        nextAudio();
//                    }
//                });
            }
        } catch (Exception e) {
        }
    }

    public void nextAudio() {
        if (current < Songs.size() - 1) {
            current++;
            song = Songs.get(current);
            playAudio(song);


        } else {
            song = Songs.get(0);
            current = 0;
            playAudio(song);

        }
    }

    public void lastAudio() {
        if (current != 0) {
            current--;
            song = Songs.get(current);
            playAudio(song);

        } else {
            song = Songs.get(Songs.size() - 1);
            current = Songs.size() - 1;
            playAudio(song);
        }
    }

    public void stopAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void pauseAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public int getTotalTime() {
        return mediaPlayer.getDuration();
    }

    public void SeekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    public boolean IsPlaying() {
        return mediaPlayer.isPlaying();
    }

    public int GetCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public Song getSong() {
        return song;
    }

    public void DestroyMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}

