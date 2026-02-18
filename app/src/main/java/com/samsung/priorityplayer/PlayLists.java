package com.samsung.priorityplayer;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PlayLists extends Fragment {
    private ArrayList<String> playLists;
    private Context ctx;
    private PLAdapter adapter;

    public PlayLists(Context ctx, ArrayList<String> playList) {
        this.ctx = ctx;
        this.playLists = playList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_play_lists, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ListView listView = view.findViewById(R.id.play_lists);
        Button btnDelete = view.findViewById(R.id.button_delete_playlist);
        Button btnAdd = view.findViewById(R.id.button_add_playlist);
        Button btnRename = view.findViewById(R.id.button_rename_playlist);
        View buttons = view.findViewById(R.id.selection_buttons);

        adapter = new PLAdapter(ctx, playLists);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, itemView, position, id) -> {
            if (adapter.isSelectionMode()) {
                adapter.toggleSelection(position);
                updateActionButtonsVisibility(buttons);
            } else {
                SongDatabaseHelper dbHelper = new SongDatabaseHelper(ctx, MainActivity.DATA_VERSION);
                List<Song> songs = dbHelper.getSongsByPlaylistName(playLists.get(position));
                MusicDataHolder.setPlName(playLists.get(position));
                ListSongs listSongsFragment = new ListSongs(ctx, (ArrayList<Song>) songs);
                openFragment(listSongsFragment);
            }
        });

        listView.setOnItemLongClickListener((parent, itemView, position, id) -> {
            adapter.toggleSelection(position);
            updateActionButtonsVisibility(buttons);
            return true;
        });

        btnDelete.setOnClickListener(v -> {
            SongDatabaseHelper db = new SongDatabaseHelper(ctx, MainActivity.DATA_VERSION);
            List<Integer> selected = adapter.getSelectedItems();
            Iterator<Integer> iterator = selected.iterator();
            ArrayList<String> toRemove = new ArrayList<>();

            while (iterator.hasNext()) {
                int index = iterator.next();
                String name = playLists.get(index);
                db.deletePL(name);
                toRemove.add(name);
            }

            playLists.removeAll(toRemove);
            adapter.clearSelection();
            adapter.notifyDataSetChanged();
            updateActionButtonsVisibility(buttons);

            Toast.makeText(ctx, "Playlists deleted", Toast.LENGTH_SHORT).show();
        });

        btnAdd.setOnClickListener(v -> showAddPlaylistDialog());

        btnRename.setOnClickListener(v -> {
            List<Integer> selected = adapter.getSelectedItems();
            if (selected.size() != 1) {
                Toast.makeText(ctx, "Select one playlist to rename", Toast.LENGTH_SHORT).show();
                return;
            }

            int index = selected.get(0);
            String oldName = playLists.get(index);
            EditText input = createDialogInput(oldName);
            input.setInputType(InputType.TYPE_CLASS_TEXT);

            dialogBuilder()
                    .setTitle("Rename playlist")
                    .setView(wrapInput(input))
                    .setPositiveButton("Save", (dialog, which) -> {
                        String newName = input.getText().toString().trim();
                        if (newName.isEmpty()) {
                            Toast.makeText(ctx, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        SongDatabaseHelper db = new SongDatabaseHelper(ctx, MainActivity.DATA_VERSION);
                        if (!oldName.equals(newName) && db.getAllPLName().contains(newName)) {
                            Toast.makeText(ctx, "Playlist with this name already exists", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        db.renamePL(oldName, newName);
                        playLists.set(index, newName);
                        adapter.clearSelection();
                        adapter.notifyDataSetChanged();
                        updateActionButtonsVisibility(view.findViewById(R.id.selection_buttons));
                        Toast.makeText(ctx, "Playlist renamed", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        super.onViewCreated(view, savedInstanceState);
    }

    private void updateActionButtonsVisibility(View buttons) {
        if (adapter.isSelectionMode()) {
            buttons.setVisibility(View.VISIBLE);
        } else {
            buttons.setVisibility(View.GONE);
        }
    }

    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.place_holder, fragment);
        transaction.commit();
    }

    private void showAddPlaylistDialog() {
        EditText input = createDialogInput("");
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        dialogBuilder()
                .setTitle("New playlist")
                .setView(wrapInput(input))
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        SongDatabaseHelper db = new SongDatabaseHelper(ctx, MainActivity.DATA_VERSION);
                        if (db.insertPL(name)) {
                            playLists.add(name);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(ctx, "Playlist created", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ctx, "Playlist already exists", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ctx, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private MaterialAlertDialogBuilder dialogBuilder() {
        return new MaterialAlertDialogBuilder(ctx, R.style.ThemeOverlay_PlayerIndividual_Dialog);
    }

    private EditText createDialogInput(String initialValue) {
        EditText input = new EditText(ctx);
        input.setText(initialValue);
        input.setTextColor(ContextCompat.getColor(ctx, R.color.darkgrey));
        input.setHintTextColor(ContextCompat.getColor(ctx, R.color.grey));
        input.setBackgroundResource(R.drawable.bg_dialog_input);
        int horizontalPadding = dpToPx(14);
        int verticalPadding = dpToPx(12);
        input.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
        input.setSingleLine(true);
        return input;
    }

    private View wrapInput(EditText input) {
        FrameLayout container = new FrameLayout(ctx);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        int marginHorizontal = dpToPx(4);
        int marginTop = dpToPx(8);
        params.setMargins(marginHorizontal, marginTop, marginHorizontal, 0);
        container.addView(input, params);
        return container;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
