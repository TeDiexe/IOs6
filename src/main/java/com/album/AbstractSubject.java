package com.album;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSubject {
    protected List<Observer> observers = new ArrayList<>();

    public void attach(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void detach(Observer observer) {
        observers.remove(observer);
    }

    protected void notifyObservers(String action, AlbumElement element) {
        for (Observer observer : observers) {
            observer.update(action, element);
        }
    }
}