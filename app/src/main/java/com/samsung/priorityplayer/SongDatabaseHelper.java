package com.samsung.priorityplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class SongDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "songs.db";

    // SONGS table
    private static final String SONGS_TABLE = "SONGS";
    private static final String SONG_ID = "ID";
    private static final String SONG_NAME = "SONG_NAME";
    private static final String SONG_PATH = "SONG_PATH";
    private static final String SONG_UPDATED_AT = "SONG_UPDATED_AT";
    private static final String SONG_PRIORITY = "PRIORITY";
    private static final String SONG_DELETED = "DELETED";

    // LINKS table
    private static final String LINKS_TABLE = "LINKS";
    private static final String LINK_PLAYLIST_NAME = "PLAYLIST_NAME";
    private static final String LINK_SONG_PATH = "SONG_PATH";

    // PLAYLIST table
    private static final String PLAYLIST_TABLE = "PLAYLIST";
    private static final String PL_ID = "PL_COLUMN_ID";
    private static final String PL_NAME = "PLAYLIST_NAME";

    public SongDatabaseHelper(@Nullable Context context, int version) {
        super(context, DATABASE_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + SONGS_TABLE + " (" +
                SONG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SONG_NAME + " TEXT NOT NULL, " +
                SONG_PATH + " TEXT UNIQUE NOT NULL, " +
                SONG_PRIORITY + " INTEGER NOT NULL DEFAULT 50, " +
                SONG_UPDATED_AT + " INTEGER, " +
                "artist TEXT, " +
                "album TEXT, " +
                "duration INTEGER, " +
                "album_art BLOB, " +
                "changeable INTEGER, " +
                SONG_DELETED + " INTEGER" + // Добавляем поле для хранения обложки в виде BLOB
                ");");

        db.execSQL("CREATE TABLE " + LINKS_TABLE + " (" +
                LINK_PLAYLIST_NAME + " " + "TEXT NOT NULL, " +
                LINK_SONG_PATH + " TEXT NOT NULL);");

        db.execSQL("CREATE TABLE " + PLAYLIST_TABLE + " (" +
                PL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PL_NAME + " TEXT NOT NULL UNIQUE);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SONGS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + LINKS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + PLAYLIST_TABLE);
        onCreate(db);
    }

    public void insertSong(Song song) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SONG_NAME, song.getName());
        values.put(SONG_PATH, song.getPath());
        values.put(SONG_PRIORITY, song.getPriority());
        values.put(SONG_UPDATED_AT, System.currentTimeMillis());
        values.put("artist", song.getArtist());
        values.put("album", song.getAlbum());
        values.put("duration", song.getDuration());
        values.put("changeable", song.isChangeable() ? 1 : 0);
        values.put(SONG_DELETED,0);

        if (song.getAlbumBitmap() != null) {
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                song.getAlbumBitmap().compress(Bitmap.CompressFormat.JPEG, 10, stream);
                values.put("album_art", stream.toByteArray());
            } catch (Exception e){

            }

        }
        Log.i("Scan","update  " + System.currentTimeMillis());

        db.insertWithOnConflict(SONGS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }
    public void updateSong(String path, int currentTime){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SONG_UPDATED_AT, currentTime);
        db.update(SONGS_TABLE, values, SONG_PATH + "=?", new String[]{path});
        Log.i("Scan","update  " + currentTime);
    }
    public void clearDB(int currentTime) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                SONGS_TABLE,
                new String[]{SONG_PATH, SONG_UPDATED_AT},
                SONG_UPDATED_AT + " != ?",
                new String[]{String.valueOf(currentTime)},
                null, null, null
        );

        while (cursor.moveToNext()) {
            String path = cursor.getString(cursor.getColumnIndexOrThrow(SONG_PATH));
            deleteSongByPath(path); // Удаляем песню и связанные данные
        }
        cursor.close();
    }


    public void updateChangeable(String path, boolean changeable) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("changeable", changeable ? 1 : 0);
        db.update(SONGS_TABLE, values, SONG_PATH + "=?", new String[]{path});
    }

    public void renameSong(String path, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SONG_NAME, newName);
        int updated = db.update(SONGS_TABLE, values, SONG_PATH + "=?", new String[]{path});
        Log.i("rename_debug", "Rows updated: " + updated);
    }

    public boolean songExists(String path) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                SONGS_TABLE,
                new String[]{SONG_PATH},
                SONG_PATH + "=? AND " + SONG_DELETED + "=0",
                new String[]{path},
                null, null, null
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }


    public void deleteSongByPath(String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SONG_DELETED, 1);
        db.update(SONGS_TABLE,values, SONG_PATH + "=?", new String[]{path});
        db.delete(LINKS_TABLE, SONG_PATH + "=?", new String[]{path});
    }
    public Song getSongByPath(String path) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                SONGS_TABLE,
                new String[]{SONG_NAME, SONG_PATH, SONG_PRIORITY, "artist", "album", "duration", "album_art", "changeable"},
                SONG_PATH + "=? AND " + SONG_DELETED + "=0",
                new String[]{path},
                null, null, null
        );

        Song song = null;

        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(SONG_NAME));
            int priority = cursor.getInt(cursor.getColumnIndexOrThrow(SONG_PRIORITY));
            String artist = cursor.getString(cursor.getColumnIndexOrThrow("artist"));
            String album = cursor.getString(cursor.getColumnIndexOrThrow("album"));
            int duration = cursor.getInt(cursor.getColumnIndexOrThrow("duration"));
            byte[] albumArtBytes = cursor.getBlob(cursor.getColumnIndexOrThrow("album_art"));
            boolean changeable = cursor.getInt(cursor.getColumnIndexOrThrow("changeable")) == 1;
            Bitmap albumArt = albumArtBytes != null ? BitmapFactory.decodeByteArray(albumArtBytes, 0, albumArtBytes.length) : null;

            song = new Song(name, path, artist, album, duration, albumArt, priority, changeable);
        }

        cursor.close();
        return song;
    }


    public void updatePriority(String path, int newPriority) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SONG_PRIORITY, newPriority);
        values.put(SONG_UPDATED_AT, System.currentTimeMillis());
        db.update(SONGS_TABLE, values, SONG_PATH + "=?", new String[]{path});
    }

    public ArrayList<Song> getAllSongs() {
        ArrayList<Song> songs = new ArrayList<>();
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query(
                     SONGS_TABLE,
                     new String[]{SONG_NAME, SONG_PATH, SONG_PRIORITY, "artist", "album", "duration", "album_art", "changeable"},
                     SONG_DELETED + "=0",
                     null,
                     null,
                     null,
                     null
             )) {

            int nameIndex = cursor.getColumnIndexOrThrow(SONG_NAME);
            int pathIndex = cursor.getColumnIndexOrThrow(SONG_PATH);
            int priorityIndex = cursor.getColumnIndexOrThrow(SONG_PRIORITY);
            int artistIndex = cursor.getColumnIndexOrThrow("artist");
            int albumIndex = cursor.getColumnIndexOrThrow("album");
            int durationIndex = cursor.getColumnIndexOrThrow("duration");
            int albumArtIndex = cursor.getColumnIndexOrThrow("album_art");
            int changeableIndex = cursor.getColumnIndexOrThrow("changeable");

            while (cursor.moveToNext()) {
                String name = cursor.getString(nameIndex);
                String path = cursor.getString(pathIndex);
                int priority = cursor.getInt(priorityIndex);
                String artist = cursor.getString(artistIndex);
                String album = cursor.getString(albumIndex);
                int duration = cursor.getInt(durationIndex);
                byte[] albumArtByteArray = cursor.getBlob(albumArtIndex);
                Bitmap albumArt = albumArtByteArray != null ? BitmapFactory.decodeByteArray(albumArtByteArray, 0, albumArtByteArray.length) : null;
                boolean changeable = cursor.getInt(changeableIndex) == 1;

                songs.add(new Song(name, path, artist, album, duration, albumArt, priority, changeable));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return songs;
    }

    public List<Song> getSongsByPlaylistName(String pl_name) {
        List<Song> songs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT s." + SONG_ID + ", s." + SONG_NAME + ", s." + SONG_PATH + ", s." + SONG_PRIORITY +
                ", s.artist, s.album, s.duration, s.album_art, s.changeable" +
                " FROM " + SONGS_TABLE + " s " +
                "JOIN " + LINKS_TABLE + " l ON s." + SONG_PATH + " = l." + LINK_SONG_PATH +
                " WHERE l." + LINK_PLAYLIST_NAME + " = ? AND s." + SONG_DELETED + " = 0";

        Cursor cursor = db.rawQuery(query, new String[]{pl_name});

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(SONG_NAME));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(SONG_PATH));
                int priority = cursor.getInt(cursor.getColumnIndexOrThrow(SONG_PRIORITY));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow("artist"));
                String album = cursor.getString(cursor.getColumnIndexOrThrow("album"));
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow("duration"));
                byte[] albumArtByteArray = cursor.getBlob(cursor.getColumnIndexOrThrow("album_art"));
                int changeableIndex = cursor.getColumnIndexOrThrow("changeable");
                Bitmap albumArt = albumArtByteArray != null ? BitmapFactory.decodeByteArray(albumArtByteArray, 0, albumArtByteArray.length) : null;
                boolean changeable = cursor.getInt(changeableIndex) == 1;
                songs.add(new Song(name, path, artist, album, duration, albumArt, priority, changeable));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return songs;
    }

    public void insertLink(String pl_name, String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(
                LINKS_TABLE,
                new String[]{LINK_SONG_PATH},
                LINK_PLAYLIST_NAME + "=? AND " + LINK_SONG_PATH + "=?",
                new String[]{pl_name, path},
                null, null, null
        );
        if (cursor.moveToFirst()) {
            cursor.close();
            return;
        }
        ContentValues values = new ContentValues();
        values.put(LINK_PLAYLIST_NAME, pl_name);
        values.put(LINK_SONG_PATH, path);
        db.insertWithOnConflict(LINKS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        cursor.close();
    }

    public void deleteLink(String pl_name, String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(LINKS_TABLE, LINK_PLAYLIST_NAME + "=? AND " + LINK_SONG_PATH + "=?", new String[]{pl_name, path});
    }

    public boolean insertPL(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(
                PLAYLIST_TABLE,
                new String[]{PL_ID},
                PL_NAME + "=?",
                new String[]{name},
                null, null, null
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();

        if (exists) {
            Log.i("insertPL", "PL already exists: " + name);
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(PL_NAME, name);
        long result = db.insert(PLAYLIST_TABLE, null, values);

        boolean success = result != -1;
        Log.i("insertPL", "PL add: " + name + " → " + success);
        return success;
    }

    public boolean deletePL(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        int deletedRows = db.delete(PLAYLIST_TABLE, PL_NAME + "=?", new String[]{name});
        db.delete(LINKS_TABLE, LINK_PLAYLIST_NAME + "=?", new String[]{name});
        return deletedRows > 0;
    }

    public boolean renamePL(String oldName, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(PLAYLIST_TABLE, null, PL_NAME + "=?", new String[]{newName}, null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        if (exists) {
            Log.w("renamePL", "Name already taken: " + newName);
            return false;
        }

        ContentValues plValues = new ContentValues();
        plValues.put(PL_NAME, newName);
        int updatedPL = db.update(PLAYLIST_TABLE, plValues, PL_NAME + "=?", new String[]{oldName});

        ContentValues linkValues = new ContentValues();
        linkValues.put(LINK_PLAYLIST_NAME, newName);
        db.update(LINKS_TABLE, linkValues, LINK_PLAYLIST_NAME + "=?", new String[]{oldName});

        Log.i("renamePL", "Renamed " + oldName + " → " + newName);
        return updatedPL > 0;
    }

    public ArrayList<String> getAllPLName() {
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + PL_NAME + " FROM " + PLAYLIST_TABLE, null);
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(0);
                list.add(name);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}
