package com.systemallica.gallery;

import java.io.File;

class FolderItem {
    private File image;
    private String title;
    private int    count;

    FolderItem(File image, String title, int count) {
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
