package com.example.androidproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidproject.model.Album;
import com.example.androidproject.model.Photo;
import com.example.androidproject.model.StorageManager;
import com.example.androidproject.model.Tag;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";
    private ArrayList<Album> albums;
    private List<Photo> searchResults;
    private SearchResultAdapter adapter;

    private Spinner spinnerType1, spinnerType2;
    private AutoCompleteTextView autoCompleteVal1, autoCompleteVal2;
    private RadioButton radioSingle, radioAnd, radioOr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        albums = StorageManager.loadAlbums(this);
        searchResults = new ArrayList<>();

        // 1. Setup UI
        spinnerType1 = findViewById(R.id.spinnerType1);
        spinnerType2 = findViewById(R.id.spinnerType2);
        autoCompleteVal1 = findViewById(R.id.autoCompleteVal1);
        autoCompleteVal2 = findViewById(R.id.autoCompleteVal2);
        radioSingle = findViewById(R.id.radioSingle);
        radioAnd = findViewById(R.id.radioAnd);
        radioOr = findViewById(R.id.radioOr);
        Button btnExecuteSearch = findViewById(R.id.btnExecuteSearch);
        GridView searchResultsGrid = findViewById(R.id.searchResultsGrid);

        // 2. Setup Spinners
        String[] types = {"Location", "Person"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        spinnerType1.setAdapter(spinnerAdapter);
        spinnerType2.setAdapter(spinnerAdapter);

        // 3. Setup AutoComplete Data
        setupAutoComplete();

        // 4. Setup Grid Adapter
        adapter = new SearchResultAdapter();
        searchResultsGrid.setAdapter(adapter);

        // 5. Setup Grid Click
        searchResultsGrid.setOnItemClickListener((parent, view, position, id) -> {
            Photo photo = searchResults.get(position);
            int albumIdx = -1;
            int photoIdx = -1;
            for (int i = 0; i < albums.size(); i++) {
                List<Photo> ps = albums.get(i).getPhotos();
                for (int j = 0; j < ps.size(); j++) {
                    if (ps.get(j).getUriString().equals(photo.getUriString())) {
                        albumIdx = i;
                        photoIdx = j;
                        break;
                    }
                }
                if (albumIdx != -1) break;
            }
            if (albumIdx != -1) {
                android.content.Intent intent = new android.content.Intent(this, PhotoDisplayActivity.class);
                intent.putExtra("ALBUM_INDEX", albumIdx);
                intent.putExtra("PHOTO_INDEX", photoIdx);
                startActivity(intent);
            }
        });

        // 6. Execute Search Logic
        btnExecuteSearch.setOnClickListener(v -> performSearch());
    }

    private void setupAutoComplete() {
        Set<String> allTagValues = new HashSet<>();

        for (Album album : albums) {
            for (Photo photo : album.getPhotos()) {
                for (Tag tag : photo.getTags()) {
                    allTagValues.add(tag.getValue());
                }
            }
        }

        List<String> tagValueList = new ArrayList<>(allTagValues);
        ArrayAdapter<String> autoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, tagValueList);

        autoCompleteVal1.setAdapter(autoAdapter);
        autoCompleteVal2.setAdapter(autoAdapter);
    }

    private void performSearch() {
        searchResults.clear();
        Set<String> seenUris = new HashSet<>(); // Prevent duplicate image results

        String type1 = spinnerType1.getSelectedItem().toString();
        String val1 = autoCompleteVal1.getText().toString().trim().toLowerCase();

        String type2 = spinnerType2.getSelectedItem().toString();
        String val2 = autoCompleteVal2.getText().toString().trim().toLowerCase();

        boolean isSingle = radioSingle.isChecked();
        boolean isAnd = radioAnd.isChecked();
        boolean isOr = radioOr.isChecked();

        if (val1.isEmpty()) {
            Toast.makeText(this, R.string.tag_1_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isSingle && val2.isEmpty()) {
            Toast.makeText(this, R.string.tag_2_required, Toast.LENGTH_SHORT).show();
            return;
        }

        for (Album album : albums) {
            for (Photo photo : album.getPhotos()) {
                if (seenUris.contains(photo.getUriString())) continue;

                boolean match1 = false;
                boolean match2 = false;

                for (Tag tag : photo.getTags()) {
                    // Substring match: Case insensitive startsWith check
                    if (tag.getName().equalsIgnoreCase(type1) && tag.getValue().toLowerCase().startsWith(val1)) {
                        match1 = true;
                    }
                    if (!isSingle && tag.getName().equalsIgnoreCase(type2) && tag.getValue().toLowerCase().startsWith(val2)) {
                        match2 = true;
                    }
                }

                boolean include = false;
                if (isSingle) {
                    include = match1;
                } else if (isAnd) {
                    include = match1 && match2;
                } else if (isOr) {
                    include = match1 || match2;
                }

                if (include) {
                    searchResults.add(photo);
                    seenUris.add(photo.getUriString());
                }
            }
        }

        if (searchResults.isEmpty()) {
            Toast.makeText(this, R.string.no_photos_found, Toast.LENGTH_SHORT).show();
        }
        adapter.notifyDataSetChanged();
    }

    // --- Custom Adapter for Search Results (reusing safe loading logic) ---
    private class SearchResultAdapter extends BaseAdapter {
        @Override
        public int getCount() { return searchResults.size(); }
        @Override
        public Object getItem(int position) { return searchResults.get(position); }
        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(SearchActivity.this).inflate(R.layout.grid_item_photo, parent, false);
            }

            ImageView imgThumbnail = convertView.findViewById(R.id.imgThumbnail);
            Photo photo = searchResults.get(position);
            Uri imageUri = Uri.parse(photo.getUriString());

            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                try (InputStream input = getContentResolver().openInputStream(imageUri)) {
                    if (input != null) {
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream(input, null, options);
                    }
                }

                options.inSampleSize = calculateInSampleSize(options);
                options.inJustDecodeBounds = false;

                try (InputStream input = getContentResolver().openInputStream(imageUri)) {
                    if (input != null) {
                        Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
                        imgThumbnail.setImageBitmap(bitmap);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading search result thumbnail", e);
            }

            return convertView;
        }

        private int calculateInSampleSize(BitmapFactory.Options options) {
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;
            
            final int reqWidth = 150;
            final int reqHeight = 150;

            if (height > reqHeight || width > reqWidth) {
                final int halfHeight = height / 2;
                final int halfWidth = width / 2;
                while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }
            return inSampleSize;
        }
    }
}