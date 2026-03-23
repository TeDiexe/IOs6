package com.album;

import java.util.ArrayList;
import java.util.List;

public class Album extends AbstractSubject implements AlbumElement {
    private String name;
    private List<AlbumElement> elements = new ArrayList<>();

    public Album(String name) {
        this.name = name;
    }

    public void addElement(AlbumElement element) {
        elements.add(element);
        notifyObservers("DODANO", element);
    }

    public void removeElement(AlbumElement element) {
        elements.remove(element);
        notifyObservers("USUNIĘTO", element);
    }

    public List<AlbumElement> getElements() {
        return elements;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override public void displayThumbnail() {}
    @Override public void displayFull() {}
    @Override public void displayDetails() {}
    @Override public String getName() { return name; }
}