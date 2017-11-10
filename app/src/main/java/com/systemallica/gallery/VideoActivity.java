package com.systemallica.gallery;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.Locale;

public class VideoActivity extends AppCompatActivity {

    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
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
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private SimpleExoPlayerView playerView;
    private SimpleExoPlayer player;
    ArrayList<String> list_of_videos = new ArrayList<>();
    int position_intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mVisible = true;
        playerView = findViewById(R.id.video_view);
        playerView.hideController();

        Intent intent = getIntent();
        // Get all the images in the folder
        list_of_videos = intent.getStringArrayListExtra("list_of_images");
        // Get position
        position_intent = intent.getIntExtra("position", 0);

        if (getSupportActionBar() != null) {
            // Display arrow to return to previous activity
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        delayedHide();
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
        File file = new File(list_of_videos.get(position_intent));
        switch(id){
            case R.id.action_share:
                shareVideo(file);
                return true;

            //case R.id.action_delete:
            //    deleteImage(file);
            //    return true;

            case R.id.action_details:
                showDetails(file);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareVideo(File video){
        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("video/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(video));
        startActivity(Intent.createChooser(shareIntent, "Share image using"));
    }

    void showDetails(final File image){
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
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Close AlertDialog
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
        MediaSource videoSource = new ExtractorMediaSource(Uri.fromFile(new File(list_of_videos.get(position_intent))),
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
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.post(mHidePart2Runnable);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        playerView.showController();
        // Show the system bar
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private void delayedHide() {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, UI_ANIMATION_DELAY);
    }
}
