package com.album;
import java.util.UUID;
import java.util.Date;
import java.util.List;

public class PhotoFactory {
    public static RealPhoto create(String fileName, int width, int height, List<String> tags) {
        String id = UUID.randomUUID().toString();
        String format = extractFormat(fileName);
        Date now = new Date();

        if (tags == null || tags.isEmpty()) {
            System.out.println("Fabryka: Tworzę zwykłe zdjęcie.");
            return new RealPhoto(id, fileName, format, width, height, now);
        } else {
            System.out.println("Fabryka: Tworzę zdjęcie z tagami.");
            return new TaggedPhoto(id, fileName, format, width, height, now, tags);
        }
    }

    private static String extractFormat(String fileName) {
        int index = fileName.lastIndexOf('.');
        return (index == -1) ? "unknown" : fileName.substring(index + 1).toLowerCase();
    }
}