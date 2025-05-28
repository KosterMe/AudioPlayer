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
            totalPri += song.getPriority();
        }
        Log.i("MyTag","priority = " + totalPri);
        return totalPri;
    }
    public static void setSongs(List<Song> songs) {
        songList = (ArrayList<Song>) songs;
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
            song.setUpper_border(UpperBorden + song.getPriority());
            LowerBorden += song.getPriority();
            UpperBorden += song.getPriority();
        }
    }
    public static void EditPriority(int index, int newPriority) {
        try {
            Song song = songList.get(index);
            Log.i("EditP", "before: " + song.getPriority() + " " + song.getLower_border() + " " + song.getUpper_border());

            totalPriority -= song.getPriority();
            song.setPriority(newPriority);
            totalPriority += newPriority;

            song.setUpper_border(song.getLower_border() + newPriority);

            for (int i = index + 1; i < songList.size(); i++) {
                Song nextSong = songList.get(i);
                nextSong.setLower_border(songList.get(i - 1).getUpper_border());
                nextSong.setUpper_border(nextSong.getLower_border() + nextSong.getPriority());
            }

            if (dbHelper != null) {
                dbHelper.updatePriority(song.getPath(), newPriority);
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
        if (songList != null) {
            songList.clear();
            songList = null;
        }
        plName = null;
    }

}
