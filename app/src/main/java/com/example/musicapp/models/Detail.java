package com.example.musicapp.models;

public class Detail {

    private String headLine;
    private String description;

    public Detail(String headLine, String description) {
        this.headLine = headLine;
        this.description = description;
    }

    public String getHeadLine() {
        return headLine;
    }

    public String getDescription() {
        return description;
    }

}
