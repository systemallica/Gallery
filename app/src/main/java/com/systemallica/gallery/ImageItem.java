package com.systemallica.gallery;

class ImageItem {
    private String imagePath;
    private String title;

    ImageItem(String imagePath, String title) {
        super();
        this.imagePath = imagePath;
        this.title = title;
    }

    String getImage() {
        return imagePath;
    }

    public void setImage(String image) {
        this.imagePath = image;
    }

    String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
