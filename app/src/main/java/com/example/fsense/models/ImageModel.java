package com.example.fsense.models;

import android.net.Uri;

public class ImageModel {
    private final Uri uri;
    private final String name;
    private final String relativePath;

    public ImageModel(Uri uri, String name, String relativePath) {
        this.uri = uri;
        this.name = name;
        this.relativePath=relativePath;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public Uri getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

}
