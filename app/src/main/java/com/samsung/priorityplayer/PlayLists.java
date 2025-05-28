package com.samsung.priorityplayer;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PlayLists extends Fragment {
    private ArrayList<String> PLList;
    private Context ctx;
    private PLAdapter adapter;

    public PlayLists(Context ctx, ArrayList<String> PLList) {
        this.ctx = ctx;
        this.PLList = PLList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_play_lists, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ListView lv = view.findViewById(R.id.play_lists);
        Button btnDelete = view.findViewById(R.id.button_delete_playlist);
        Button btnAdd = view.findViewById(R.id.button_add_playlist);
        Button btnRename = view.findViewById(R.id.button_rename_playlist);
        View buttons = view.findViewById(R.id.selection_buttons);

        adapter = new PLAdapter(ctx, PLList);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener((parent, itemView, position, id) -> {
            if (adapter.isSelectionMode()) {
                adapter.toggleSelection(position);
                updateActionButtonsVisibility(buttons);
            } else {
                SongDatabaseHelper dbHelper = new SongDatabaseHelper(ctx, MainActivity.DATA_VERSION);
                List<Song> Songs = dbHelper.getSongsByPlaylistName(PLList.get(position));
                MusicDataHolder.setPlName(PLList.get(position));
                ListSongs lvFragment = new ListSongs(ctx, (ArrayList<Song>) Songs);
                openFragment(lvFragment);
            }
        });

        lv.setOnItemLongClickListener((parent, itemView, position, id) -> {
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
                String name = PLList.get(index);
                db.deletePL(name);
                toRemove.add(name);
            }

            PLList.removeAll(toRemove);
            adapter.clearSelection();
            adapter.notifyDataSetChanged();
            updateActionButtonsVisibility(buttons);

            Toast.makeText(ctx, "Плейлисты удалены", Toast.LENGTH_SHORT).show();
        });

        btnAdd.setOnClickListener(v -> showAddPlaylistDialog());

        btnRename.setOnClickListener(v -> {
            List<Integer> selected = adapter.getSelectedItems();
            if (selected.size() != 1) {
                Toast.makeText(ctx, "Выберите один плейлист для переименования", Toast.LENGTH_SHORT).show();
                return;
            }

            int index = selected.get(0);
            String oldName = PLList.get(index);

            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle("Переименовать плейлист");

            final EditText input = new EditText(ctx);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setText(oldName);
            builder.setView(input);

            builder.setPositiveButton("Сохранить", (dialog, which) -> {
                String newName = input.getText().toString().trim();
                if (newName.isEmpty()) {
                    Toast.makeText(ctx, "Имя не может быть пустым", Toast.LENGTH_SHORT).show();
                    return;
                }

                SongDatabaseHelper db = new SongDatabaseHelper(ctx, MainActivity.DATA_VERSION);

                // Проверка: уже существует плейлист с таким именем
                if (!oldName.equals(newName) && db.getAllPLName().contains(newName)) {
                    Toast.makeText(ctx, "Плейлист с таким именем уже существует", Toast.LENGTH_SHORT).show();
                    return;
                }

                db.renamePL(oldName, newName);
                PLList.set(index, newName);
                adapter.clearSelection();
                adapter.notifyDataSetChanged();
                updateActionButtonsVisibility(view.findViewById(R.id.selection_buttons));
                Toast.makeText(ctx, "Плейлист переименован", Toast.LENGTH_SHORT).show();
            });


            builder.setNegativeButton("Отмена", null);
            builder.show();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("Новый плейлист");

        final EditText input = new EditText(ctx);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Создать", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                SongDatabaseHelper db = new SongDatabaseHelper(ctx, MainActivity.DATA_VERSION);
                if (db.insertPL(name)) {
                    PLList.add(name);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(ctx, "Плейлист создан", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ctx, "Плейлист уже существует", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ctx, "Имя не может быть пустым", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }
}