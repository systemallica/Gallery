package com.systemallica.gallery;

import android.content.Context;
import android.content.Intent;
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

    ArrayList<String> list_of_images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PagerAdapter mPagerAdapter;
        ViewPager mPager;

        Intent intent = getIntent();
        // Get all the images in the folder
        list_of_images = intent.getStringArrayListExtra("list_of_images");

        // Instantiate a ViewPager and a PagerAdapter.
        mPagerAdapter = new CustomPagerAdapter(this);
        mPager = findViewById(R.id.pager);

        // Set the adapter
        mPager.setAdapter(mPagerAdapter);

        if (getSupportActionBar() != null) {
            // Display arrow to return to previous activity
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            case R.id.delete:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
