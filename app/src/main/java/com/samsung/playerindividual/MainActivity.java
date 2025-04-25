package com.samsung.playerindividual;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 1;
    SongPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, 100);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "music_channel",
                    "Music Playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        ArrayList<Song> Songs = getAudioFiles();
        MusicDataHolder.setSongs(Songs);
        ListSongs ls = new ListSongs( this);
        openFragment(ls);

        ImageButton btn1 = findViewById(R.id.main_btn_list);
        ImageButton btn2 = findViewById(R.id.main_btn_rand);
        ImageButton btn3 = findViewById(R.id.main_btn_liked);
        btn2.setSelected(true);
        List<ImageButton> buttons = Arrays.asList(btn1, btn2, btn3);

        View.OnClickListener listener = clickedButton -> {
            for (ImageButton button : buttons) {
                button.setSelected(button == clickedButton);
            }
            if (clickedButton.equals(btn1)) {
                ListSongs ls1 = new ListSongs(this);
                openFragment(ls1);
            } else if (clickedButton.equals(btn2)) {
                PlayerRandom rs = new PlayerRandom();
                openFragment(rs);
            } else if (clickedButton.equals(btn3)) {
            }
        };

        btn1.setOnClickListener(listener);
        btn2.setOnClickListener(listener);
        btn3.setOnClickListener(listener);


    }

    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.place_holder, fragment); // Меняем текущий фрагмент
        transaction.commit();
    }

    private ArrayList<Song> getAudioFiles() {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " +
                MediaStore.Audio.Media.DURATION + " >= 3000";  // 3 сек

        String[] projection = {MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA};
        ArrayList<Song> arr = new ArrayList<Song>();
        Cursor cursor = getContentResolver().query(uri, projection, selection, null, MediaStore.Audio.Media.DATE_ADDED + " DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                if (path.endsWith(".mp3") || path.endsWith(".m4a")) {
                    arr.add(new Song(name.substring(0, name.length() - 4), path));
                }
            }
            cursor.close();
        }
        return arr;
    }
}