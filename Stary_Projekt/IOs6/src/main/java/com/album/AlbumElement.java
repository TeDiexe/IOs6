package com.album;

public interface AlbumElement {
    void displayThumbnail();
    void displayFull();
    void displayDetails();
    String getName();

    void accept(Visitor visitor);
}