package com.samsung.priorityplayer;

import android.media.AudioAttributes;
import android.media.MediaPlayer;

import java.io.IOException;
import java.util.List;
import java.util.Stack;

public class Player {
    private static MediaPlayer mediaPlayer;
    private static List<Song> songList;
    private static int currentIndex;
    private final Stack<Integer> stack = new Stack<>();

    public void setSongs(List<Song> songs) {
        songList = songs;
    }

    public void play(int index) {
        if (songList == null || songList.isEmpty() || index < 0 || index >= songList.size()) return;
        currentIndex = index;
        playAudio(songList.get(currentIndex));
    }

    private void playAudio(Song song) {
        stop();
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());
            mediaPlayer.setDataSource(song.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    public int getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    public int getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }

    public static boolean isPlayingStatic() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public Song getSong() {
        return songList != null && currentIndex >= 0 && currentIndex < songList.size()
                ? songList.get(currentIndex)
                : null;
    }

    public void playNext() {
        if (songList != null && !songList.isEmpty()) {
            stack.push(currentIndex);
            if (!MusicDataHolder.isIsRandom()) currentIndex = (currentIndex + 1) % songList.size();
            else {
                if (getSong().isChangeable()) MusicDataHolder.EditPriority(currentIndex,(int) (((double) getCurrentPosition() / getDuration() * 100 + getSong().getPriority()) / 2));
                currentIndex = RandomSong.getRandomElementNumber();
            }
            playAudio(songList.get(currentIndex));
        }
    }

    public void playPrevious() {
        if (songList != null && !songList.isEmpty()) {
            if (!MusicDataHolder.isIsRandom()) currentIndex = (currentIndex - 1 + songList.size()) % songList.size();
            else{
                if (!stack.isEmpty()) currentIndex = stack.pop();
                else return;
            }
            playAudio(songList.get(currentIndex));
        }
    }

    public void release() {
        stop();
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public int getCurrentIndex() {
        return currentIndex;
    }
}
