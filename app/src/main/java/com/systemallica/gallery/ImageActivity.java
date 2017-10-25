package com.systemallica.gallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
    int position_array;

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

            position_array = position;

            ImageView imageView = new ImageView(getApplicationContext());
            File image = new File(list_of_images.get(position));

            GlideApp
                    .with(getApplicationContext())
                    .load(image)
                    .transition(withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(imageView);

            // Set title to image name
            if(getSupportActionBar()!= null) {
                getSupportActionBar().setTitle(image.getName());
            }

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
            case R.id.delete:
                deleteImage(new File(list_of_images.get(position_array)));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteImage(File image){
        int move_to;
        if (image.exists()) {
            // Remove file from device
            if (image.delete()) {
                // Add deleted file position to ArrayList and send it as Extra
                files_to_delete.add(position_array);
                Intent intent = new Intent();
                intent.putIntegerArrayListExtra("files", files_to_delete);
                // Set result of activity to 1 -> File deleted
                setResult(1, intent);
                //Remove image from MediaStore
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(list_of_images.get(position_array)))));
                // Set adapter position
                if(position_array>1){
                    move_to = position_array - 1;
                    mPager.setCurrentItem(move_to, true);
                }else{
                    move_to = position_array + 1;
                    mPager.setCurrentItem(move_to, true);
                }
                // Remove image from arrayList
                list_of_images.remove(position_array);
                // Notify data changed
                mPagerAdapter.notifyDataSetChanged();
            }
        }
    }
}
