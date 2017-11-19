package com.systemallica.gallery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.Locale;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class ImageActivity extends AppCompatActivity {

    ArrayList<String> list_of_images = new ArrayList<>();
    PagerAdapter mPagerAdapter;
    ViewPager mPager;
    int positionArray;

    private final Handler mHideHandler = new Handler();
    private final Runnable mHideRunnable = new Runnable() {
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
    private final Runnable mShowRunnable = new Runnable() {
        @Override
        public void run() {
            // Display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        // Get all the images in the folder
        list_of_images = intent.getStringArrayListExtra("list_of_images");
        // Get position
        int position_intent = intent.getIntExtra("position", 0);
        // Instantiate a ViewPager and a PagerAdapter.
        mPagerAdapter = new CustomPagerAdapter(this);
        mPager = findViewById(R.id.pager);
        // Set offScreenLimit
        mPager.setOffscreenPageLimit(4);
        // Set the adapter
        mPager.setAdapter(mPagerAdapter);
        // Set current position
        mPager.setCurrentItem(position_intent);
        // Set onPageChangeListener
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                // Set title to current image's name
                positionArray = mPager.getCurrentItem();
                toolbar.setTitle(Utils.getBaseName(new File(list_of_images.get(positionArray))));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (getSupportActionBar() != null) {
            // Display arrow to return to previous activity
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Hide it by default
            getSupportActionBar().hide();
            // Set current title
            getSupportActionBar().setTitle(new File(list_of_images.get(position_intent)).getName());
        }

        // Make navBar translucent
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            int translucentBackground = ContextCompat.getColor(this, R.color.translucent_background);
            getWindow().setNavigationBarColor(translucentBackground);
        }

        // Set result to 0 -> No file deleted
        setResult(0);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide()
        hide();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 99) {
            if(resultCode == 1) {
                // Update UI
                deleteVideo();
            }else if(resultCode == 2) {
                // Change toolbar text
                String name = data.getStringExtra("name");
                Toolbar toolbar = findViewById(R.id.toolbar);
                toolbar.setTitle(name);
                mPagerAdapter.notifyDataSetChanged();
                // Reload images when leaving activity
                setResult(5);
            }
        }
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
        mHideHandler.removeCallbacks(mShowRunnable);
        mHideHandler.post(mHideRunnable);
    }

    private void show() {
        // Show the system bar
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.post(mShowRunnable);
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
        public Object instantiateItem(ViewGroup container, final int position) {


            final File image = new File(list_of_images.get(position));

            // Inflate layout
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.view_pager_item, container, false);
            // Get overlay ImageView
            ImageView overlay = layout.findViewById(R.id.outside_imageview);

            // Video/gif thumbnail
            if(Utils.isVideoOrGif(image.getName())){
                ImageView imageView = layout.findViewById(R.id.inside_imageview);
                GlideApp
                        .with(getApplicationContext())
                        .load(image)
                        .transition(withCrossFade())
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(imageView);
                if(Utils.isVideo(image.getName())) {
                    SubsamplingScaleImageView subSampling = layout.findViewById(R.id.inside_imageview_sub);

                    // Add overlay
                    overlay.setVisibility(View.VISIBLE);
                    // Play video on tap
                    overlay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.e("click", "glide");
                            // Create video intent
                            Intent intent = new Intent(ImageActivity.this, VideoActivity.class);
                            // Pass arrayList of image paths
                            intent.putExtra("position", position);
                            intent.putExtra("videoPath", image.getAbsolutePath());
                            // Start activity
                            startActivityForResult(intent, 99);
                        }
                    });
                    // Set up the user interaction to manually show or hide the system UI.
                    subSampling.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v){
                            toggle();
                        }
                    });

                    imageView.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v){
                            toggle();
                        }
                    });

                }else {
                    // Set up the user interaction to manually show or hide the system UI.
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            toggle();
                        }
                    });
                }
                // Full image display
            }else{
                // Get subSampling view
                SubsamplingScaleImageView imageView = layout.findViewById(R.id.inside_imageview_sub);
                // Use EXIF rotation
                imageView.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
                // Allow more zooming
                imageView.setMinimumDpi(10);
                // Set image
                imageView.setImage(ImageSource.uri(image.getPath()));
                // Hide overlay
                overlay.setVisibility(View.INVISIBLE);
                // Set up the user interaction to manually show or hide the system UI.
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        toggle();
                    }
                });
            }

            container.addView(layout);

            return layout;
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
        positionArray = mPager.getCurrentItem();
        File file = new File(list_of_images.get(positionArray));
        switch(id){
            case R.id.action_share:
                shareImage(file);
                return true;

            case R.id.action_delete:
                deleteImage(file);
                return true;

            case R.id.action_rename:
                renameFile(file);
                return true;

            case R.id.action_details:
                showDetails(file);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void renameFile(final File file){

        runOnUiThread(new Runnable() {
            public void run() {

                // Inflate layout and get views
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.rename_dialog, null);
                TextView old_name = layout.findViewById(R.id.old_name);
                final EditText new_name = layout.findViewById(R.id.new_name);

                String set = "Old name: " + Utils.getBaseName(file);
                old_name.setText(set);

                // Create and show dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(ImageActivity.this);
                builder.setView(layout)
                        .setTitle(R.string.rename_rename)
                        .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Change toolbar text
                                Toolbar toolbar = findViewById(R.id.toolbar);
                                toolbar.setTitle(new_name.getText().toString());
                                // Build path with new name
                                String newName;
                                String folderPath = file.getParentFile().getPath();
                                newName = folderPath + "/" + new_name.getText().toString() + Utils.getExtension(file);
                                // Rename the file and set activity result
                                if(file.renameTo(new File(newName))){
                                    //Remove image from MediaStore
                                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                                    //Add new file to MediaStore
                                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(newName))));
                                    // Send intent
                                    Intent intent = new Intent();
                                    intent.putExtra("file", positionArray);
                                    // Set result of activity
                                    setResult(4, intent);
                                }
                            }
                        })
                        .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int which) {
                                // Close AlertDialog
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });

    }

    private void showDetails(final File image){
        runOnUiThread(new Runnable() {
            public void run() {

                // Inflate layout and get views
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.details_dialog, null);
                TextView nameT = layout.findViewById(R.id.name);
                TextView pathT = layout.findViewById(R.id.path);
                TextView sizeT = layout.findViewById(R.id.size);
                TextView typeT = layout.findViewById(R.id.type);
                TextView modifiedT = layout.findViewById(R.id.modified);

                // Name
                nameT.setText(image.getName());
                // Path
                pathT.setText(image.getPath());
                // Size
                Long size = image.length();
                Double sizeD = size.doubleValue();
                String sizeText = " bytes";
                if(sizeD>1024){
                    sizeD = sizeD/1024;//KB
                    sizeText = " kB";
                    if(sizeD>1024){
                        sizeD = sizeD/1024;//MB
                        sizeText = " MB";
                        if(sizeD>1024){
                            sizeD = sizeD/1024;//GB
                            sizeText = " GB";
                        }
                    }
                }
                Locale current = getResources().getConfiguration().locale;
                String result = String.format(current,"%.3f", sizeD) + sizeText;
                sizeT.setText(result);
                // Type
                String type = Utils.getMimeType(image.getPath());
                typeT.setText(type);
                // Modified
                // Convert from ms to time
                Calendar date = new GregorianCalendar();
                date.setTimeInMillis(image.lastModified());
                // Format time as e.g. "Fri Feb 17 07:45:42 PST 2017"
                StringBuilder sbu = new StringBuilder();
                Formatter fmt = new Formatter(sbu);
                fmt.format("%tc", date.getTime());

                modifiedT.setText(sbu);

                // Create and show dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(ImageActivity.this);
                builder.setView(layout)
                        .setTitle(R.string.details_image)
                        .setIcon(R.drawable.ic_information_outline_black_48dp)
                        .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Close AlertDialog
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
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
                                        int fileToDelete = positionArray;
                                        Intent intent = new Intent();
                                        intent.putExtra("file", fileToDelete);
                                        // Set result of activity to 1 -> File deleted
                                        setResult(1, intent);
                                        //Remove image from MediaStore
                                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(list_of_images.get(positionArray)))));
                                        // Remove image from arrayList
                                        list_of_images.remove(positionArray);
                                        // Notify data changed
                                        mPagerAdapter.notifyDataSetChanged();
                                        // Set adapter position
                                        if(positionArray != 0){
                                            move_to = positionArray - 1;
                                            mPager.setCurrentItem(move_to, true);
                                        }else{
                                            if(list_of_images.size() == 0) {
                                                // Set result of activity to 2 -> Last file of folder was deleted
                                                setResult(2, intent);
                                                finish();
                                            }else{
                                                // Set result of activity to 3 -> First image of folder was deleted -> Need to recalculate folder thumbnail
                                                setResult(3, intent);
                                            }
                                        }

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

    private void deleteVideo(){

        runOnUiThread(new Runnable() {
            public void run() {
                int move_to;
                // Add deleted file position to ArrayList and send it as Extra
                int fileToDelete = positionArray;
                Intent intent = new Intent();
                intent.putExtra("file", fileToDelete);
                // Set result of activity to 1 -> File deleted
                setResult(1, intent);
                // Remove image from arrayList
                list_of_images.remove(positionArray);
                // Notify data changed
                mPagerAdapter.notifyDataSetChanged();
                // Set adapter position
                if(positionArray != 0){
                    move_to = positionArray - 1;
                    mPager.setCurrentItem(move_to, true);
                }else{
                    if(list_of_images.size() == 0) {
                        // Set result of activity to 2 -> Last file of folder was deleted
                        setResult(2, intent);
                        finish();
                    }else{
                        // Set result of activity to 3 -> First file of folder was deleted -> Need to recalculate folder thumbnail
                        setResult(3, intent);
                    }
                }
            }
        });
    }
}
