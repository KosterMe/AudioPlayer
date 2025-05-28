package com.samsung.priorityplayer;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;

public class ListSongs extends Fragment {
    private ArrayList<Song> Songs;
    private Context ctx;
    private ListAdapter adapter;
    private Button btnAddToPlaylist;
    private Button btnEdit;
    private ImageButton btnSort;
    private boolean isAscending = true;

    public ListSongs(Context ctx, ArrayList<Song> songList) {
        this.ctx = ctx;
        Songs = songList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_songs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ListView lv = view.findViewById(R.id.list_songs);
        adapter = new ListAdapter(ctx, Songs);
        lv.setAdapter(adapter);
        try {
            lv.post(() -> lv.smoothScrollToPosition(MusicDataHolder.getCurrentIndex() + 5));
        }catch (Exception e){
            Log.i("list_view_autoscroll", "dont have index");
        }


        btnEdit = view.findViewById(R.id.button_edit);
        btnEdit.setVisibility(View.GONE);

        // Инициализация кнопки "Добавить в плейлист"
        btnAddToPlaylist = view.findViewById(R.id.button_add_to_playlist);
        btnAddToPlaylist.setVisibility(View.GONE); // Скрыть кнопку по умолчанию

        // Слушатель для выбора песни
        lv.setOnItemClickListener((parent, itemView, position, id) -> {
            if (adapter.isSelectionMode()) {
                adapter.toggleSelection(position);
                updateActionButtonsVisibility(view);
            } else {
                // обычный запуск песни
                SongPlayer mp = new SongPlayer(Songs, position);
                MusicDataHolder.setIsRandom(false);
                openFragment(mp);
            }
        });

        // Слушатель для длинного нажатия для включения режима выделения
        lv.setOnItemLongClickListener((parent, itemView, position, id) -> {
            adapter.toggleSelection(position);
            updateActionButtonsVisibility(view);
            return true;
        });

        // Кнопка "Удалить"
        view.findViewById(R.id.button_delete).setOnClickListener(v -> {
            List<Song> toDelete = new ArrayList<>();
            for (int pos : adapter.getSelectedItems()) {
                toDelete.add(Songs.get(pos));

                SongDatabaseHelper db = new SongDatabaseHelper(ctx, MainActivity.DATA_VERSION);
                if (!MusicDataHolder.getPlName().isEmpty()){
                    db.deleteLink(MusicDataHolder.getPlName(),Songs.get(pos).getPath());
                }else{
                    db.deleteSongByPath(Songs.get(pos).getPath());
                }
            }

            Songs.removeAll(toDelete);
            adapter.clearSelection();
            adapter.notifyDataSetChanged();
            updateActionButtonsVisibility(view);
        });

        // Кнопка "Добавить в плейлист"
        btnAddToPlaylist.setOnClickListener(v -> {
            List<Song> selectedSongs = new ArrayList<>();
            for (int pos : adapter.getSelectedItems()) {
                selectedSongs.add(Songs.get(pos));
            }
            // Показать диалог выбора плейлиста
            showPlaylistPickerDialog(selectedSongs);
            adapter.clearSelection();
            updateActionButtonsVisibility(view);
        });
        btnEdit.setOnClickListener(v -> {
            List<Integer> selectedPositions = new ArrayList<>(adapter.getSelectedItems());

            if (selectedPositions.size() != 1) {
                Toast.makeText(ctx, "Выберите одну песню для редактирования", Toast.LENGTH_SHORT).show();
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
            btnEdit.setVisibility(View.VISIBLE);  // <- добавлено
        } else {
            buttons.setVisibility(View.GONE);
            btnAddToPlaylist.setVisibility(View.GONE);
            btnEdit.setVisibility(View.GONE);     // <- добавлено
        }
    }

    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.place_holder_for_song_player, fragment);
        transaction.commit();
    }

    // Показать диалог для выбора плейлиста
    private void showPlaylistPickerDialog(List<Song> selectedSongs) {
        SongDatabaseHelper db = new SongDatabaseHelper(ctx, MainActivity.DATA_VERSION);
        List<String> playlists = db.getAllPLName();

        if (playlists.isEmpty()) {
            Toast.makeText(ctx, "Нет плейлистов", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] playlistArray = playlists.toArray(new String[0]);

        new android.app.AlertDialog.Builder(ctx)
                .setTitle("Выберите плейлист")
                .setItems(playlistArray, (dialog, which) -> {
                    // Добавляем все выбранные песни в этот плейлист
                    for (Song song : selectedSongs) {
                        db.insertLink(playlistArray[which], song.getPath());
                    }
                    Toast.makeText(ctx, "Песни добавлены в плейлист", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showEditOptionDialog(int pos) {
        Song song = Songs.get(pos);
        String[] options = {"Переименовать", "Изменить приоритет"};

        new AlertDialog.Builder(ctx)
                .setTitle("Что изменить?")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showRenameDialog(song);
                    } else {
                        showPriorityDialog(pos);
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showRenameDialog(Song song) {
        EditText input = new EditText(ctx);
        input.setText(song.getName());

        new AlertDialog.Builder(ctx)
                .setTitle("Переименование")
                .setView(input)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        song.setName(newName);
                        SongDatabaseHelper db = new SongDatabaseHelper(ctx, MainActivity.DATA_VERSION);
                        db.renameSong(song.getPath(), newName);
                        MusicDataHolder.setSongs(Songs);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(ctx, "Название обновлено", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ctx, "Название не может быть пустым", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showPriorityDialog(int pos) {
        Song song = Songs.get(pos);
        View dialogView = LayoutInflater.from(ctx).inflate(R.layout.dialog_edit_priority, null);

        SeekBar seekBarPriority = dialogView.findViewById(R.id.seekbar_priority);
        TextView priorityText = dialogView.findViewById(R.id.text_priority_value);
        CheckBox checkBoxLock = dialogView.findViewById(R.id.checkbox_lock_priority); // чекбокс

        int priority = (int) song.getPriority();
        seekBarPriority.setProgress(priority);
        priorityText.setText("Приоритет: " + priority);

        checkBoxLock.setChecked(!song.isChangeable()); // инвертировано: "Не изменять"
        checkBoxLock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            song.setChangeable(!isChecked); // логика инвертируется: если чекбокс активен — нельзя менять
        });

        seekBarPriority.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                priorityText.setText("Приоритет: " + progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        new AlertDialog.Builder(ctx)
                .setTitle("Изменить приоритет")
                .setView(dialogView)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    int newPriority = seekBarPriority.getProgress();
                    MusicDataHolder.EditPriority(pos, newPriority);
                    SongDatabaseHelper db = new SongDatabaseHelper(ctx, MainActivity.DATA_VERSION);
                    db.updatePriority(song.getPath(), newPriority);
                    db.updateChangeable(song.getPath(), !checkBoxLock.isChecked());
                    MusicDataHolder.setSongs(Songs);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(ctx, "Приоритет обновлен", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showSortDialog() {
        String[] options = {"По названию", "По приоритету"};

        new AlertDialog.Builder(ctx)
                .setTitle("Сортировка")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            if (isAscending) {
                                Songs.sort((s1, s2) -> s1.getName().compareToIgnoreCase(s2.getName()));
                            } else {
                                Songs.sort((s1, s2) -> s2.getName().compareToIgnoreCase(s1.getName()));
                            }
                            break;
                        case 1:
                            if (isAscending) {
                                Songs.sort((s1, s2) -> Float.compare(s1.getPriority(), s2.getPriority()));
                            } else {
                                Songs.sort((s1, s2) -> Float.compare(s2.getPriority(), s1.getPriority()));
                            }
                            break;
                    }
                    isAscending = !isAscending; // Переключение направления
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}
