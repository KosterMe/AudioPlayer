package com.samsung.playerindividual;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

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
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        ListView ls = findViewById(R.id.list_songs);
        ArrayList<Song> Songs = getAudioFiles();
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, getSongNames(Songs));
//        ListAdapter adapter = new ListAdapter(this,Songs);
        ls.setAdapter(adapter);
        ls.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SongPlayer mp = new SongPlayer(Songs,i);
                openFragment(mp);
            }
        });
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
        String[] projection = {MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA};
        ArrayList<Song> arr = new ArrayList<Song>();
        Cursor cursor = getContentResolver().query(uri, projection, null, null, MediaStore.Audio.Media.DATE_ADDED + " DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                String extension = name.substring(name.length()-3);
                if (extension.equals("mp3"))
                    arr.add(new Song(name.substring(0,name.length() - 4),path));
            }
            cursor.close();
        }
        return arr;
    }
    private ArrayList<String> getSongNames(ArrayList<Song> Songs) {
        ArrayList<String> names = new ArrayList<>();
        for (Song song : Songs) {
            names.add(song.getName());
        }
        return names;
    }


}