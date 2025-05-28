package com.samsung.priorityplayer;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ListAdapter extends BaseAdapter {
    private ArrayList<Song> SongList;
    private Context ctx;
    private Set<Integer> selectedItems = new HashSet<>();
    private boolean selectionMode = false;

    public ListAdapter(Context context, ArrayList<Song> songList) {
        this.ctx = context;
        SongList = songList;
    }

    public void toggleSelection(int position) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position);
        } else {
            selectedItems.add(position);
        }
        if (selectedItems.isEmpty()) {
            selectionMode = false;
        } else {
            selectionMode = true;
        }
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedItems.clear();
        selectionMode = false;
        notifyDataSetChanged();
    }

    public boolean isSelectionMode() {
        return selectionMode;
    }

    public Set<Integer> getSelectedItems() {
        return selectedItems;
    }

    @Override
    public int getCount() {
        return SongList.size();
    }

    @Override
    public Object getItem(int i) {
        return SongList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(ctx).inflate(R.layout.item_song, parent, false);
        }

        Song song = SongList.get(i);
        TextView tn = view.findViewById(R.id.item_name);
        TextView ta = view.findViewById(R.id.item_artist);
        ImageView album = view.findViewById(R.id.item_ImageView);

        tn.setText(song.getName());
        ta.setText(song.getArtist());
        if (song.getAlbumBitmap() != null) {
            album.setImageBitmap(song.getAlbumBitmap());
        } else {
            album.setImageResource(R.drawable.default_image_for_item);
        }

        // Отметим фон выделенных элементов
        view.setBackgroundColor(selectedItems.contains(i) ?
                ContextCompat.getColor(ctx, R.color.selected_item_color) :
                Color.TRANSPARENT);

        return view;
    }
}

