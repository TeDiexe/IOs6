package com.album;

public class PhotoProxy implements AlbumElement {
    private String filename;
    private int height, width;
    private RealPhoto realPhoto;

    public PhotoProxy(String filename) {
        this.filename = filename;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void displayThumbnail() {
        System.out.println("Thumbnail of photo: " + filename);
    }

    @Override
    public void displayFull() {
        if (realPhoto == null) {
            realPhoto = PhotoFactory.create(filename, width, height, null);
        }
        realPhoto.displayFull();
    }

    @Override
    public void displayDetails() {
    }

    @Override
    public String getName() {
        return filename;
    }
}