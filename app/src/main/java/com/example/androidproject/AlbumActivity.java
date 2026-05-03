package com.example.androidproject;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androidproject.model.Album;
import com.example.androidproject.model.Photo;
import com.example.androidproject.model.StorageManager;

import java.io.InputStream;
import java.util.ArrayList;

public class AlbumActivity extends AppCompatActivity {

    private static final String TAG = "AlbumActivity";
    private ArrayList<Album> albums;
    private int albumIndex;
    private Album currentAlbum;
    private PhotoAdapter adapter;

    // Modern way to handle Activity Results (replacing deprecated startActivityForResult)
    private final ActivityResultLauncher<Intent> photoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        // CRITICAL: Request persistable permission so we can access this URI after app restarts
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        String uriString = uri.toString();

                        // Check for duplicates in the current album
                        boolean exists = false;
                        for (Photo p : currentAlbum.getPhotos()) {
                            if (p.getUriString().equals(uriString)) {
                                exists = true;
                                break;
                            }
                        }

                        if (!exists) {
                            currentAlbum.addPhoto(new Photo(uriString));
                            saveAndRefresh();
                        } else {
                            Toast.makeText(this, "Photo is already in this album.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        // 1. Get the album index passed from MainActivity
        albumIndex = getIntent().getIntExtra("ALBUM_INDEX", -1);
        albums = StorageManager.loadAlbums(this);

        if (albumIndex == -1 || albumIndex >= albums.size()) {
            Toast.makeText(this, "Error loading album", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentAlbum = albums.get(albumIndex);

        // 2. Setup UI
        TextView tvAlbumName = findViewById(R.id.tvAlbumName);
        tvAlbumName.setText(currentAlbum.getName());

        Button btnAddPhoto = findViewById(R.id.btnAddPhoto);
        btnAddPhoto.setOnClickListener(v -> openGallery());

        GridView photoGridView = findViewById(R.id.photoGridView);
        adapter = new PhotoAdapter();
        photoGridView.setAdapter(adapter);

        // 3. Setup Clicks
        photoGridView.setOnItemClickListener((parent, view, position, id) -> openPhotoDisplay(position));

        photoGridView.setOnItemLongClickListener((parent, view, position, id) -> {
            showPhotoOptionsDialog(position);
            return true;
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        photoPickerLauncher.launch(intent);
    }

    private void showPhotoOptionsDialog(int position) {
        String[] options = {getString(R.string.remove_photo), getString(R.string.move_photo), getString(R.string.copy_photo)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.photo_options);
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                currentAlbum.getPhotos().remove(position);
                saveAndRefresh();
            } else if (which == 1) {
                showMoveCopyDialog(position, true);
            } else if (which == 2) {
                showMoveCopyDialog(position, false);
            }
        });
        builder.show();
    }

    private void showMoveCopyDialog(int photoPosition, boolean isMove) {
        Photo photo = currentAlbum.getPhotos().get(photoPosition);
        ArrayList<String> albumNames = new ArrayList<>();
        ArrayList<Integer> albumIndices = new ArrayList<>();

        for (int i = 0; i < albums.size(); i++) {
            if (i != albumIndex) {
                albumNames.add(albums.get(i).getName());
                albumIndices.add(i);
            }
        }

        if (albumNames.isEmpty()) {
            Toast.makeText(this, R.string.no_other_albums, Toast.LENGTH_SHORT).show();
            return;
        }

        String[] namesArray = albumNames.toArray(new String[0]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle((isMove ? getString(R.string.move_to_album) : getString(R.string.copy_to_album)));
        builder.setItems(namesArray, (dialog, which) -> {
            int targetAlbumIndex = albumIndices.get(which);
            Album targetAlbum = albums.get(targetAlbumIndex);

            // Check for duplicates in target album
            boolean exists = false;
            for (Photo p : targetAlbum.getPhotos()) {
                if (p.getUriString().equals(photo.getUriString())) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                targetAlbum.addPhoto(photo);
                if (isMove) {
                    currentAlbum.getPhotos().remove(photoPosition);
                }
                saveAndRefresh();
                Toast.makeText(this, (isMove ? R.string.moved_success : R.string.copied_success), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.photo_exists_target, Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }


    private void openPhotoDisplay(int photoIndex) {
        // Phase 3.5: We will build PhotoDisplayActivity next to handle the Slideshow
        Intent intent = new Intent(AlbumActivity.this, PhotoDisplayActivity.class);
        intent.putExtra("ALBUM_INDEX", albumIndex);
        intent.putExtra("PHOTO_INDEX", photoIndex);
        startActivity(intent);
    }

    private void saveAndRefresh() {
        StorageManager.saveAlbums(this, albums);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload in case tags or photos changed in the slideshow view
        albums = StorageManager.loadAlbums(this);
        currentAlbum = albums.get(albumIndex);
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    // --- Custom Adapter for loading Thumbnails safely ---
    private class PhotoAdapter extends BaseAdapter {

        @Override
        public int getCount() { return currentAlbum.getPhotos().size(); }

        @Override
        public Object getItem(int position) { return currentAlbum.getPhotos().get(position); }

        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(AlbumActivity.this).inflate(R.layout.grid_item_photo, parent, false);
            }

            ImageView imgThumbnail = convertView.findViewById(R.id.imgThumbnail);
            Photo photo = currentAlbum.getPhotos().get(position);
            Uri imageUri = Uri.parse(photo.getUriString());

            try {
                // Decode bounds first to avoid loading huge images into memory
                BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
                try (InputStream input = getContentResolver().openInputStream(imageUri)) {
                    if (input != null) {
                        onlyBoundsOptions.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
                    }
                }

                // Calculate sample size (scale down by powers of 2)
                onlyBoundsOptions.inSampleSize = calculateInSampleSize(onlyBoundsOptions);
                onlyBoundsOptions.inJustDecodeBounds = false;

                // Decode actual bitmap
                try (InputStream input = getContentResolver().openInputStream(imageUri)) {
                    if (input != null) {
                        Bitmap bitmap = BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
                        imgThumbnail.setImageBitmap(bitmap);
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Error loading thumbnail", e);
                // Optionally set a fallback error image here
            }

            return convertView;
        }

        private int calculateInSampleSize(BitmapFactory.Options options) {
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > 150 || width > 150) {
                final int halfHeight = height / 2;
                final int halfWidth = width / 2;
                while ((halfHeight / inSampleSize) >= 150 && (halfWidth / inSampleSize) >= 150) {
                    inSampleSize *= 2;
                }
            }
            return inSampleSize;
        }
    }
}