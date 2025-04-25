package com.samsung.playerindividual;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MusicDataHolder {
    private static ArrayList<Song> songList = new ArrayList<>();
    private static int currentIndex;
    private static int totalTemperature;
    private static boolean isRandom;

    public static boolean isIsRandom() {
        return isRandom;
    }

    public static void setIsRandom(boolean isRandom) {
        MusicDataHolder.isRandom = isRandom;
    }

    private static int getStartTemperature() {
        int totalTemp = 0;
        for (Song song: songList){
            totalTemp += song.getTemperature();
        }
        Log.i("MyTag","temp = " + totalTemp);
        return totalTemp;
    }
    public static void setSongs(List<Song> songs) {
        songList = (ArrayList<Song>) songs;
        totalTemperature = getStartTemperature();
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
            song.setUpper_border(UpperBorden + song.getTemperature());
            LowerBorden += song.getTemperature();
            UpperBorden += song.getTemperature();
        }
    }
    public static void EditTemperature(int number, int newTemperature){
        try {
            songList.get(number).setUpper_border(songList.get(number).getLower_border() + newTemperature);
            totalTemperature -= songList.get(number).getTemperature();
            songList.get(number).setTemperature(newTemperature);
            totalTemperature += songList.get(number).getTemperature();
            for (int i = number + 1; i < songList.size();i++){
                songList.get(i).setLower_border(songList.get(i-1).getUpper_border());
                songList.get(i).setUpper_border(songList.get(i).getLower_border() + songList.get(i).getTemperature());
            }
        } catch (IndexOutOfBoundsException e){
        }
    }
    public static int getTotalTemperature(){
        return totalTemperature;
    }

}
