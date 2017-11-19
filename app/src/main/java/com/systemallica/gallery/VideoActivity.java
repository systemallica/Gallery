package com.systemallica.gallery;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.Calendar;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoActivity extends AppCompatActivity {

    private final Handler mHideHandler = new Handler();
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {

            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
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

    private SimpleExoPlayer player;
    String videoPath;
    int position_intent;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.video_view) SimpleExoPlayerView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        mVisible = true;
        playerView.hideController();

        Intent intent = getIntent();
        // Get all the images in the folder
        videoPath = intent.getStringExtra("videoPath");
        // Get position
        position_intent = intent.getIntExtra("position", 0);

        if (getSupportActionBar() != null) {
            // Display arrow to return to previous activity
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Set current title
            getSupportActionBar().setTitle(Utils.getBaseName(new File(videoPath)));
        }

        // Make navBar translucent
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            int translucentBackground = ContextCompat.getColor(this, R.color.translucent_background);
            getWindow().setNavigationBarColor(translucentBackground);
        }

        exoPlayer();
    }

    @Override
    public  boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();

        if(action == 1) {
            toggle();
        }
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            player.release();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            player.release();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        hide();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_video, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        File file = new File(videoPath);
        switch(id){
            case R.id.action_share:
                shareVideo(file);
                return true;

            case R.id.action_delete:
                deleteVideo(file);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(VideoActivity.this);
                builder.setView(layout)
                        .setTitle(R.string.rename_rename)
                        .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Change toolbar text
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
                                    intent.putExtra("name", new_name.getText().toString());
                                    // Set result of activity
                                    setResult(2, intent);
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

    private void shareVideo(File video){
        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("video/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(video));
        startActivity(Intent.createChooser(shareIntent, "Share image using"));
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
                    sizeText = " kilobytes";
                    if(sizeD>1024){
                        sizeD = sizeD/1024;//MB
                        sizeText = " megabytes";
                        if(sizeD>1024){
                            sizeD = sizeD/1024;//GB
                            sizeText = " gigabytes";
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
                AlertDialog.Builder builder = new AlertDialog.Builder(VideoActivity.this);
                builder.setView(layout)
                        .setTitle(R.string.details_video)
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

    private void deleteVideo(final File video){

        runOnUiThread(new Runnable() {
            public void run() {

                AlertDialog.Builder builder = new AlertDialog.Builder(VideoActivity.this);
                builder.setTitle(R.string.delete_title)
                        .setMessage(R.string.delete_message)
                        .setIcon(R.drawable.ic_warning_black_24dp)
                        .setPositiveButton(R.string.delete_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (video.exists()) {
                                    // Remove file from device
                                    if (video.delete()) {
                                        // Add deleted file position and send it as Extra
                                        int fileToDelete = position_intent;
                                        Intent intent = new Intent();
                                        intent.putExtra("file", fileToDelete);
                                        // Set result of activity to 1 -> File deleted
                                        setResult(1, intent);
                                        // Remove image from MediaStore
                                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(videoPath))));
                                        // Finish activity
                                        finish();
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

    public void exoPlayer(){

        // 1. Create a default TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        // 2. Create the player
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        // Bind the player to the view.
        playerView.setPlayer(player);

        // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter bandwidthMeter2 = new DefaultBandwidthMeter();

        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "yourApplicationName"), bandwidthMeter2);

        // Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        // This is the MediaSource representing the media to be played.
        MediaSource videoSource = new ExtractorMediaSource(Uri.fromFile(new File(videoPath)),
                dataSourceFactory, extractorsFactory, null, null);

        // Prepare the player with the source.
        player.prepare(videoSource);
        player.setPlayWhenReady(true);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        playerView.hideController();
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
        playerView.showController();
        // Show the system bar
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.post(mShowRunnable);
    }

}
