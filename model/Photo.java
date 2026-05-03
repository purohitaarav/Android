package com.example.photosapp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Photo implements Serializable {
    private static final long serialVersionUID = 1L;

    // Store as a String because android.net.Uri is NOT Serializable
    private String uriString;
    private List<Tag> tags;

    public Photo(String uriString) {
        this.uriString = uriString;
        this.tags = new ArrayList<>();
    }

    public String getUriString() { return uriString; }
    public List<Tag> getTags() { return tags; }

    public boolean addTag(String name, String value) {
        Tag newTag = new Tag(name, value);
        if (!tags.contains(newTag)) {
            tags.add(newTag);
            return true;
        }
        return false;
    }

    public boolean removeTag(String name, String value) {
        return tags.remove(new Tag(name, value));
    }

    // Helper to get the filename from the URI to use as the caption
    public String getFileName() {
        if (uriString != null) {
            int lastSlash = uriString.lastIndexOf('/');
            if (lastSlash != -1 && lastSlash < uriString.length() - 1) {
                return uriString.substring(lastSlash + 1);
            }
        }
        return "Unknown";
    }
}