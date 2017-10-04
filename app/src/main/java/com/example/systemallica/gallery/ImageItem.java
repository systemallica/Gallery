package com.example.systemallica.gallery;

import android.graphics.Bitmap;

class ImageItem {
    private Bitmap image;
    private String title;

    ImageItem(Bitmap image, String title) {
        super();
        this.image = image;
        this.title = title;
    }

    Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
