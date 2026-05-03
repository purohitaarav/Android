package com.example.photosapp.storage;

import android.content.Context;

import com.example.photosapp.model.Album;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class DataManager {

    private static final String FILE_NAME = "albums.dat";

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

    public static ArrayList<Album> loadAlbums(Context context) {
        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            ObjectInputStream ois = new ObjectInputStream(fis);

            ArrayList<Album> albums = (ArrayList<Album>) ois.readObject();

            ois.close();
            fis.close();

            return albums;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}