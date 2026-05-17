package com.album;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaggedPhoto extends RealPhoto {
    private List<String> tags;

    public TaggedPhoto(String id, String fileName, String format, int resolutionWidth, int resolutionHeight, Date createdDate, List<String> tags) {

        super(id, fileName, format, resolutionWidth, resolutionHeight, createdDate);
        this.tags = (tags != null) ? new ArrayList<>(tags) : new ArrayList<>();
    }

    public void addTag(String tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    public void removeTag(String tag) {
        tags.remove(tag);
    }

    public List<String> getTags() {
        return new ArrayList<>(tags);
    }

    @Override
    public String toString() {
        return "TaggedPhoto{" +
                "fileName='" + fileName + '\'' +
                ", tags=" + tags +
                '}';
    }
}