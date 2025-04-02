package com.samsung.playerindividual;

import android.hardware.camera2.CameraExtensionSession;

public class Song {
    private String name;
    private final String path;
    public Song(String name,String path){
        this.name = name;
        this.path = path;
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

}
