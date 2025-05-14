package com.samsung.audioplayer;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int DATA_VERSION = 37;
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }


        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);

        SongDatabaseHelper dbHelper = new SongDatabaseHelper(this,DATA_VERSION);

        MusicDataHolder.init(this);

        ArrayList<Song> songs = scanAndSaveAudioFiles(dbHelper);
        MusicDataHolder.setSongs(songs);


        PlayerRandom pr = new PlayerRandom(dbHelper.getAllSongs());
        openFragment(pr);

        ImageButton btn1 = findViewById(R.id.main_btn_list);
        ImageButton btn2 = findViewById(R.id.main_btn_rand);
        ImageButton btn3 = findViewById(R.id.main_btn_playlist);
        btn2.setSelected(true);
        List<ImageButton> buttons = Arrays.asList(btn1, btn2, btn3);


        View.OnClickListener listener = clickedButton -> {
            for (ImageButton button : buttons) {
                button.setSelected(button == clickedButton);
            }
            if (clickedButton.equals(btn1)) {
                ListSongs ls = new ListSongs(this,dbHelper.getAllSongs());
                MusicDataHolder.setPlName("");
                openFragment(ls);
            } else if (clickedButton.equals(btn2)) {
                PlayerRandom rs = new PlayerRandom(dbHelper.getAllSongs());
                MusicDataHolder.setPlName("");
                openFragment(rs);
            } else if (clickedButton.equals(btn3)) {
                PlayLists p = new PlayLists(this,dbHelper.getAllPLName());
                MusicDataHolder.setPlName("");
                openFragment(p);
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

    private ArrayList<Song> scanAndSaveAudioFiles(SongDatabaseHelper dbHelper) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " +
                MediaStore.Audio.Media.DURATION + " >= 3000";

        String[] projection = {
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATA
        };

        ArrayList<Song> songs = new ArrayList<>();
        Cursor cursor = getContentResolver().query(uri, projection, selection, null, MediaStore.Audio.Media.DATE_ADDED + " DESC");

        if (cursor != null) {
            int currentTime = (int) System.currentTimeMillis();
            while (cursor.moveToNext()) {
                String defaultName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                Log.i("Scan","1 " + songs.size());

                    if (path.endsWith(".mp3")) {
                        String name = "";
                        String art = "";
                        String alb = "";
                        int duration = 0;
                        Bitmap albumBitmap = null;
                        try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
                            retriever.setDataSource(path);
                            String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                            String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                            String album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

                            name = title != null ? title : defaultName;
                            art = artist != null ? artist : "Неизвестный исполнитель";
                            alb = album != null ? album : "Неизвестный альбом";
                            duration = durationStr != null ? Integer.parseInt(durationStr) : 0;

                            byte[] albumArt = retriever.getEmbeddedPicture();
                            if (albumArt != null) {
                                albumBitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
                            } else {
                                albumBitmap = null;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Log.i("Scan", "Try add " + songs.size());
                        Song song = new Song(name.replaceAll("\\.mp3", ""), path, art, alb, duration, albumBitmap, 50, true);
                        dbHelper.insertSong(song);
                        dbHelper.updateSong(path, currentTime);
                        Log.i("Scan", "add " + songs.size());
                        songs.add(song);
                }
            }
            cursor.close();
            dbHelper.clearDB(currentTime);
        }
        Log.i("Scan","" + songs.size());
        return songs;
    }


}
