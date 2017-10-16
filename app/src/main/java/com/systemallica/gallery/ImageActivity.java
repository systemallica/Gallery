package com.systemallica.gallery;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;

public class ImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        // Get file of image passed with the intent
        Intent intent = getIntent();
        File image = new File(intent.getStringExtra("image"));

        if (getSupportActionBar() != null) {
            // Set title to image name
            getSupportActionBar().setTitle(image.getName());
            // Display arrow to return to previous activity
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        GlideApp
                .with(this)
                .load(image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into((ImageView)findViewById(R.id.image));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
