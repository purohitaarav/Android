package com.example.androidproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androidproject.model.Album;
import com.example.androidproject.model.StorageManager;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Album> albums;
    private ArrayAdapter<Album> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Load data from previous session
        albums = StorageManager.loadAlbums(this);
        // 2. Set up the UI components
        ListView albumListView = findViewById(R.id.albumListView);
        Button btnAddAlbum = findViewById(R.id.btnAddAlbum);
        Button btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });
        
        // 3. Set up the adapter (uses standard Android plain text list item)
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, albums);
        albumListView.setAdapter(adapter);

        // 4. Button Click: Create Album
        btnAddAlbum.setOnClickListener(v -> showCreateAlbumDialog());

        // 5. Short Click: Open Album
        albumListView.setOnItemClickListener((parent, view, position, id) -> openAlbum(position));

        // 6. Long Click: Rename or Delete Album
        albumListView.setOnItemLongClickListener((parent, view, position, id) -> {
            showAlbumOptionsDialog(position);
            return true; // Indicates the long click was consumed
        });
    }

    private void showCreateAlbumDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.create_new_album);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton(R.string.create, (dialog, which) -> {
            String albumName = input.getText().toString().trim();
            if (albumName.isEmpty()) {
                Toast.makeText(this, R.string.album_name_empty, Toast.LENGTH_SHORT).show();
                return;
            }
            // Check for duplicates
            for (Album a : albums) {
                if (a.getName().equalsIgnoreCase(albumName)) {
                    Toast.makeText(this, R.string.album_exists, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            albums.add(new Album(albumName));
            saveAndRefresh();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showAlbumOptionsDialog(int position) {
        String[] options = {getString(R.string.rename_album), getString(R.string.delete_album)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.album_options);
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                showRenameAlbumDialog(position);
            } else if (which == 1) {
                deleteAlbum(position);
            }
        });
        builder.show();
    }

    private void showRenameAlbumDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.rename_album);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(albums.get(position).getName()); // Pre-fill current name
        builder.setView(input);

        builder.setPositiveButton(R.string.rename, (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(this, R.string.album_name_empty, Toast.LENGTH_SHORT).show();
                return;
            }
            // Check for duplicates (excluding itself)
            for (int i = 0; i < albums.size(); i++) {
                if (i != position && albums.get(i).getName().equalsIgnoreCase(newName)) {
                    Toast.makeText(this, R.string.album_exists, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            albums.get(position).setName(newName);
            saveAndRefresh();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void deleteAlbum(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_album);
        builder.setMessage(getString(R.string.delete_confirm_msg, albums.get(position).getName()));
        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            albums.remove(position);
            saveAndRefresh();
        });
        builder.setNegativeButton(R.string.no, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void openAlbum(int position) {
        // We will build AlbumActivity next. We pass the index of the album so the next
        // screen knows which album's photos to display.
        Intent intent = new Intent(MainActivity.this, AlbumActivity.class);
        intent.putExtra("ALBUM_INDEX", position);
        startActivity(intent);
    }

    private void saveAndRefresh() {
        StorageManager.saveAlbums(this, albums);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list when coming back from AlbumActivity (in case photos were added/deleted)
        albums = StorageManager.loadAlbums(this);
        if (adapter != null) {
            adapter.clear();
            adapter.addAll(albums);
            adapter.notifyDataSetChanged();
        }
    }
}