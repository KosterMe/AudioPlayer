package com.samsung.priorityplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

public class SongPlayer extends Fragment {
    private Song song;
    private List<Song> Songs;
    private SeekBar seekBar;
    private TextView tvCurrentTime;
    private int current;
    View view;
    private boolean isUserSeeking = false;

    private final BroadcastReceiver musicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;

            switch (intent.getAction()) {
                case MusicService.ACTION_UPDATE_UI:
                    int duration = intent.getIntExtra("duration", 0);
                    int position = intent.getIntExtra("position", 0);
                    updateSeekBar(position, duration);
                    break;
                case MusicService.ACTION_SONG_CHANGED:
                    current = MusicDataHolder.getCurrentIndex();
                    song = Songs.get(current);
                    Log.i("MyTag","changer");
                    updateSongInfo();
                    Log.i("MyTag","updated: " + song.getName());
                    break;
                default:
                    Log.i("MyTag","nothing");
            }
        }
    };

    public SongPlayer(List<Song> Songs, int current) {
        this.Songs = Songs;
        this.current = current;
        this.song = Songs.get(current);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_song_player, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.view = view;

        startMusicService();

        updateSongInfo();

        ImageButton next_ib = view.findViewById(R.id.fragment_next);
        next_ib.setOnClickListener(v -> sendServiceAction(MusicService.ACTION_NEXT));

        ImageButton last_ib = view.findViewById(R.id.fragment_last);
        last_ib.setOnClickListener(v -> sendServiceAction(MusicService.ACTION_PREV));

        seekBar = view.findViewById(R.id.fragment_seek_bar);
        tvCurrentTime = view.findViewById(R.id.current_time);
        TextView tvTotalTime = view.findViewById(R.id.total_time);
        tvTotalTime.setText(formatTime(song.getDuration()));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserSeeking = false;
                Intent intent = new Intent(requireContext(), MusicService.class);
                intent.setAction(MusicService.ACTION_SEEK);
                intent.putExtra("seek_to", seekBar.getProgress());
                if (seekBar.getProgress() + 1000 >= song.getDuration()){
                    intent.setAction(MusicService.ACTION_NEXT);
                }
                requireContext().startService(intent);
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicService.ACTION_UPDATE_UI);
        filter.addAction(MusicService.ACTION_SONG_CHANGED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(musicReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            Log.i("MyTag","reg");
        } else {
            requireContext().registerReceiver(musicReceiver, filter);
        }

    }

    private void startMusicService() {
        MusicDataHolder.setSongs(Songs);
        MusicDataHolder.setCurrentIndex(current);
        Intent intent = new Intent(requireContext(), MusicService.class);
        intent.setAction(MusicService.ACTION_INIT);
        requireContext().startService(intent);
    }

    public void updatePlayPauseButton(ImageButton play_stop_ib){
        if (Player.isPlayingStatic()){
            play_stop_ib.setImageResource(R.drawable.baseline_stop_24);
        }
        else{
            play_stop_ib.setImageResource(R.drawable.baseline_play_arrow_24);
        }
    }

    private void sendServiceAction(String action) {
        Intent intent = new Intent(requireContext(), MusicService.class);
        intent.setAction(action);

        // Если пользователь изменяет SeekBar, передаем новую позицию
        if (action.equals(MusicService.ACTION_SEEK)) {
            intent.putExtra("seek_to", seekBar.getProgress());
        }

        requireContext().startService(intent);
    }

    private void updateSeekBar(int position, int duration) {
        if (!isUserSeeking && seekBar != null) {
            seekBar.setMax(duration);
            seekBar.setProgress(position);
            tvCurrentTime.setText(formatTime(position));
            TextView tvTotalTime = view.findViewById(R.id.total_time);
            tvTotalTime.setText(formatTime(duration));
        }
    }

    private void updateSongInfo() {
        if (view == null || song == null) return;


        TextView tw = view.findViewById(R.id.fragment_song_name);
        tw.setText(song.getName());

        TextView artist = view.findViewById(R.id.fragment_song_artist);
        artist.setText(song.getArtist());

        ImageView iw = view.findViewById(R.id.fragment_image);
        if (song.getAlbumBitmap() != null) {
            iw.setImageBitmap(song.getAlbumBitmap());
        } else {
            iw.setImageResource(R.drawable.default_image_for_item);
        }
        TextView tvTotalTime = view.findViewById(R.id.total_time);
        tvTotalTime.setText(formatTime(song.getDuration()));

        ImageButton play_stop_ib = view.findViewById(R.id.fragment_play_stop);
        updatePlayPauseButton(play_stop_ib);
        play_stop_ib.setOnClickListener(view -> {
            if (Player.isPlayingStatic()) {
                sendServiceAction(MusicService.ACTION_PAUSE);
                Log.i("Update_src","Updated1");
            }
            else {
                sendServiceAction(MusicService.ACTION_PLAY);
                Log.i("Update_src","Updated2");
            }
        });



    }


    @Override
    public void onDestroyView() {
        try {
            requireContext().unregisterReceiver(musicReceiver);
        } catch (IllegalArgumentException e) {
        }
        view = null;
        super.onDestroyView();
    }

    private String formatTime(int millis) {
        int minutes = (millis / 1000) / 60;
        int seconds = (millis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
