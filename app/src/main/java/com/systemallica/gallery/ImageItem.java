package com.systemallica.gallery;

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

    String getTitle() {
        return title;
    }

}
