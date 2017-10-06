package com.systemallica.gallery;

import android.graphics.Bitmap;

class ImageItem {
    private Bitmap image;
    private String title;
    private int    count;

    ImageItem(Bitmap image, String title, int count) {
        super();
        this.image = image;
        this.title = title;
        this.count = count;
    }

    Bitmap getImage() {
        return image;
    }

    String getTitle() {
        return title;
    }

    int getCount(){
        return count;
    }
}
