package com.systemallica.gallery;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;

import java.io.File;
import java.util.ArrayList;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.systemallica.gallery.Utils.dpToPx;

public class MainActivity extends AppCompatActivity {

    final int MY_PERMISSIONS_REQUEST_BOTH= 114;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Check for sdk >= 23
        if (Build.VERSION.SDK_INT >= 23) {
            //Check CAMERA and MEDIA permission
            if (checkSelfPermission(Manifest.permission.CAMERA)!= PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!=
                    PERMISSION_GRANTED ) {
                requestPermissions(new String[]{
                                Manifest.permission.CAMERA,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_BOTH);
            //Check CAMERA permission
            }else{
                setFABListener();
                loadImages();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_BOTH:
                if(grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED
                        && grantResults[1] == PERMISSION_GRANTED) {
                    setFABListener();
                    loadImages();
                }else if (grantResults.length > 0 && grantResults[1] == PERMISSION_GRANTED){
                    Snackbar.make(findViewById(R.id.main), "Camera button won't work", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    loadImages();
                }else{
                    Snackbar.make(findViewById(R.id.main), "App can't work... closing", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    System.exit(0);
                }
        }
    }

    public void loadImages(){

        //TODO: open folders
        //TODO: set grid width/height dynamically
        //TODO: get videos
        //TODO: add DB(?)

        GridView gridView;
        GridViewAdapter gridAdapter;

        // Define the cursor and get path and bitmap of images
        Uri uri;
        ArrayList<ImageItem> list_of_folder_images = new ArrayList<>();
        ArrayList<String> list_of_folders = new ArrayList<>();
        Cursor cursor;
        int column_index_data;
        int column_index_folder_name;
        String path_of_image;
        String folder_name;

        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

        cursor = getContentResolver().query(uri, projection, null, null, null);

        if (cursor!= null) {
            // Get path of image
            column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            // Get folder name
            column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

            // Get width in dp and px
            //DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            //float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
            //int pxWidth = dpToPx((int)dpWidth, this);

            while (cursor.moveToNext()) {
                path_of_image = cursor.getString(column_index_data);
                folder_name = cursor.getString(column_index_folder_name);

                if (!list_of_folders.contains(folder_name)) {
                    list_of_folders.add(folder_name);

                    Log.i("path: ", path_of_image);
                    File imgFile = new File(path_of_image);

                    // Get number of pictures in folder
                    int files_in_folder = imgFile.getParentFile().listFiles().length;
                    // Avoid OOM error by extending allocated memory
                    //BitmapFactory.Options options = new BitmapFactory.Options();
                    //options.inJustDecodeBounds = true;
                    // Sample original image
                    //Bitmap bitmap_sampled = decodeSampledBitmapFromFile(imgFile, pxWidth, pxWidth);
                    // Generate thumbnail
                    //Bitmap bitmap_thumbnail = ThumbnailUtils.extractThumbnail(bitmap_sampled, pxWidth, pxWidth);
                    list_of_folder_images.add(new ImageItem(imgFile, folder_name, files_in_folder));
                }
            }
            // Close the cursor
            cursor.close();
        }

        // Find GridView to populate
        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setNumColumns(3);
        gridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, list_of_folder_images);
        gridView.setAdapter(gridAdapter);
    }

    //public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
    //    // Raw height and width of image
    //    final int height = options.outHeight;
    //    final int width = options.outWidth;
    //    int inSampleSize = 1;
    //
    //    if (height > reqHeight || width > reqWidth) {
    //
    //        final int halfHeight = height / 2;
    //        final int halfWidth = width / 2;
    //
    //        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
    //        // height and width larger than the requested height and width.
    //        while ((halfHeight / inSampleSize) > reqHeight
    //                && (halfWidth / inSampleSize) > reqWidth) {
    //            inSampleSize *= 2;
    //        }
    //    }
    //
    //    return inSampleSize;
    //}

    //public static Bitmap decodeSampledBitmapFromFile(File file, int reqWidth, int reqHeight) {
    //
    //    // First decode with inJustDecodeBounds=true to check dimensions
    //    final BitmapFactory.Options options = new BitmapFactory.Options();
    //    options.inJustDecodeBounds = true;
    //    BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    //    // Calculate inSampleSize
    //    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
    //
    //    // Decode bitmap with inSampleSize set
    //    options.inJustDecodeBounds = false;
    //    return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    //}

    private void setFABListener() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                startActivity(intent);
            }
        });
    }
}
