package com.samsung.priorityplayer;

import android.Manifest;
import android.content.Intent;
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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    public static final int DATA_VERSION = 49;
    private static final int REQUEST_MEDIA_PERMISSION = 100;

    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private SongDatabaseHelper dbHelper;
    private boolean navigationInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        requestPermission();
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_MEDIA_AUDIO,
            }, REQUEST_MEDIA_PERMISSION);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
            }, REQUEST_MEDIA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_MEDIA_PERMISSION) {
            boolean granted = grantResults.length > 0;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }

            if (granted) {
                initDataAsync();
            } else {
                Toast.makeText(this, "App needs file access to work.", Toast.LENGTH_SHORT).show();
                Log.e("Permissions", "Not all required permissions were granted");
                finish();
            }
        }
    }

    private void initDataAsync() {
        dbHelper = new SongDatabaseHelper(this, DATA_VERSION);
        MusicDataHolder.init(this);

        ioExecutor.execute(() -> {
            scanAndSaveAudioFiles(dbHelper);
            ArrayList<Song> songs = dbHelper.getAllSongs();
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                MusicDataHolder.setSongs(songs);
                initNavigation(songs);
            });
        });
    }

    private void initNavigation(ArrayList<Song> songs) {
        if (navigationInitialized) {
            return;
        }

        ImageButton btn1 = findViewById(R.id.main_btn_list);
        ImageButton btn2 = findViewById(R.id.main_btn_rand);
        ImageButton btn3 = findViewById(R.id.main_btn_playlist);
        List<ImageButton> buttons = Arrays.asList(btn1, btn2, btn3);

        btn2.setSelected(true);
        openFragment(new PlayerRandom(songs));

        View.OnClickListener listener = clickedButton -> {
            for (ImageButton button : buttons) {
                button.setSelected(button == clickedButton);
            }
            if (clickedButton.equals(btn1)) {
                ListSongs ls = new ListSongs(this, dbHelper.getAllSongs());
                MusicDataHolder.setPlName("");
                openFragment(ls);
            } else if (clickedButton.equals(btn2)) {
                PlayerRandom rs = new PlayerRandom(dbHelper.getAllSongs());
                MusicDataHolder.setPlName("");
                openFragment(rs);
            } else if (clickedButton.equals(btn3)) {
                PlayLists p = new PlayLists(this, dbHelper.getAllPLName());
                MusicDataHolder.setPlName("");
                openFragment(p);
            }
        };

        btn1.setOnClickListener(listener);
        btn2.setOnClickListener(listener);
        btn3.setOnClickListener(listener);
        navigationInitialized = true;
    }

    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.place_holder, fragment);
        transaction.commit();
    }

    private void scanAndSaveAudioFiles(SongDatabaseHelper dbHelper) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " +
                MediaStore.Audio.Media.DURATION + " >= 3000";

        String[] projection = {
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATA
        };

        try (Cursor cursor = getContentResolver().query(uri, projection, selection, null, MediaStore.Audio.Media.DATE_ADDED + " DESC")) {
            if (cursor == null) {
                return;
            }
            long currentTime = System.currentTimeMillis();
            while (cursor.moveToNext()) {
                String defaultName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                if (path == null || !path.endsWith(".mp3")) {
                    continue;
                }

                if (dbHelper.isUserDeleted(path)) {
                    continue;
                }

                if (!dbHelper.songExists(path)) {
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
                        art = artist != null ? artist : "Unknown artist";
                        alb = album != null ? album : "Unknown album";
                        if (durationStr != null) {
                            try {
                                duration = Integer.parseInt(durationStr);
                            } catch (NumberFormatException ignored) {
                                duration = 0;
                            }
                        }

                        byte[] albumArt = retriever.getEmbeddedPicture();
                        if (albumArt != null && albumArt.length < 1_000_000) {
                            albumBitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
                        }
                    } catch (Exception e) {
                        Log.w("scan", "Metadata read failed for " + path, e);
                    }

                    Song song = new Song(name.replace(".mp3", ""), path, art, alb, duration, albumBitmap, 50, true);
                    dbHelper.insertSong(song);
                    dbHelper.updateSong(path, currentTime);
                } else {
                    dbHelper.updateSong(path, currentTime);
                }
            }
            dbHelper.clearDB(currentTime);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ioExecutor.shutdownNow();

        MusicDataHolder.release();
        stopService(new Intent(this, MusicService.class));

        Log.d("MainActivity", "onDestroy completed, resources released");
    }
}
