package com.systemallica.gallery;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.pwittchen.swipe.library.Swipe;
import com.github.pwittchen.swipe.library.SwipeListener;

import java.io.File;
import java.util.ArrayList;

public class ImageActivity extends AppCompatActivity {

    private Swipe swipe;
    int position;
    File image;
    ArrayList<String> list_of_images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        // Listener for swipe actions
        setSwipeListener();

        Intent intent = getIntent();
        // Get file of image passed with the intent
        image = new File(intent.getStringExtra("image"));
        // Get position in ArrayList
        position = intent.getIntExtra("position", 0);
        // Get the other images in the folder
        list_of_images = intent.getStringArrayListExtra("list_of_images");

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

    @Override public boolean dispatchTouchEvent(MotionEvent event) {
        swipe.dispatchTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    public void setSwipeListener(){
        swipe = new Swipe();

        swipe.setListener(new SwipeListener() {
            @Override public void onSwipingLeft(final MotionEvent event) {

            }

            @Override public void onSwipedLeft(final MotionEvent event) {
                // Load next image to ImageView
                if (position < list_of_images.size()) {
                    position++;
                    GlideApp
                            .with(getApplicationContext())
                            .load(new File(list_of_images.get(position)))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into((ImageView)findViewById(R.id.image));
                }

            }

            @Override public void onSwipingRight(final MotionEvent event) {

            }

            @Override public void onSwipedRight(final MotionEvent event) {
                // Load previous image to ImageView
                if (position > 0) {
                    position--;
                    GlideApp
                            .with(getApplicationContext())
                            .load(new File(list_of_images.get(position)))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into((ImageView)findViewById(R.id.image));
                }
            }

            @Override public void onSwipingUp(final MotionEvent event) {

            }

            @Override public void onSwipedUp(final MotionEvent event) {

            }

            @Override public void onSwipingDown(final MotionEvent event) {

            }

            @Override public void onSwipedDown(final MotionEvent event) {

            }
        });
    }
}
