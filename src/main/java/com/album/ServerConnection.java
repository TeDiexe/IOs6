package com.album;

public class ServerConnection {
    private static ServerConnection instance;
    private String serverAddress;

    private ServerConnection(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public static synchronized ServerConnection getInstance(String serverAddress) {
        if (instance == null) {
            instance = new ServerConnection(serverAddress);
        }
        return instance;
    }

    public void uploadPhoto(Photo photo) {
        System.out.println("Wysyłanie zdjęcia: " + photo + " na " + serverAddress);
    }

    public Photo downloadPhoto(String id) {
        System.out.println("Pobieranie zdjęcia o ID: " + id + ", z adresu: " + serverAddress);
        return null;
    }

    public String getServerAddress() {
        return serverAddress;
    }
}
