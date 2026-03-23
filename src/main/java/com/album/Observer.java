package com.album;

public interface Observer {
    void update(String action, AlbumElement element);
}