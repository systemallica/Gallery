package com.systemallica.gallery;

import java.io.File;

class ImageItem {
    private File image;
    private String title;
    private int    count;

    ImageItem(File image, String title, int count) {
        super();
        this.image = image;
        this.title = title;
        this.count = count;
    }

    File getImage() {
        return image;
    }

    String getTitle() {
        return title;
    }

    int getCount(){
        return count;
    }
}
