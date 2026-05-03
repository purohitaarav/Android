package com.example.photosapp.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Album implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private ArrayList<Photo> photos;

    // Constructor
    public Album(String name) {
        this.name = name;
        this.photos = new ArrayList<>();
    }

    // Getters
    public String getName() {
        return name;
    }

    public ArrayList<Photo> getPhotos() {
        return photos;
    }

    // Setter
    public void setName(String name) {
        this.name = name;
    }

    // Add photo
    public void addPhoto(Photo photo) {
        if (photo != null && !photos.contains(photo)) {
            photos.add(photo);
        }
    }

    // Remove photo
    public void removePhoto(Photo photo) {
        photos.remove(photo);
    }

    // Get photo by index
    public Photo getPhoto(int index) {
        if (index >= 0 && index < photos.size()) {
            return photos.get(index);
        }
        return null;
    }

    // Number of photos
    public int size() {
        return photos.size();
    }

    // Check if empty
    public boolean isEmpty() {
        return photos.isEmpty();
    }

    @Override
    public String toString() {
        return name;
    }
}