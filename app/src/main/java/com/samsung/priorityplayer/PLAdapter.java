package com.samsung.priorityplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PLAdapter extends BaseAdapter {
    private final ArrayList<String> playList;
    private final Context ctx;
    private final Set<Integer> selectedItems = new HashSet<>();

    public PLAdapter(Context context, ArrayList<String> plList) {
        this.ctx = context;
        this.playList = plList;
    }

    @Override
    public int getCount() {
        return playList.size();
    }

    @Override
    public Object getItem(int i) {
        return playList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void toggleSelection(int position) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position);
        } else {
            selectedItems.add(position);
        }
        notifyDataSetChanged();
    }

    public boolean isSelectionMode() {
        return !selectedItems.isEmpty();
    }

    public List<Integer> getSelectedItems() {
        return new ArrayList<>(selectedItems);
    }

    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(ctx).inflate(R.layout.item_playlist, viewGroup, false);
        }

        TextView title = view.findViewById(R.id.item_playlist_id);
        title.setText(playList.get(i));

        view.setBackgroundResource(R.drawable.bg_card_surface);
        int itemColor = selectedItems.contains(i)
                ? ContextCompat.getColor(ctx, R.color.selected_item_color)
                : ContextCompat.getColor(ctx, R.color.surface_card);
        view.getBackground().setTint(itemColor);

        return view;
    }
}
