package com.example.photosapp.model;

import java.io.Serializable;
import java.util.Objects;

public class Tag implements Serializable {

    private static final long serialVersionUID = 1L;

    private String type;   // "person" or "location"
    private String value;

    // Constructor
    public Tag(String type, String value) {
        this.type = type.toLowerCase();
        this.value = value.toLowerCase();
    }

    // Getters
    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    // Setters
    public void setType(String type) {
        this.type = type.toLowerCase();
    }

    public void setValue(String value) {
        this.value = value.toLowerCase();
    }

    // Equality (important for preventing duplicates)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;
        Tag tag = (Tag) o;
        return type.equals(tag.type) && value.equals(tag.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public String toString() {
        return type + ": " + value;
    }
}