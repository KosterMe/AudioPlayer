package com.samsung.audioplayer;

import static com.samsung.audioplayer.MusicService.ACTION_PAUSE;
import static com.samsung.audioplayer.MusicService.ACTION_PLAY;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;

public class PlayerRandom extends Fragment {
    private ArrayList<Song> Songs;
    private ComplexWaveVisualizerView fakeVisualizer;
    private Handler handler = new Handler();
    private Runnable visualizerUpdater;
    private View view;
    private boolean isPaused = false;

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
                    Log.i("MyTag","nothing");
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
                if (Player.isPlayingStatic() && MusicDataHolder.isIsRandom()) {  // Проверка на воспроизведение
                    fakeVisualizer.setPlaying(true);  // Включаем визуализатор

                } else {
                    fakeVisualizer.setPlaying(false);  // Останавливаем движение
                }
                handler.postDelayed(this, 50);  // Обновление каждые 50 мс
            }
        };
        handler.post(visualizerUpdater);  // Запускаем обновление

        IntentFilter filter = new IntentFilter();

        filter.addAction(MusicService.ACTION_SONG_CHANGED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(musicReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            Log.i("MyTag","reg");
        } else {
            requireContext().registerReceiver(musicReceiver, filter);
        }
    }
    private void updateButton(){
        ImageButton rpb = view.findViewById(R.id.random_playback_button);
        if (Player.isPlayingStatic() && MusicDataHolder.isIsRandom()){
            rpb.setImageResource(R.drawable.baseline_pause_circle_24);
            rpb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(requireContext(), MusicService.class);
                    intent.setAction(ACTION_PAUSE);
                    requireContext().startService(intent);
                    isPaused = true;
                }
            });

        }else{
            rpb.setImageResource(R.drawable.baseline_play_circle_filled_24);
            rpb.setOnClickListener(v -> {
                if (isPaused){
                    Intent intent = new Intent(requireContext(), MusicService.class);
                    intent.setAction(ACTION_PLAY);
                    requireContext().startService(intent);
                }else{
                    SongPlayer sp = new SongPlayer(Songs, RandomSong.getRandomElementNumber());
                    MusicDataHolder.setIsRandom(true);
                    openFragment(sp);
                }
                isPaused = false;
            });
        }

    }


    // Переход на новый фрагмент
    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.place_holder_for_song_player, fragment);
        transaction.commit();
    }

    // Очистка в onDestroyView()
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(visualizerUpdater);  // Останавливаем обновления
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_random_song, container, false);
    }
}
