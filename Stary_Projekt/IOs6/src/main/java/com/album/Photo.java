package com.album;

import java.util.Date;

public class Photo implements AlbumElement {
    protected String id;
    protected String fileName;
    protected String format;
    protected int resolutionWidth;
    protected int resolutionHeight;
    protected Date createdDate;

    public Photo(String id, String fileName, String format, int resolutionWidth, int resolutionHeight, Date createdDate) {
        this.id = id;
        this.fileName = fileName;
        this.format = format;
        this.resolutionWidth = resolutionWidth;
        this.resolutionHeight = resolutionHeight;
        this.createdDate = createdDate;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void displayThumbnail() {}

    @Override
    public void displayFull() {}

    @Override
    public void displayDetails() {
        System.out.println("Zdjęcie: " + fileName + " (Format: " + format + ")");
    }

    @Override
    public String getName() {
        return fileName;
    }

    public String getId() { return id; }
    public String getFileName() { return fileName; }
    public String getFormat() { return format; }

    @Override
    public String toString() {
        return "Photo{" + "id='" + id + '\'' + ", fileName='" + fileName + '\'' + '}';
    }
}