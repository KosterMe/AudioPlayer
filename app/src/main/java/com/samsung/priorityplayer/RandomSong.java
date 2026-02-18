package com.samsung.priorityplayer;

import android.util.Log;

import java.util.List;

public class RandomSong {
    public static int getRandomElementNumber() {
        List<Song> songs = MusicDataHolder.getSongs();
        if (songs == null || songs.isEmpty()) {
            return -1;
        }
        int totalSum = MusicDataHolder.getTotalPriority();
        if (totalSum <= 0) {
            return (int) (Math.random() * songs.size());
        }
        int RandomElement = (int) (Math.random() * totalSum);
        Log.i("MyTag","total: " + totalSum);
        try {
            return binary_search(RandomElement);
        } catch (ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }

    public static int binary_search(int key) {
        List<Song> Songs = MusicDataHolder.getSongs();
        Log.i("MyTag","size: " + Songs.size());
        int right = Songs.size() - 1;
        int left = 0;
        while (right >= left) {
            int middle = (right + left) / 2;
            if (Songs.get(middle).getLower_border() > key) {
                right = middle - 1;
                continue;
            }
            if (Songs.get(middle).getUpper_border() <= key) {
                left = middle + 1;
                continue;
            }
            Log.i("MyTag","mid: " + middle);
            return middle;
        }
        return 0;
    }
}
