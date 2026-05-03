package com.example.photosapp.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Photo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String uriString; // path to the image
    private ArrayList<Tag> tags;

    // Constructor
    public Photo(String uriString) {
        this.uriString = uriString;
        this.tags = new ArrayList<>();
    }

    // Getter
    public String getUriString() {
        return uriString;
    }

    public ArrayList<Tag> getTags() {
        return tags;
    }

    // Add tag
    public void addTag(Tag tag) {
        if (tag == null) return;

        // prevent duplicates
        for (Tag t : tags) {
            if (t.equals(tag)) {
                return;
            }
        }
        tags.add(tag);
    }

    // Remove tag
    public void removeTag(Tag tag) {
        tags.remove(tag);
    }

    // Get tags by type (person/location)
    public ArrayList<Tag> getTagsByType(String type) {
        ArrayList<Tag> result = new ArrayList<>();
        for (Tag tag : tags) {
            if (tag.getType().equalsIgnoreCase(type)) {
                result.add(tag);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return uriString;
    }
}