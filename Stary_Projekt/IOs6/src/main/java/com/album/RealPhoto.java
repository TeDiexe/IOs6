package com.album;

import java.util.Date;

public class RealPhoto implements AlbumElement {
    protected String id;
    protected String fileName;
    protected String format;
    protected int resolutionWidth;
    protected int resolutionHeight;
    protected Date createdDate;

    public RealPhoto(String id, String fileName, String format, int width, int height, Date date) {
        this.id = id;
        this.fileName = fileName;
        this.format = format;
        this.resolutionWidth = width;
        this.resolutionHeight = height;
        this.createdDate = date;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override public void displayThumbnail() {}
    @Override public void displayFull() {}
    @Override public void displayDetails() {}
    @Override public String getName() { return fileName; }

    public String getFormat() { return format; }
    public int getResolutionWidth() { return resolutionWidth; }
}