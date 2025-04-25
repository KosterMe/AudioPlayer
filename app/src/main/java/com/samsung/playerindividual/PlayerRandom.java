package com.samsung.playerindividual;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

public class PlayerRandom extends Fragment {
    private ArrayList<Song> Songs = (ArrayList<Song>) MusicDataHolder.getSongs();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_random_song, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ImageButton rpb = view.findViewById(R.id.random_playback_button);
        rpb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                    SongPlayer sp = new SongPlayer(Songs, RandomSong.getRandomElementNumber());
                    MusicDataHolder.setIsRandom(true);
                    openFragment(sp);
                }
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }
    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.place_holder_for_song_player, fragment); // Меняем текущий фрагмент
        transaction.commit();
    }

}