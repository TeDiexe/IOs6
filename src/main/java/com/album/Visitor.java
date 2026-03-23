package com.album;

public interface Visitor {
    void visit(Album album);
    void visit(RealPhoto photo);
    void visit(Photo photo);
    void visit(PhotoProxy proxy);
}