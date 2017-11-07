package com.systemallica.gallery;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class FolderActivity extends AppCompatActivity {

    final ArrayList<File> list_of_files = new ArrayList<>();
    final ArrayList<String> list_of_paths = new ArrayList<>();
    String folder;
    GridView gridView;
    GridViewAdapterImages gridAdapter;
    int columns = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get name of folder passed with the intent
        Intent intent = getIntent();
        folder = intent.getStringExtra("folder");
        loadImages(folder, columns);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_folder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            case R.id.action_increase_column:
                if(columns<6) {
                    columns++;
                    loadImages(folder, columns);
                }
                return true;
            case R.id.action_decrease_column:
                if(columns>1) {
                    columns--;
                    loadImages(folder, columns);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadImages(String folder, int columns){

            // Define the cursor and get path and bitmap of images
            Uri uri;
            Cursor cursor;
            int column_index_data;
            String path_of_image;
            String path_of_video;

            // Images-------------------------------------------------------------------------------

            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            String[] projection_i = { MediaStore.MediaColumns.DATA };

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

            String[] projection_v = { MediaStore.MediaColumns.DATA };

            cursor = getContentResolver().query(uri, projection_v, where, new String[]{folder},
                    MediaStore.MediaColumns.DATE_ADDED + " DESC");

            if (cursor!= null) {
                // Get path of video
                column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

                while (cursor.moveToNext()) {
                    // Get file
                    path_of_video = cursor.getString(column_index_data);
                    list_of_paths.add(path_of_video);
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
            gridView.setNumColumns(columns);
            // Create and set the adapter (context, layout_of_image, list_of_images)
            gridAdapter = new GridViewAdapterImages(FolderActivity.this,
                    R.layout.grid_item_layout_image,
                    list_of_files,
                    columns);
            gridView.setAdapter(gridAdapter);
            gridAdapter.notifyDataSetChanged();

            // OnClick listener
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v,
                                        int position, long id) {
                    // Create intent
                    Intent intent = new Intent(FolderActivity.this, ImageActivity.class);
                    // Pass arrayList of image paths
                    intent.putExtra("position", position);
                    intent.putExtra("list_of_images", list_of_paths);
                    // Start activity
                    startActivityForResult(intent, 1);
                }
            });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode!=0) {
                // One or more images were deleted
                ArrayList<Integer> position = data.getIntegerArrayListExtra("files");
                for (int i = 0; i < position.size(); i++) {
                    list_of_paths.remove(list_of_paths.get(position.get(i)));
                    list_of_files.remove(list_of_files.get(position.get(i)));
                }
                gridAdapter.notifyDataSetChanged();
            }
            // The only image of the folder was deleted
            if(resultCode == 2) {
                TextView text = findViewById(R.id.placeholderNoImages);
                text.setText(R.string.no_images);

                // Set result of activity to 1 -> Folder emptied
                setResult(1);
            }

            // The first image of the folder was deleted, need a new thumbnail
            if(resultCode == 3) {
                // Set result of activity to 2 -> Thumbnail needs change
                setResult(2);
            }
        }
    }
}
