package com.samsung.priorityplayer;

import android.content.Context;
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
    private final ArrayList<Song> songList;
    private final Context ctx;
    private final Set<Integer> selectedItems = new HashSet<>();
    private boolean selectionMode = false;

    public ListAdapter(Context context, ArrayList<Song> songList) {
        this.ctx = context;
        this.songList = songList;
    }

    public void toggleSelection(int position) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position);
        } else {
            selectedItems.add(position);
        }
        selectionMode = !selectedItems.isEmpty();
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
        return songList.size();
    }

    @Override
    public Object getItem(int i) {
        return songList.get(i);
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

        Song song = songList.get(i);
        TextView nameText = view.findViewById(R.id.item_name);
        TextView artistText = view.findViewById(R.id.item_artist);
        ImageView albumView = view.findViewById(R.id.item_ImageView);

        nameText.setText(song.getName());
        artistText.setText(song.getArtist());
        if (song.getAlbumBitmap() != null) {
            albumView.setImageBitmap(song.getAlbumBitmap());
        } else {
            albumView.setImageResource(R.drawable.default_image_for_item);
        }

        view.setBackgroundResource(R.drawable.bg_card_surface);
        int itemColor = selectedItems.contains(i)
                ? ContextCompat.getColor(ctx, R.color.selected_item_color)
                : ContextCompat.getColor(ctx, R.color.surface_card);
        view.getBackground().setTint(itemColor);

        return view;
    }
}
