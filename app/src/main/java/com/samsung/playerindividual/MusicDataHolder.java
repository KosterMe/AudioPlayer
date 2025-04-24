package com.samsung.playerindividual;

import java.util.List;

public class MusicDataHolder {
    private static List<Song> songList;
    private static int currentIndex;

    public static void setSongs(List<Song> songs) {
        songList = songs;
    }

    public static List<Song> getSongs() {
        return songList;
    }

    public static void setCurrentIndex(int index) {
        currentIndex = index;
    }

    public static int getCurrentIndex() {
        return currentIndex;
    }
}
