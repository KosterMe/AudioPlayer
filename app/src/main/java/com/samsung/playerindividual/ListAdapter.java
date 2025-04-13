package com.samsung.playerindividual;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapter extends BaseAdapter {
    private ArrayList<Song> SongList;
    private Context ctx;

    public ListAdapter(Context context, ArrayList<Song> songList) {
        this.ctx = context;
        SongList = songList;
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
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(ctx).inflate(R.layout.item_song, null);
        }

        TextView tn = view.findViewById(R.id.item_name);
        Song thisSong = SongList.get(i);
        tn.setText(thisSong.getName());
        TextView ta = view.findViewById(R.id.item_artist);
        ta.setText(thisSong.getArtist());
        ImageView album = view.findViewById(R.id.item_ImageView);
        if (thisSong.getAlbumBitmap() != null) {
            album.setImageBitmap(thisSong.getAlbumBitmap());
        } else {
            album.setImageResource(R.drawable.default_image_for_item);
        }
        return view;
    }
}
