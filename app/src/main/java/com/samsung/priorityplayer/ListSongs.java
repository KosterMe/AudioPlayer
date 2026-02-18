package com.samsung.priorityplayer;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class ListSongs extends Fragment {
    private ArrayList<Song> songs;
    private Context ctx;
    private ListAdapter adapter;
    private Button btnAddToPlaylist;
    private Button btnEdit;
    private ImageButton btnSort;
    private boolean isAscending = true;

    public ListSongs(Context ctx, ArrayList<Song> songList) {
        this.ctx = ctx;
        this.songs = songList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_songs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ListView listView = view.findViewById(R.id.list_songs);
        adapter = new ListAdapter(ctx, songs);
        listView.setAdapter(adapter);

        try {
            listView.post(() -> listView.smoothScrollToPosition(MusicDataHolder.getCurrentIndex() + 5));
        } catch (Exception e) {
            Log.i("list_view_autoscroll", "No current index available");
        }

        btnEdit = view.findViewById(R.id.button_edit);
        btnEdit.setVisibility(View.GONE);
        btnAddToPlaylist = view.findViewById(R.id.button_add_to_playlist);
        btnAddToPlaylist.setVisibility(View.GONE);

        listView.setOnItemClickListener((parent, itemView, position, id) -> {
            if (adapter.isSelectionMode()) {
                adapter.toggleSelection(position);
                updateActionButtonsVisibility(view);
            } else {
                SongPlayer playerFragment = new SongPlayer(songs, position);
                MusicDataHolder.setIsRandom(false);
                openFragment(playerFragment);
            }
        });

        listView.setOnItemLongClickListener((parent, itemView, position, id) -> {
            adapter.toggleSelection(position);
            updateActionButtonsVisibility(view);
            return true;
        });

        view.findViewById(R.id.button_delete).setOnClickListener(v -> {
            List<Song> toDelete = new ArrayList<>();
            SongDatabaseHelper db = new SongDatabaseHelper(ctx, MainActivity.DATA_VERSION);

            for (int pos : adapter.getSelectedItems()) {
                Song selected = songs.get(pos);
                toDelete.add(selected);

                if (MusicDataHolder.getPlName() != null && !MusicDataHolder.getPlName().isEmpty()) {
                    db.deleteLink(MusicDataHolder.getPlName(), selected.getPath());
                } else {
                    db.deleteSongByPath(selected.getPath());
                }
            }

            songs.removeAll(toDelete);
            adapter.clearSelection();
            adapter.notifyDataSetChanged();
            updateActionButtonsVisibility(view);
        });

        btnAddToPlaylist.setOnClickListener(v -> {
            List<Song> selectedSongs = new ArrayList<>();
            for (int pos : adapter.getSelectedItems()) {
                selectedSongs.add(songs.get(pos));
            }
            showPlaylistPickerDialog(selectedSongs);
            adapter.clearSelection();
            updateActionButtonsVisibility(view);
        });

        btnEdit.setOnClickListener(v -> {
            List<Integer> selectedPositions = new ArrayList<>(adapter.getSelectedItems());
            if (selectedPositions.size() != 1) {
                Toast.makeText(ctx, "Select one track to edit", Toast.LENGTH_SHORT).show();
                return;
            }
            showEditOptionDialog(selectedPositions.get(0));
            adapter.clearSelection();
            updateActionButtonsVisibility(view);
        });

        btnSort = view.findViewById(R.id.button_sort);
        btnSort.setOnClickListener(v -> showSortDialog());

        super.onViewCreated(view, savedInstanceState);
    }

    private void updateActionButtonsVisibility(View view) {
        View buttons = view.findViewById(R.id.selection_buttons);
        if (adapter.isSelectionMode()) {
            buttons.setVisibility(View.VISIBLE);
            btnAddToPlaylist.setVisibility(View.VISIBLE);
            btnEdit.setVisibility(View.VISIBLE);
        } else {
            buttons.setVisibility(View.GONE);
            btnAddToPlaylist.setVisibility(View.GONE);
            btnEdit.setVisibility(View.GONE);
        }
    }

    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.place_holder_for_song_player, fragment);
        transaction.commit();
    }

    private void showPlaylistPickerDialog(List<Song> selectedSongs) {
        SongDatabaseHelper db = new SongDatabaseHelper(ctx, MainActivity.DATA_VERSION);
        List<String> playlists = db.getAllPLName();

        if (playlists.isEmpty()) {
            Toast.makeText(ctx, "No playlists available", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] playlistArray = playlists.toArray(new String[0]);

        dialogBuilder()
                .setTitle("Choose playlist")
                .setItems(playlistArray, (dialog, which) -> {
                    for (Song song : selectedSongs) {
                        db.insertLink(playlistArray[which], song.getPath());
                    }
                    Toast.makeText(ctx, "Songs added to playlist", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditOptionDialog(int pos) {
        Song song = songs.get(pos);
        String[] options = {"Rename", "Change priority"};

        dialogBuilder()
                .setTitle("What do you want to edit?")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showRenameDialog(song);
                    } else {
                        showPriorityDialog(pos);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showRenameDialog(Song song) {
        EditText input = createDialogInput(song.getName());

        dialogBuilder()
                .setTitle("Rename track")
                .setView(wrapInput(input))
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        song.setName(newName);
                        SongDatabaseHelper db = new SongDatabaseHelper(ctx, MainActivity.DATA_VERSION);
                        db.renameSong(song.getPath(), newName);
                        MusicDataHolder.setSongs(songs);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(ctx, "Track name updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ctx, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showPriorityDialog(int pos) {
        Song song = songs.get(pos);
        View dialogView = LayoutInflater.from(ctx).inflate(R.layout.dialog_edit_priority, null);

        SeekBar seekBarPriority = dialogView.findViewById(R.id.seekbar_priority);
        TextView priorityText = dialogView.findViewById(R.id.text_priority_value);
        CheckBox checkBoxLock = dialogView.findViewById(R.id.checkbox_lock_priority);

        int priority = song.getPriority();
        seekBarPriority.setProgress(priority);
        priorityText.setText(getString(R.string.priority_value, priority));

        checkBoxLock.setChecked(!song.isChangeable());
        checkBoxLock.setOnCheckedChangeListener((buttonView, isChecked) -> song.setChangeable(!isChecked));

        seekBarPriority.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                priorityText.setText(getString(R.string.priority_value, progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        dialogBuilder()
                .setTitle("Change priority")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    int newPriority = seekBarPriority.getProgress();
                    MusicDataHolder.EditPriority(pos, newPriority);
                    SongDatabaseHelper db = new SongDatabaseHelper(ctx, MainActivity.DATA_VERSION);
                    db.updatePriority(song.getPath(), newPriority);
                    db.updateChangeable(song.getPath(), !checkBoxLock.isChecked());
                    MusicDataHolder.setSongs(songs);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(ctx, "Priority updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showSortDialog() {
        String[] options = {"By title", "By priority"};

        dialogBuilder()
                .setTitle("Sort")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            if (isAscending) {
                                songs.sort((s1, s2) -> s1.getName().compareToIgnoreCase(s2.getName()));
                            } else {
                                songs.sort((s1, s2) -> s2.getName().compareToIgnoreCase(s1.getName()));
                            }
                            break;
                        case 1:
                            if (isAscending) {
                                songs.sort((s1, s2) -> Float.compare(s1.getPriority(), s2.getPriority()));
                            } else {
                                songs.sort((s1, s2) -> Float.compare(s2.getPriority(), s1.getPriority()));
                            }
                            break;
                        default:
                            break;
                    }
                    isAscending = !isAscending;
                    adapter.notifyDataSetChanged();
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
