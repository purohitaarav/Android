package com.example.androidproject.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.androidproject.R;
import com.example.androidproject.model.Photo;

import java.util.List;

public class PhotoAdapter extends ArrayAdapter<Photo> {

    public PhotoAdapter(@NonNull Context context, @NonNull List<Photo> photos) {
        super(context, 0, photos);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_photo, parent, false);
        }

        Photo photo = getItem(position);
        ImageView imgPhoto = convertView.findViewById(R.id.imgPhoto);
        TextView txtCaption = convertView.findViewById(R.id.txtPhotoCaption);

        if (photo != null) {
            imgPhoto.setImageURI(Uri.parse(photo.getUriString()));
            txtCaption.setText(photo.getFileName());
        }

        return convertView;
    }
}
