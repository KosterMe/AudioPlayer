package com.samsung.priorityplayer;

import static com.samsung.priorityplayer.MusicService.ACTION_PAUSE;
import static com.samsung.priorityplayer.MusicService.ACTION_PLAY;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;

public class PlayerRandom extends Fragment {
    private ArrayList<Song> Songs;
    private ComplexWaveVisualizerView fakeVisualizer;
    private final Handler handler = new Handler();
    private Runnable visualizerUpdater;
    private View view;
    private boolean isPaused = false;
    private boolean receiverRegistered = false;

    public PlayerRandom(ArrayList<Song> songs) {
        Songs = songs;
    }

    private final BroadcastReceiver musicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;

            switch (intent.getAction()) {
                case MusicService.ACTION_SONG_CHANGED:
                    updateButton();
                    break;
                default:
                    Log.i("MyTag", "nothing");
            }
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;

        fakeVisualizer = view.findViewById(R.id.fakeVisualizer);
        updateButton();

        visualizerUpdater = new Runnable() {
            @Override
            public void run() {
                if (Player.isPlayingStatic() && MusicDataHolder.isIsRandom()) {
                    fakeVisualizer.setPlaying(true);
                } else {
                    fakeVisualizer.setPlaying(false);
                }
                handler.postDelayed(this, 50);
            }
        };
        handler.post(visualizerUpdater);

        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicService.ACTION_SONG_CHANGED);
        ContextCompat.registerReceiver(requireContext(), musicReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        receiverRegistered = true;
    }

    private void updateButton() {
        if (view == null) {
            return;
        }

        ImageButton randomPlaybackButton = view.findViewById(R.id.random_playback_button);
        if (Player.isPlayingStatic() && MusicDataHolder.isIsRandom()) {
            randomPlaybackButton.setImageResource(R.drawable.baseline_pause_circle_24);
            randomPlaybackButton.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), MusicService.class);
                intent.setAction(ACTION_PAUSE);
                requireContext().startService(intent);
                isPaused = true;
            });
        } else {
            randomPlaybackButton.setImageResource(R.drawable.baseline_play_circle_filled_24);
            randomPlaybackButton.setOnClickListener(v -> {
                if (isPaused) {
                    Intent intent = new Intent(requireContext(), MusicService.class);
                    intent.setAction(ACTION_PLAY);
                    requireContext().startService(intent);
                } else {
                    if (Songs == null || Songs.isEmpty()) {
                        Toast.makeText(requireContext(), "No tracks available", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int randomIndex = RandomSong.getRandomElementNumber();
                    if (randomIndex < 0 || randomIndex >= Songs.size()) {
                        randomIndex = 0;
                    }
                    SongPlayer songPlayer = new SongPlayer(Songs, randomIndex);
                    MusicDataHolder.setIsRandom(true);
                    openFragment(songPlayer);
                }
                isPaused = false;
            });
        }
    }

    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.place_holder_for_song_player, fragment);
        transaction.commit();
    }

    @Override
    public void onDestroyView() {
        handler.removeCallbacks(visualizerUpdater);
        try {
            if (receiverRegistered) {
                requireContext().unregisterReceiver(musicReceiver);
            }
        } catch (IllegalArgumentException e) {
        } finally {
            receiverRegistered = false;
        }
        view = null;
        super.onDestroyView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_random_song, container, false);
    }
}