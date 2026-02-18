package com.samsung.priorityplayer;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MusicDataHolder {
    private static ArrayList<Song> songList = new ArrayList<>();
    private static int currentIndex;
    private static int totalPriority;
    private static boolean isRandom;
    private static SongDatabaseHelper dbHelper;
    private static String plName;

    public static void init(Context context) {
        if (dbHelper == null) {
            dbHelper = new SongDatabaseHelper(context.getApplicationContext(),MainActivity.DATA_VERSION);
        }
    }
    public static boolean isIsRandom() {
        return isRandom;
    }

    public static void setIsRandom(boolean isRandom) {
        MusicDataHolder.isRandom = isRandom;
    }

    public static String getPlName() {
        return plName;
    }

    public static void setPlName(String plName) {
        MusicDataHolder.plName = plName;
    }

    private static int getStartPriority() {
        int totalPri = 0;
        for (Song song: songList){
            totalPri += Math.max(0, song.getPriority());
        }
        Log.i("MyTag","priority = " + totalPri);
        return totalPri;
    }
    public static void setSongs(List<Song> songs) {
        if (songs == null) {
            songList = new ArrayList<>();
        } else {
            songList = new ArrayList<>(songs);
        }
        totalPriority = getStartPriority();
        getStartBorder();
        Log.i("MyTag","totalsize = " + songList.size());
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
    public static void getStartBorder(){
        int LowerBorden = 0;
        int UpperBorden = 0;
        for(Song song: songList){
            song.setLower_border(LowerBorden);
            int priority = Math.max(0, song.getPriority());
            song.setUpper_border(UpperBorden + priority);
            LowerBorden += priority;
            UpperBorden += priority;
        }
    }
    public static void EditPriority(int index, int newPriority) {
        try {
            Song song = songList.get(index);
            Log.i("EditP", "before: " + song.getPriority() + " " + song.getLower_border() + " " + song.getUpper_border());

            int safePriority = Math.max(0, newPriority);
            totalPriority -= Math.max(0, song.getPriority());
            song.setPriority(safePriority);
            totalPriority += safePriority;

            song.setUpper_border(song.getLower_border() + safePriority);

            for (int i = index + 1; i < songList.size(); i++) {
                Song nextSong = songList.get(i);
                nextSong.setLower_border(songList.get(i - 1).getUpper_border());
                nextSong.setUpper_border(nextSong.getLower_border() + Math.max(0, nextSong.getPriority()));
            }

            if (dbHelper != null) {
                dbHelper.updatePriority(song.getPath(), safePriority);
            } else {
                Log.e("EditP", "dbHelper is not initialized");
            }

            Log.i("EditP", "after: " + song.getPriority() + " " + song.getLower_border() + " " + song.getUpper_border() + " " + song.isChangeable());
            Log.i("EditP", "" + getTotalPriority());
        } catch (IndexOutOfBoundsException e) {
            Log.e("EditP", "Index out of bounds: " + index, e);
        }
    }
    public static int getTotalPriority(){
        return totalPriority;
    }
    public static void release() {
        songList.clear();
        totalPriority = 0;
        currentIndex = 0;
        isRandom = false;
        plName = null;
    }

}
