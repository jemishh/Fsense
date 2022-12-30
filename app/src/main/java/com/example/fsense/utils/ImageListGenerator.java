package com.example.fsense.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import com.example.fsense.models.ImageModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ImageListGenerator {


    List<ImageModel> imageModelList = new ArrayList<>();

    Uri collection;

    public List<ImageModel> getImageList(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.RELATIVE_PATH
        };
        String selection = MediaStore.Images.Media.SIZE +
                " >= ?";
        String[] selectionArgs = new String[]{
                String.valueOf(TimeUnit.SECONDS.convert(5, TimeUnit.MINUTES))
        };
        String sortOrder = MediaStore.Images.Media.DISPLAY_NAME + " DESC";

        try (Cursor cursor = context.getContentResolver().query(
                collection,
                projection,
                null,
                null,
                sortOrder
        )) {
            // Cache column indices.
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int nameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            int relativePathColumn= cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH);
            while (cursor.moveToNext()) {
                // Get values of columns for a given video.
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                String relativePath=cursor.getString(relativePathColumn);

                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI , id);

                // Stores column values and the contentUri in a local object
                // that represents the media file.
                imageModelList.add(new ImageModel(contentUri, name,relativePath));
            }

            return imageModelList;
        }
    }
}
