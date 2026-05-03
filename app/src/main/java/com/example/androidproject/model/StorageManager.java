package com.example.androidproject.model;

import android.content.Context;
import android.util.Log;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class StorageManager {
    private static final String FILE_NAME = "photos_app_data.dat";
    private static final String TAG = "StorageManager";

    public static void saveAlbums(Context context, ArrayList<Album> albums) {
        try {
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(albums);
            oos.close();
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "Error saving albums", e);
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
            Log.w(TAG, "Could not load albums (file might not exist yet)");
        }
        return albums;
    }
}