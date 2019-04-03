package com.example.musicapp.models;

import android.graphics.Bitmap;

public class Song {

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

    public Song(String url,
                String title,
                String artist,
                String album,
                int firstYear,
                int lastYear,
                long duration,
                long size,
                String mimType) {
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

    public void setImage(Bitmap image) {
        this.image = image;
    }

}