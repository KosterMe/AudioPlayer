package com.samsung.audioplayer;

import android.graphics.Bitmap;
import java.io.Serializable;

public class Song implements Serializable {
    private String name;
    private final String path;
    private String artist;
    private String album;
    private Bitmap albumBitmap;
    private int duration;
    private int priority = 50;
    private int lower_border;
    private int upper_border;
    private boolean changeable = true;

    public Song(String name, String path, String artist, String album, int duration, Bitmap albumBitmap, int priority, boolean changeable) {
        this.path = path;
        this.name = name;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.albumBitmap = albumBitmap;
        this.priority = priority;
        this.changeable = changeable;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChangeable() {
        return changeable;
    }

    public void setChangeable(boolean changeable) {
        this.changeable = changeable;
    }

    public int getDuration() {
        return duration;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public Bitmap getAlbumBitmap() {
        return albumBitmap;
    }

    public void setAlbumBitmap(Bitmap albumBitmap) {
        this.albumBitmap = albumBitmap;
    }

    public int getUpper_border() {
        return upper_border;
    }

    public void setUpper_border(int upper_border) {
        this.upper_border = upper_border;
    }

    public int getLower_border() {
        return lower_border;
    }

    public void setLower_border(int lower_border) {
        this.lower_border = lower_border;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
