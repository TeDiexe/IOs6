package com.album;

public class AlbumLogger implements Observer {
    @Override
    public void update(String action, AlbumElement element) {
        System.out.println("[LOG] Zdarzenie: " + action +
                " | Element: " + element.getName());
    }
}