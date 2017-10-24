package com.systemallica.gallery;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.io.File;
import java.util.ArrayList;

public class FolderActivity extends AppCompatActivity {

    final ArrayList<File> list_of_files = new ArrayList<>();
    final ArrayList<String> list_of_paths = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get name of folder passed with the intent
        Intent intent = getIntent();
        String folder = intent.getStringExtra("folder");
        new loadImages(folder).execute();

        if (getSupportActionBar() != null) {
            // Set title to folder name
            getSupportActionBar().setTitle(folder);
            // Display arrow to return to previous activity
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private class loadImages extends AsyncTask<Void, Void, Void> {

        GridView gridView;
        GridViewAdapterImages gridAdapter;
        String folder;

        loadImages(String folder){
            super();
            this.folder = folder;
        }

        protected Void doInBackground(Void... params) {
            // Define the cursor and get path and bitmap of images
            Uri uri;
            Cursor cursor;
            int column_index_data;
            String path_of_image;
            String path_of_video;

            // Images-------------------------------------------------------------------------------

            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            String[] projection_i = { MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

            String where = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ?";

            cursor = getContentResolver().query(uri, projection_i, where, new String[]{folder},
                    MediaStore.MediaColumns.DATE_ADDED + " DESC");

            if (cursor!= null) {
                // Get path of image
                column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

                while (cursor.moveToNext()) {
                    // Get file
                    path_of_image = cursor.getString(column_index_data);
                    list_of_paths.add(path_of_image);
                    File imgFile = new File(path_of_image);
                    // Add to list
                    list_of_files.add(imgFile);

                }
                // Close the cursor
                cursor.close();
            }

            // Videos-------------------------------------------------------------------------------
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

            String[] projection_v = { MediaStore.MediaColumns.DATA,
                    MediaStore.Video.Media.BUCKET_DISPLAY_NAME };

            cursor = getContentResolver().query(uri, projection_v, where, new String[]{folder},
                    MediaStore.MediaColumns.DATE_ADDED + " DESC");

            if (cursor!= null) {
                // Get path of video
                column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

                while (cursor.moveToNext()) {
                    // Get file
                    path_of_video = cursor.getString(column_index_data);
                    File imgFile = new File(path_of_video);
                    // Add to list
                    list_of_files.add(imgFile);
                }
                // Close the cursor
                cursor.close();
            }

            // Find GridView to populate
            gridView = findViewById(R.id.gridViewFolder);
            // Set number of columns
            gridView.setNumColumns(3);
            // Create and set the adapter (context, layout_of_image, list_of_images)
            gridAdapter = new GridViewAdapterImages(FolderActivity.this,
                    R.layout.grid_item_layout_image, list_of_files, 3);

            gridAdapter.notifyDataSetChanged();

            return null;
        }
        protected void onPostExecute(Void param) {
            gridView.setAdapter(gridAdapter);
            // OnClick listener
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v,
                                        int position, long id) {

                    // Create intent
                    Intent intent = new Intent(getBaseContext(), ImageActivity.class);
                    // Pass arrayList of image paths
                    intent.putExtra("position", position);
                    intent.putExtra("list_of_images", list_of_paths);
                    // Start activity
                    startActivity(intent);
                }
            });
        }
    }
}
