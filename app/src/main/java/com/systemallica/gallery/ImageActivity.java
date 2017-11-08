package com.systemallica.gallery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.util.ArrayList;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class ImageActivity extends AppCompatActivity {

    ArrayList<String> list_of_images = new ArrayList<>();
    ArrayList<Integer> files_to_delete = new ArrayList<>();
    PagerAdapter mPagerAdapter;
    ViewPager mPager;
    int positionArray;

    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {

            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        // Get all the images in the folder
        list_of_images = intent.getStringArrayListExtra("list_of_images");
        // Get position
        int position_intent = intent.getIntExtra("position", 0);
        // Instantiate a ViewPager and a PagerAdapter.
        mPagerAdapter = new CustomPagerAdapter(this);
        mPager = findViewById(R.id.pager);

        // Set the adapter
        mPager.setAdapter(mPagerAdapter);
        // Set current position
        mPager.setCurrentItem(position_intent);

        if (getSupportActionBar() != null) {
            // Display arrow to return to previous activity
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Hide it by default
            getSupportActionBar().hide();
        }

        // Set result to 0 -> No file deleted
        setResult(0);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(UI_ANIMATION_DELAY);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.post(mHidePart2Runnable);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    class CustomPagerAdapter extends PagerAdapter {

        Context mContext;

        CustomPagerAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return list_of_images.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == (object);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {


            File image = new File(list_of_images.get(position));

            // Inflate layout
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.view_pager_item, container, false);
            // Get view
            PhotoView photoView = layout.findViewById(R.id.inside_imageview);
            ImageView overlay = layout.findViewById(R.id.outside_imageview);

            // If it's a video, add overlay
            if(Utils.isVideo(image.getName())){
                overlay.setVisibility(View.VISIBLE);
            }else{
                overlay.setVisibility(View.INVISIBLE);
            }

            GlideApp
                    .with(getApplicationContext())
                    .load(image)
                    .transition(withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(photoView);

            container.addView(layout);

            // Set up the user interaction to manually show or hide the system UI.
            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggle();
                }
            });


            return layout;

            //TODO: set title on page change
            // Set title to image name-> should be done on page change
            //if(getSupportActionBar()!= null) {
            //    getSupportActionBar().setTitle(image.getName());
            //}

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id){
            case R.id.action_share:
                positionArray = mPager.getCurrentItem();
                shareImage(new File(list_of_images.get(positionArray)));
                return true;

            case R.id.action_delete:
                positionArray = mPager.getCurrentItem();
                deleteImage(new File(list_of_images.get(positionArray)));
                return true;

            case R.id.action_details:
                //TODO
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareImage(File image){
        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(image));
        startActivity(Intent.createChooser(shareIntent, "Share image using"));
    }

    private void deleteImage(final File image){
        runOnUiThread(new Runnable() {
            public void run() {

                AlertDialog.Builder builder = new AlertDialog.Builder(ImageActivity.this);
                builder.setTitle(R.string.delete_title)
                        .setMessage(R.string.delete_message)
                        .setIcon(R.drawable.ic_warning_black_24dp)
                        .setPositiveButton(R.string.delete_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (image.exists()) {
                                    // Remove file from device
                                    if (image.delete()) {
                                        int move_to;
                                        // Add deleted file position to ArrayList and send it as Extra
                                        files_to_delete.add(positionArray);
                                        Intent intent = new Intent();
                                        intent.putIntegerArrayListExtra("files", files_to_delete);
                                        // Set result of activity to 1 -> File deleted
                                        setResult(1, intent);
                                        //Remove image from MediaStore
                                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(list_of_images.get(positionArray)))));
                                        // Set adapter position
                                        if(positionArray != 0){
                                            move_to = positionArray - 1;
                                            mPager.setCurrentItem(move_to, true);
                                        }else{
                                            if(list_of_images.size() == 1) {
                                                // Set result of activity to 2 -> Last file of folder was deleted
                                                setResult(2, intent);
                                                finish();
                                                return;
                                            }else{
                                                // Set result of activity to 3 -> First image of folder was deleted -> Need to recalculate folder thumbnail
                                                setResult(3, intent);
                                                move_to = positionArray + 1;
                                                mPager.setCurrentItem(move_to, true);
                                            }
                                        }
                                        // Remove image from arrayList
                                        list_of_images.remove(positionArray);
                                        // Notify data changed
                                        mPagerAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        })
                        .setNegativeButton(R.string.delete_no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });
    }
}
