package com.samsung.playerindividual;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.CameraExtensionSession;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

public class Song {
    private String name;
    private String defaultName;
    private final String path;
    private String artist;
    private String album;
    private Bitmap albumBitmap;
    private final int defaultBitmap = R.drawable.default_image;
    public Song(String defaultName,String path){
        this.defaultName = defaultName;
        this.path = path;
        getAudioMetadata(path);
    }

    public String getPath() {
        return path;

    }

    public String getName() {
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    private void getAudioMetadata(String filePath){
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            setName(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            setArtist(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            setAlbum(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));

            byte[] albumArt = retriever.getEmbeddedPicture();
            albumBitmap = null;
            if (albumArt != null) {
                albumBitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
            }
        } catch (Exception e) {
            setName("erropo");
            e.printStackTrace();
        } finally {
            try {
                retriever.release();  // Освобождаем ресурсы
            } catch (IOException e) {
                setName("ererer");
            }
        }
        setName(name != null ? name : defaultName);
        setArtist(artist != null ? artist : "Неизвестный исполнитель");
        setAlbum(album != null ? album : "Неизвестный альбом");

        if (albumBitmap != null) {
            setAlbumBitmap(albumBitmap);
        } else {
        }


    }

    public String getArtist() {
        return artist;
    }

    private void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    private void setAlbum(String album) {
        this.album = album;
    }

    public Bitmap getAlbumBitmap() {
        return albumBitmap;
    }

    public void setAlbumBitmap(Bitmap albumBitmap) {
        this.albumBitmap = albumBitmap;
    }
}
