package com.example.photosapp.model;

import android.content.Context;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class StorageManager {
    private static final String FILE_NAME = "photos_app_data.dat";

    public static void saveAlbums(Context context, ArrayList<Album> albums) {
        try {
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(albums);
            oos.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Album> loadAlbums(Context context) {
        ArrayList<Album> albums = new ArrayList<>();
        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            albums = (ArrayList<Album>) ois.readObject();
            ois.close();
            fis.close();
        } catch (Exception e) {
            // File might not exist on first run, return an empty list
            e.printStackTrace();
        }
        return albums;
    }
}