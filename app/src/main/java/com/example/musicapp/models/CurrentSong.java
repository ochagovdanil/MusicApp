package com.example.musicapp.models;

import android.graphics.Bitmap;

public class CurrentSong {

    private int position; // a position of an item in the list
    private Bitmap image;
    private String url;
    private String title;
    private String artist;
    private String album;
    private int firstYear;
    private int lastYear;
    private long duration;
    private long size;
    private String mimType;

    public CurrentSong(int position,
                       Bitmap image,
                       String url,
                       String title,
                       String artist,
                       String album,
                       int firstYear,
                       int lastYear,
                       long duration,
                       long size,
                       String mimType) {
        this.position = position;
        this.image = image;
        this.url = url;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.firstYear = firstYear;
        this.lastYear = lastYear;
        this.duration = duration;
        this.size = size;
        this.mimType = mimType;
    }

    public int getPosition() {
        return position;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public int getFirstYear() {
        return firstYear;
    }

    public int getLastYear() {
        return lastYear;
    }

    public long getDuration() {
        return duration;
    }

    public long getSize() {
        return size;
    }

    public String getMimType() {
        return mimType;
    }

}
