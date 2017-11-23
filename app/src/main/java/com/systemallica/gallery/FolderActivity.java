package com.systemallica.gallery;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FolderActivity extends AppCompatActivity {

    final ArrayList<File> list_of_files = new ArrayList<>();
    final ArrayList<String> list_of_paths = new ArrayList<>();
    String folder;
    GridViewAdapterImages gridAdapter;
    int columns = 3;
    @BindView(R.id.swipelayout) SwipeRefreshLayout swipeLayout;
    @BindView(R.id.gridViewFolder) GridView gridView;
    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);
        ButterKnife.bind(this);
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

        // Change navBar colour
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            int app_primary = ContextCompat.getColor(this, R.color.app_primary);
            getWindow().setNavigationBarColor(app_primary);
        }

        // Set on swipe refresh listener
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startRefresh();
            }
        });
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

    private void startRefresh(){

        swipeLayout.setRefreshing(true);
        // Sleep main thread for better UI feedback
        new DummySleep().execute();

    }

    private class DummySleep extends AsyncTask<Void, Void, Void> {

        static final int TASK_DURATION = 2 * 1000; // 2 seconds

        @Override
        protected Void doInBackground(Void... params) {
            // Sleep for a small amount of time to simulate a background-task
            try {
                Thread.sleep(TASK_DURATION);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            endRefresh();
        }

    }

    private void endRefresh(){
        // Refresh data
        loadImages(folder, columns);
        // Stop the refreshing indicator
        swipeLayout.setRefreshing(false);
    }

    private void loadImages(String folder, int columns){

            // Define the cursor and get path and bitmap of images
            Uri uri;
            Cursor cursor;
            int column_index_data;
            String path_of_image;
            String path_of_video;

            // Initialisations
            list_of_files.clear();
            list_of_paths.clear();

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
                    File imgFile = new File(path_of_video);
                    // Add to list
                    list_of_files.add(imgFile);
                }
                // Close the cursor
                cursor.close();
            }

            //Collections.sort(list_of_paths, new Utils.SortFilesByDate());
            Collections.sort(list_of_files, new Utils.SortFilesByDate());
            // Create list of files' paths
            for (File file : list_of_files) {
                list_of_paths.add(file.getPath());
            }

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
                    if(Utils.isVideo(list_of_paths.get(position))){
                        // Create video intent
                        Intent intent = new Intent(FolderActivity.this, VideoActivity.class);
                        // Pass arrayList of image paths
                        intent.putExtra("position", position);
                        intent.putExtra("videoPath", list_of_paths.get(position));
                        // Start activity
                        startActivityForResult(intent, 2);
                    }else {
                        // Create image intent
                        Intent intent = new Intent(FolderActivity.this, ImageActivity.class);
                        // Pass arrayList of image paths
                        intent.putExtra("position", position);
                        intent.putExtra("list_of_images", list_of_paths);
                        // Start activity
                        startActivityForResult(intent, 1);
                    }
                }
            });

            // OnLongClick listener
            gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> arg0, View grid_item,
                                               int position, long arg3) {
                    // Get layout
                    ViewGroup layout = (ViewGroup) grid_item;
                    // Get "check" ImageView
                    ImageView check = (ImageView) layout.getChildAt(2);
                    // Toggle "check"
                    if(check.getVisibility() == View.VISIBLE){
                        check.setVisibility(View.GONE);
                    }else {
                        check.setVisibility(View.VISIBLE);
                    }

                    return true;
                }
            });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == 1) {
                // One image was deleted
                int position = data.getIntExtra("file", 0);
                list_of_paths.remove(list_of_paths.get(position));
                list_of_files.remove(list_of_files.get(position));
                gridAdapter.notifyDataSetChanged();
                // One pic deleted
                setResult(2);
            }
            // The only image of the folder was deleted
            else if(resultCode == 2) {
                int position = data.getIntExtra("file", 0);
                list_of_paths.remove(list_of_paths.get(position));
                list_of_files.remove(list_of_files.get(position));
                gridAdapter.notifyDataSetChanged();
                // Reload
                startRefresh();
                // Show message
                Snackbar.make(findViewById(R.id.folder), getString(R.string.no_images), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                // Folder emptied
                setResult(1);
            }
            // The first image of the folder was deleted, need a new thumbnail
            else if(resultCode == 3) {
                // Thumbnail needs change
                setResult(2);
            }
            // UI needs to be reloaded
            else if(resultCode == 4) {
                // One image was renamed
                int position = data.getIntExtra("file", 0);
                list_of_paths.remove(list_of_paths.get(position));
                list_of_files.remove(list_of_files.get(position));
                gridAdapter.notifyDataSetChanged();
                // Reload
                startRefresh();
                setResult(2);
            }
            // Simple UI refresh
            else if(resultCode == 5) {
                // Reload
                startRefresh();
                setResult(2);
            }
        }else if (requestCode == 2){
            if(resultCode == 1) {
                // One video was deleted/renamed
                int position = data.getIntExtra("file", 0);
                list_of_paths.remove(list_of_paths.get(position));
                list_of_files.remove(list_of_files.get(position));
                gridAdapter.notifyDataSetChanged();
                // Reload
                startRefresh();
                // Set result
                setResult(2);
            }
        }
    }
}
