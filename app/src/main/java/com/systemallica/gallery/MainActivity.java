package com.systemallica.gallery;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {

    final int MY_PERMISSIONS_REQUEST_BOTH= 114;
    int columns = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Check for sdk >= 23
        if (Build.VERSION.SDK_INT >= 23) {
            // Check CAMERA and MEDIA permission
            if (checkSelfPermission(Manifest.permission.CAMERA)!= PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!= PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PERMISSION_GRANTED ){
                requestPermissions(new String[]{
                                Manifest.permission.CAMERA,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_BOTH);
            // When permissions are granted
            }else{
                setFABListener();
                new loadFolders(columns).execute();
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

        switch(id){
            case R.id.action_settings:
                Snackbar.make(findViewById(R.id.main), "Settings", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return true;
            case R.id.action_increase_column:
                if(columns<6) {
                    columns++;
                    new loadFolders(columns).execute();
                }
                return true;
            case R.id.action_decrease_column:
                if(columns>1) {
                    columns--;
                    new loadFolders(columns).execute();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_BOTH:
                if(grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED
                        && grantResults[1] == PERMISSION_GRANTED) {
                    setFABListener();
                    new loadFolders(columns).execute();
                }else if (grantResults.length > 0 && grantResults[1] == PERMISSION_GRANTED){
                    Snackbar.make(findViewById(R.id.main), "Camera button won't work",
                            Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    new loadFolders(columns).execute();
                }else{
                    Snackbar.make(findViewById(R.id.main), "App can't work... closing",
                            Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    System.exit(0);
                }
        }
    }

    private class loadFolders extends AsyncTask<Void, Void, ArrayList<FolderItem>>{

        GridView gridView;
        GridViewAdapterFolders gridAdapter;
        int columns;

        loadFolders(int columns){
            super();
            this.columns = columns;
        }

        protected ArrayList<FolderItem> doInBackground(Void... params) {

            // Define the cursor and get path and bitmap of images
            Uri uri;
            ArrayList<FolderItem> list_of_folders = new ArrayList<>();
            ArrayList<String> list_of_folder_names = new ArrayList<>();
            Cursor cursor;
            int column_index_data;
            int column_index_folder_name;
            String path_of_image;
            String path_of_video;
            String folder_name;

            // Images-------------------------------------------------------------------------------

            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            String[] projection_i = { MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

            cursor = getContentResolver().query(uri, projection_i, null, null,
                    MediaStore.MediaColumns.DATE_ADDED + " DESC");

            if (cursor!= null) {
                // Get path of image
                column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                // Get folder name
                column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

                while (cursor.moveToNext()) {
                    path_of_image = cursor.getString(column_index_data);
                    folder_name = cursor.getString(column_index_folder_name);

                    if (!list_of_folder_names.contains(folder_name)) {
                        list_of_folder_names.add(folder_name);

                        //Log.i("path: ", path_of_image);
                        File imgFile = new File(path_of_image);

                        // Get number of pictures in folder
                        int files_in_folder = imgFile.getParentFile().listFiles(new ImageFileFilter()).length;
                        // Add to list
                        list_of_folders.add(new FolderItem(imgFile, folder_name, files_in_folder));
                    }
                }
                // Close the cursor
                cursor.close();
            }

            // Videos-------------------------------------------------------------------------------
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

            String[] projection_v = { MediaStore.MediaColumns.DATA,
                    MediaStore.Video.Media.BUCKET_DISPLAY_NAME };

            cursor = getContentResolver().query(uri, projection_v, null, null,
                    MediaStore.MediaColumns.DATE_ADDED + " DESC");

            if (cursor!= null) {
                // Get path of video
                column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                // Get folder name
                column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);

                while (cursor.moveToNext()) {
                    path_of_video = cursor.getString(column_index_data);
                    folder_name = cursor.getString(column_index_folder_name);

                    if (!list_of_folder_names.contains(folder_name)) {
                        list_of_folder_names.add(folder_name);

                        //Log.i("path: ", path_of_video);
                        File imgFile = new File(path_of_video);

                        // Get number of pictures in folder
                        int files_in_folder = imgFile.getParentFile().listFiles(new VideoFileFilter()).length;
                        // Add to list
                        list_of_folders.add(new FolderItem(imgFile, folder_name, files_in_folder));
                    }
                }
                // Close the cursor
                cursor.close();
            }

            Collections.sort(list_of_folders, new SortFoldersByName());

            // Find GridView to populate
            gridView = findViewById(R.id.gridView);
            // Set number of columns
            gridView.setNumColumns(columns);
            // Create and set the adapter (context, layout_of_image, list_of_folders)
            gridAdapter = new GridViewAdapterFolders(MainActivity.this,
                                                        R.layout.grid_item_layout_folder,
                                                        list_of_folders,
                                                        columns);

            return list_of_folders;
        }

        protected void onPostExecute(final ArrayList<FolderItem> list_of_folders) {
            gridView.setAdapter(gridAdapter);
            gridAdapter.notifyDataSetChanged();
            // OnClick listener
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v,
                                        int position, long id) {
                    // Create intent
                    Intent intent = new Intent(getBaseContext(), FolderActivity.class);
                    intent.putExtra("folder", list_of_folders.get(position).getTitle());
                    // Start activity
                    startActivity(intent);
                }
            });

            setFABScrollListener();
        }
    }

    private void setFABListener() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                startActivity(intent);
            }
        });
    }

    private void setFABScrollListener(){
        final FloatingActionButton fab = findViewById(R.id.fab);
        GridView gridView = findViewById(R.id.gridView);

        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            int mLastFirstVisibleItem;

            // Called at end of scrolling action
            public void onScroll (AbsListView view, int firstVisibleItem, int visibleItemCount,
                                  int totalItemCount) {
                // Do nothing
            }

            // Called at beginning of scrolling action
            public void onScrollStateChanged(AbsListView view, int scrollState){

                if (view.getId() == view.getId()) {
                    final int currentFirstVisibleItem = view.getFirstVisiblePosition();
                    if (currentFirstVisibleItem > mLastFirstVisibleItem) {
                        // Scrolling down
                        fab.hide();
                    } else if (currentFirstVisibleItem < mLastFirstVisibleItem) {
                        // Scrolling up
                        fab.show();
                    }

                    mLastFirstVisibleItem = currentFirstVisibleItem;
                }
            }
        });
    }

    // Filters
    private static class SortFoldersByName implements Comparator<FolderItem> {
        @Override
        public int compare(FolderItem o1, FolderItem o2) {
            return (o1.getTitle()).compareTo(o2.getTitle());
        }
    }

    private static class ImageFileFilter implements FileFilter {
        private final String[] okFileExtensions = new String[] { "jpg", "jpeg", "png", "gif" };

        public boolean accept(File file) {
            for (String extension : okFileExtensions) {
                if (file.getName().toLowerCase().endsWith(extension)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class VideoFileFilter implements FileFilter {
        private final String[] okFileExtensions = new String[] { "mp4" };

        public boolean accept(File file) {
            for (String extension : okFileExtensions) {
                if (file.getName().toLowerCase().endsWith(extension)) {
                    return true;
                }
            }
            return false;
        }
    }
}
