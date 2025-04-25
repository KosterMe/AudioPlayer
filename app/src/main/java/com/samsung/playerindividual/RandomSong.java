package com.samsung.playerindividual;

import android.util.Log;

import java.util.List;

public class RandomSong {
    public static int getRandomElementNumber() {
        int totalSum = MusicDataHolder.getTotalTemperature();
        int RandomElement = (int) ((Math.random() * totalSum) + 1);
        Log.i("MyTag","total: " + totalSum);
        try {
            return (binary_search(RandomElement));
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
            if (Songs.get(middle).getUpper_border() < key) {
                left = middle + 1;
                continue;
            }
            Log.i("MyTag","mid: " + middle);
            return middle;
        }
        return 0;
    }
}
