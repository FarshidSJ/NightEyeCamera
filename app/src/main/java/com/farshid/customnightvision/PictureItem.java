package com.farshid.customnightvision;


import android.net.Uri;

public class PictureItem {
    private Uri uri;
    private String name;
    private String extension;

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String date) {
        this.name = date;
    }


    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}
