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
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {

    final int MY_PERMISSIONS_REQUEST_BOTH= 114;
    int columns = 2;
    int folder_position;
    ArrayList<FolderItem> list_of_folders = new ArrayList<>();
    ArrayList<FolderItem> list_of_folders_i = new ArrayList<>();
    ArrayList<FolderItem> list_of_folders_v = new ArrayList<>();
    ArrayList<String> list_of_folder_names = new ArrayList<>();
    ArrayList<String> list_of_folder_names_i = new ArrayList<>();
    ArrayList<String> list_of_folder_names_v = new ArrayList<>();
    GridView gridView;
    GridViewAdapterFolders gridAdapter;

    @BindView(R.id.swipelayout) SwipeRefreshLayout swipeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
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
                loadFolders(columns);
            }
        }

        // Set on swipe refresh listener
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startRefresh();
            }
        });

        // Change navBar colour
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            int app_primary = ContextCompat.getColor(this, R.color.app_primary);
            getWindow().setNavigationBarColor(app_primary);
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
                    loadFolders(columns);
                }
                return true;
            case R.id.action_decrease_column:
                if(columns>1) {
                    columns--;
                    loadFolders(columns);
                }
                return true;
            case R.id.action_about:
                // Start AboutActivity
                Intent intent = new Intent(getBaseContext(), AboutActivity.class);
                startActivity(intent);
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
                    loadFolders(columns);
                }else if (grantResults.length > 0 && grantResults[1] == PERMISSION_GRANTED){
                    Snackbar.make(findViewById(R.id.main), "Camera button won't work",
                            Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    loadFolders(columns);
                }else{
                    Snackbar.make(findViewById(R.id.main), "App can't work... closing",
                            Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    System.exit(0);
                }
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
        loadFolders(columns);
        // Stop the refreshing indicator
        swipeLayout.setRefreshing(false);
    }

    private void loadFolders(int columns){

        // Define the cursor and get path and bitmap of images
        Uri uri;
        Cursor cursor;
        int column_index_data;
        int column_index_folder_name;
        String path_of_image;
        String path_of_video;
        String folder_name;

        // Initialisations
        list_of_folders.clear();
        list_of_folders_i.clear();
        list_of_folders_v.clear();
        list_of_folder_names.clear();
        list_of_folder_names_i.clear();
        list_of_folder_names_v.clear();

        // Images-----------------------------------------------------------------------------------

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

                if (!list_of_folder_names_i.contains(folder_name)) {
                    list_of_folder_names_i.add(folder_name);

                    File imgFile = new File(path_of_image);

                    // Get number of pictures in folder
                    int files_in_folder = imgFile.getParentFile().listFiles(new Utils.MediaFileFilter()).length;
                    // Add to list
                    list_of_folders_i.add(new FolderItem(imgFile, folder_name, files_in_folder));
                }
            }
            // Close the cursor
            cursor.close();
        }

        // Videos-----------------------------------------------------------------------------------
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

                if (!list_of_folder_names_v.contains(folder_name)) {
                    list_of_folder_names_v.add(folder_name);

                    File imgFile = new File(path_of_video);

                    // Get number of pictures in folder
                    int files_in_folder = imgFile.getParentFile().listFiles(new Utils.MediaFileFilter()).length;
                    // Add to list
                    list_of_folders_v.add(new FolderItem(imgFile, folder_name, files_in_folder));
                }
            }
            // Close the cursor
            cursor.close();
        }

        // Compare the results of both queries and join them together in a single sorted list
        boolean match;

        // For every folder with videos
        for(int j=0; j<list_of_folders_v.size(); j++){
            match = false;
            // For every folder with images
            for(int i=0; i< list_of_folders_i.size(); i++){
                // If the folder contains both videos and images
                if(list_of_folders_v.get(j).getTitle().equals(list_of_folders_i.get(i).getTitle())){
                    match = true;
                    // If the video is more recent than the image
                    if(list_of_folders_v.get(j).getImage().lastModified() > list_of_folders_i.get(i).getImage().lastModified()){
                        // Add video to list
                        list_of_folders.add(list_of_folders_v.get(j));
                        list_of_folder_names.add(list_of_folders_v.get(j).getTitle());
                        break;
                    }else{
                        // Add image to list
                        list_of_folders.add(list_of_folders_i.get(i));
                        list_of_folder_names.add(list_of_folders_i.get(i).getTitle());
                        break;
                    }
                }
            }
            // If the folder only contains videos
            if(!match){
                // Add video to list
                list_of_folders.add(list_of_folders_v.get(j));
                list_of_folder_names.add(list_of_folders_v.get(j).getTitle());
            }
        }
        // For every folder with images
        for(int z=0; z<list_of_folders_i.size(); z++){
            // If the folder only contains images
            if(!list_of_folder_names.contains(list_of_folders_i.get(z).getTitle())){
                // Add image to list
                list_of_folders.add(list_of_folders_i.get(z));
                list_of_folder_names.add(list_of_folders_i.get(z).getTitle());
            }
        }

        Collections.sort(list_of_folders, new Utils.SortFoldersByName());

        // Find GridView to populate
        gridView = findViewById(R.id.gridView);
        // Set number of columns
        gridView.setNumColumns(columns);
        // Create and set the adapter (context, layout_of_image, list_of_folders)
        gridAdapter = new GridViewAdapterFolders(MainActivity.this,
                R.layout.grid_item_layout_folder,
                list_of_folders,
                columns);
        gridView.setAdapter(gridAdapter);
        gridAdapter.notifyDataSetChanged();

        // OnClick listener
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                folder_position = position;
                // Create intent
                Intent intent = new Intent(getBaseContext(), FolderActivity.class);
                intent.putExtra("folder", list_of_folders.get(position).getTitle());
                // Start activity
                startActivityForResult(intent, 1);
            }
        });

        setFABScrollListener();

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == 1) {
                list_of_folders.remove(folder_position);
                gridAdapter.notifyDataSetChanged();
            }
            if(resultCode == 2) {
                startRefresh();
            }
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

}
