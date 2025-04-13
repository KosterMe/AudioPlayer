package com.samsung.playerindividual;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.os.Handler;
import android.widget.Toast;

import java.util.List;

public class SongPlayer extends Fragment {
    private static final int EPS = 1500;
    private Song song;
    private List<Song> Songs;
    private Player mp;
    private SeekBar seekBar;
    private TextView tvCurrentTime;
    private Handler handler = new Handler();
    private int current;
    View view;

    public SongPlayer(List<Song> Songs, int current) {
        this.Songs = Songs;
        this.current = current;
        this.song = Songs.get(current);

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

        mp = new Player(current, Songs);
        startActivity(song);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(requireContext(), "click", Toast.LENGTH_SHORT).show();
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }

    private void startActivity(Song song) {
        mp.stopAudio();
        mp.playAudio(song);

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

        seekBar = view.findViewById(R.id.fragment_seek_bar);
        seekBar.setMax(mp.getTotalTime());
        tvCurrentTime = view.findViewById(R.id.current_time);
        TextView tvTotalTime = view.findViewById(R.id.total_time);
        tvTotalTime.setText(formatTime(mp.getTotalTime()));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvCurrentTime.setText(formatTime(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekBar.getProgress() >= mp.getTotalTime() - EPS) {
                    mp.nextAudio();
                    startActivity(mp.getSong());
                } else {
                    mp.SeekTo(seekBar.getProgress());
                }
            }
        });
        Runnable updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (mp.getMediaPlayer() != null && mp.IsPlaying()) {
                    seekBar.setProgress(mp.GetCurrentPosition());
                    tvCurrentTime.setText(formatTime(mp.GetCurrentPosition()));
                    handler.postDelayed(this, 1000);
                    if (mp.GetCurrentPosition() >= mp.getTotalTime() - EPS) {
                        mp.nextAudio();
                        startActivity(mp.getSong());
                    }
                }
            }
        };
        handler.postDelayed(updateSeekBar, 1000);

        ImageButton play_stop_ib = view.findViewById(R.id.fragment_play_stop);
        play_stop_ib.setImageResource(R.drawable.baseline_stop_24);
        play_stop_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mp.IsPlaying()) {
                    mp.pauseAudio();
                    play_stop_ib.setImageResource(R.drawable.baseline_play_arrow_24);
                } else {
                    mp.resumeAudio();
                    handler.postDelayed(updateSeekBar, 1000);
                    play_stop_ib.setImageResource(R.drawable.baseline_stop_24);
                }
            }
        });

        ImageButton next_ib = view.findViewById(R.id.fragment_next);
        next_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mp.nextAudio();
                startActivity(mp.getSong());
            }
        });
        ImageButton last_ib = view.findViewById(R.id.fragment_last);
        last_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mp.lastAudio();
                startActivity(mp.getSong());
            }
        });

    }

    private String formatTime(int millis) {
        int minutes = (millis / 1000) / 60;
        int seconds = (millis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void onDestroy() {
        mp.DestroyMedia();
        super.onDestroy();
    }

}