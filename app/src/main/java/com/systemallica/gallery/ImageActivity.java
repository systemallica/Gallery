package com.systemallica.gallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class ImageActivity extends AppCompatActivity {

    ArrayList<String> list_of_images = new ArrayList<>();
    ArrayList<Integer> files_to_delete = new ArrayList<>();
    PagerAdapter mPagerAdapter;
    ViewPager mPager;
    int positionArray;
    boolean isToolbarHidden;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                                                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

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
            isToolbarHidden = true;
        }

        // Set result to 0 -> No file deleted
        setResult(0);

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
        public Object instantiateItem(ViewGroup container, int position) {

            ImageView imageView = new ImageView(getApplicationContext());
            File image = new File(list_of_images.get(position));

            GlideApp
                    .with(getApplicationContext())
                    .load(image)
                    .transition(withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(imageView);

            //TODO: set title on page change
            // Set title to image name-> should be done on page change
            //if(getSupportActionBar()!= null) {
            //    getSupportActionBar().setTitle(image.getName());
            //}

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getSupportActionBar() != null) {

                        if(isToolbarHidden){
                            getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                            getSupportActionBar().show();
                            isToolbarHidden = false;
                        }else{
                            getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                            getSupportActionBar().hide();
                            isToolbarHidden = true;
                        }
                    }
                }
            });

            container.addView(imageView);

            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((ImageView) object);
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
                Log.e("test", " test1");
                positionArray = mPager.getCurrentItem();
                shareImage(new File(list_of_images.get(positionArray)));
                return true;

            case R.id.delete:
                positionArray = mPager.getCurrentItem();
                deleteImage(new File(list_of_images.get(positionArray)));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareImage(File image){
        Log.e("test", " test2");
        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(image));
        startActivity(Intent.createChooser(shareIntent, "Share image using"));
    }

    private void deleteImage(File image){
        int move_to;
        if (image.exists()) {
            // Remove file from device
            if (image.delete()) {
                // Add deleted file position to ArrayList and send it as Extra
                Log.e("deleted image: ", image.getAbsolutePath());
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
                        // Set result of activity to 2 -> Last file of folder deleted
                        setResult(2, intent);
                        finish();
                        return;
                    }else{
                        // Set result of activity to 3 -> First image of folder was delete -> Need to recalculate folder thumbnail
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
}
