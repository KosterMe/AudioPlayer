package com.samsung.playerindividual;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.os.Handler;
import java.util.List;

public class SongPlayer extends Fragment {
    private MediaPlayer mediaPlayer;
    private Song song;
    private List<Song> Songs;
    private Player mp;
    private int current;
    private SeekBar seekBar;
    private TextView tvCurrentTime;
    private Handler handler = new Handler();
    View view;
    public SongPlayer(List<Song> Songs,int current) {
        this.Songs = Songs;
        this.current = current;
        this.song = Songs.get(current);
        playAudio(song);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        return inflater.inflate(R.layout.fragment_song_player, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.view = view;
        startActivity(song);
        super.onViewCreated(view, savedInstanceState);
    }

    private void startActivity(Song song){
        TextView tw = view.findViewById(R.id.fragment_song_name);
        tw.setText(song.getName());
        ImageButton play_stop_ib = view.findViewById(R.id.fragment_play_stop);
        play_stop_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()){
                    pauseAudio();
                    play_stop_ib.setImageResource(R.drawable.baseline_play_arrow_24);
                }else{
                    resumeAudio();
                    play_stop_ib.setImageResource(R.drawable.baseline_stop_24);
                }
            }
        });

        seekBar = view.findViewById(R.id.fragment_seek_bar);
        seekBar.setMax(mediaPlayer.getDuration());
        tvCurrentTime = view.findViewById(R.id.current_time);
        TextView tvTotalTime = view.findViewById(R.id.total_time);
        tvTotalTime.setText(formatTime(mediaPlayer.getDuration()));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
                tvCurrentTime.setText(formatTime(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        Runnable updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    tvCurrentTime.setText(formatTime(mediaPlayer.getCurrentPosition()));
                    handler.postDelayed(this, 500);
                }
            }
        };
        handler.postDelayed(updateSeekBar, 1000);

        ImageButton next_ib = view.findViewById(R.id.fragment_next);
        next_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextAudio();
            }
        });
        ImageButton last_ib = view.findViewById(R.id.fragment_last);
        last_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lastAudio();
            }
        });

    }

    private String formatTime(int millis) {
        int minutes = (millis / 1000) / 60;
        int seconds = (millis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }



    public void playAudio(Song song){
        stopAudio();
        mediaPlayer = new MediaPlayer();
        String path = song.getPath();
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    nextAudio();
                }
            });
        }catch (Exception e){}
    }
    public void resumeAudio(){
        try {
            if (mediaPlayer != null) {
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        nextAudio();
                    }
                });
            }
        }catch (Exception e){}
    }
    public void nextAudio(){
        if (current < Songs.size()) {
            current++;
            song = Songs.get(current);
            playAudio(song);
            startActivity(song);

        }
        else {
            song = Songs.get(0);
            current = 0;
            playAudio(song);
            startActivity(song);
        }
    }
    public void lastAudio(){
        if (current != 0) {
            current--;
            song = Songs.get(current);
            playAudio(song);
            startActivity(song);

        }
        else {
            song = Songs.get(Songs.size()-1);
            current = Songs.size()-1;
            playAudio(song);
            startActivity(song);
        }
    }
    public void stopAudio(){
        if (mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    public void pauseAudio(){
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }
    public void onDestroy(){
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

}