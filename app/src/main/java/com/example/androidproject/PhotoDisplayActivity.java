package com.example.androidproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androidproject.model.Album;
import com.example.androidproject.model.Photo;
import com.example.androidproject.model.StorageManager;
import com.example.androidproject.model.Tag;

import java.io.InputStream;
import java.util.ArrayList;

public class PhotoDisplayActivity extends AppCompatActivity {

    private static final String TAG = "PhotoDisplayActivity";
    private ArrayList<Album> albums;
    private int albumIndex;
    private int photoIndex;
    private Album currentAlbum;
    private Photo currentPhoto;

    private ImageView imgFullSize;
    private TextView tvPhotoTitle;
    private ListView listTags;
    private ArrayAdapter<Tag> tagAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_display);

        albumIndex = getIntent().getIntExtra("ALBUM_INDEX", -1);
        photoIndex = getIntent().getIntExtra("PHOTO_INDEX", -1);

        albums = StorageManager.loadAlbums(this);
        if (albumIndex == -1 || photoIndex == -1 || albumIndex >= albums.size()) {
            Toast.makeText(this, "Error loading photo", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentAlbum = albums.get(albumIndex);
        
        imgFullSize = findViewById(R.id.imgFullSize);
        tvPhotoTitle = findViewById(R.id.tvPhotoTitle);
        listTags = findViewById(R.id.listTags);

        Button btnBack = findViewById(R.id.btnBackToAlbum);
        btnBack.setOnClickListener(v -> finish());

        Button btnPrev = findViewById(R.id.btnPrev);
        btnPrev.setOnClickListener(v -> showPreviousPhoto());

        Button btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> showNextPhoto());

        Button btnAddTag = findViewById(R.id.btnAddTag);
        btnAddTag.setOnClickListener(v -> showAddTagDialog());

        Button btnRemoveTag = findViewById(R.id.btnRemoveTag);
        btnRemoveTag.setOnClickListener(v -> removeSelectedTag());

        updateUI();
    }

    private void updateUI() {
        if (photoIndex < 0 || photoIndex >= currentAlbum.getPhotos().size()) {
            return;
        }

        currentPhoto = currentAlbum.getPhotos().get(photoIndex);
        tvPhotoTitle.setText(currentPhoto.getFileName());

        // Load image
        Uri imageUri = Uri.parse(currentPhoto.getUriString());
        try (InputStream is = getContentResolver().openInputStream(imageUri)) {
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            imgFullSize.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.e(TAG, "Error loading full size image", e);
            imgFullSize.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        // Setup tags list
        tagAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, currentPhoto.getTags());
        listTags.setAdapter(tagAdapter);

        listTags.setOnItemLongClickListener((parent, view, position, id) -> {
            Tag tagToRemove = currentPhoto.getTags().get(position);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.remove_tag_confirm_title)
                    .setMessage(getString(R.string.remove_tag_confirm_msg, tagToRemove.toString()))
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        currentPhoto.getTags().remove(position);
                        StorageManager.saveAlbums(this, albums);
                        tagAdapter.notifyDataSetChanged();
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
            return true;
        });
    }

    private void showPreviousPhoto() {
        if (photoIndex > 0) {
            photoIndex--;
            updateUI();
        } else {
            Toast.makeText(this, "First photo reached", Toast.LENGTH_SHORT).show();
        }
    }

    private void showNextPhoto() {
        if (photoIndex < currentAlbum.getPhotos().size() - 1) {
            photoIndex++;
            updateUI();
        } else {
            Toast.makeText(this, "Last photo reached", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddTagDialog() {
        String[] types = {"person", "location"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_tag_type);
        builder.setItems(types, (dialog, which) -> {
            String selectedType = types[which];
            showTagValueDialog(selectedType);
        });
        builder.show();
    }

    private void showTagValueDialog(String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.enter_tag_value, type));

        final android.widget.EditText input = new android.widget.EditText(this);
        builder.setView(input);

        builder.setPositiveButton(R.string.add, (dialog, which) -> {
            String value = input.getText().toString().trim();
            if (!value.isEmpty()) {
                if (currentPhoto.addTag(type, value)) {
                    StorageManager.saveAlbums(this, albums);
                    tagAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(this, R.string.tag_already_exists, Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void removeSelectedTag() {
        Toast.makeText(this, R.string.remove_tag_instruction, Toast.LENGTH_SHORT).show();
    }
}