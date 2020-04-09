package com.systemallica.gallery

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_video.*
import kotlinx.android.synthetic.main.rename_dialog.*
import java.io.File
import java.util.*

class VideoActivity : AppCompatActivity() {
    private val mHideHandler = Handler()
    private val mHideRunnable = Runnable {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }
    private val mShowRunnable = Runnable { // Display of UI elements
        val actionBar = supportActionBar
        actionBar?.show()
    }
    private var mVisible = false
    private var videoPath: String? = null
    private var positionIntent = 0
    private var player: SimpleExoPlayer? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContentView(R.layout.activity_video)
        setSupportActionBar(toolbar)
        player = SimpleExoPlayer.Builder(this).build()

        // Bind the player to the view.
        video_view!!.player = player
        mVisible = true
        video_view!!.hideController()
        val intent = intent
        // Get all the images in the folder
        videoPath = intent.getStringExtra("videoPath")
        // Get position
        positionIntent = intent.getIntExtra("position", 0)
        if (supportActionBar != null) {
            // Display arrow to return to previous activity
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            // Set current title
            supportActionBar!!.title = Utils.getBaseName(File(videoPath!!))
        }

        // Make navBar translucent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val translucentBackground = ContextCompat.getColor(this, R.color.translucent_background)
            window.navigationBarColor = translucentBackground
        }
        initExoPlayer()
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.action
        if (action == 1) {
            toggle()
        }
        return true
    }

    public override fun onPause() {
        super.onPause()
        player!!.release()
    }

    public override fun onStop() {
        super.onStop()
        player!!.release()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        hide()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_video, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        val file = File(videoPath!!)
        return when (id) {
            R.id.action_share -> {
                shareVideo(file)
                true
            }
            R.id.action_delete -> {
                deleteVideo(file)
                true
            }
            R.id.action_rename -> {
                renameFile(file)
                true
            }
            R.id.action_details -> {
                showDetails(file)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun renameFile(file: File) {
        runOnUiThread { // Inflate layout and get views
            val inflater = layoutInflater
            val layout = inflater.inflate(R.layout.rename_dialog, null)
            val set = "Old name: " + Utils.getBaseName(file)
            old_name.text = set

            // Create and show dialog
            val builder = AlertDialog.Builder(this@VideoActivity)
            builder.setView(layout)
                    .setTitle(R.string.rename_rename)
                    .setPositiveButton(R.string.action_ok) { _, _ ->
                        // Change toolbar text
                        toolbar!!.title = new_name.text.toString()
                        // Build path with new name
                        val newName: String
                        val folderPath = Objects.requireNonNull(file.parentFile).path
                        newName = folderPath + "/" + new_name.text.toString() + Utils.getExtension(file)
                        // Rename the file and set activity result
                        if (file.renameTo(File(newName))) {
                            //Remove image from MediaStore
                            sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))
                            //Add new file to MediaStore
                            sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(File(newName))))
                            // Send intent
                            val intent = Intent()
                            intent.putExtra("name", new_name.text.toString())
                            // Set result of activity
                            setResult(2, intent)
                        }
                    }
                    .setNegativeButton(R.string.action_cancel) { _, _ ->
                        // Close AlertDialog
                    }
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun shareVideo(video: File) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "video/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(video))
        startActivity(Intent.createChooser(shareIntent, "Share image using"))
    }

    private fun showDetails(image: File) {
        runOnUiThread { // Inflate layout and get views
            val inflater = layoutInflater
            val layout = inflater.inflate(R.layout.details_dialog, null)
            val nameT = layout.findViewById<TextView>(R.id.name)
            val pathT = layout.findViewById<TextView>(R.id.path)
            val sizeT = layout.findViewById<TextView>(R.id.size)
            val typeT = layout.findViewById<TextView>(R.id.type)
            val modifiedT = layout.findViewById<TextView>(R.id.modified)

            // Name
            nameT.text = image.name
            // Path
            pathT.text = image.path
            // Size
            var sizeD = image.length().toDouble()
            var sizeText = " bytes"
            if (sizeD > 1024) {
                sizeD /= 1024 //KB
                sizeText = " kilobytes"
                if (sizeD > 1024) {
                    sizeD /= 1024 //MB
                    sizeText = " megabytes"
                    if (sizeD > 1024) {
                        sizeD /= 1024 //GB
                        sizeText = " gigabytes"
                    }
                }
            }
            val current = resources.configuration.locale
            val result = String.format(current, "%.3f", sizeD) + sizeText
            sizeT.text = result
            // Type
            val type = Utils.getMimeType(image.path)
            typeT.text = type
            // Modified
            // Convert from ms to time
            val date: Calendar = GregorianCalendar()
            date.timeInMillis = image.lastModified()
            // Format time as e.g. "Fri Feb 17 07:45:42 PST 2017"
            val sbu = StringBuilder()
            val fmt = Formatter(sbu)
            fmt.format("%tc", date.time)
            modifiedT.text = sbu

            // Create and show dialog
            val builder = AlertDialog.Builder(this@VideoActivity)
            builder.setView(layout)
                    .setTitle(R.string.details_video)
                    .setIcon(R.drawable.ic_information_outline_black_48dp)
                    .setPositiveButton(R.string.action_ok) { _, _ ->
                        // Close AlertDialog
                    }
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun deleteVideo(video: File) {
        runOnUiThread {
            val builder = AlertDialog.Builder(this@VideoActivity)
            builder.setTitle(R.string.delete_title)
                    .setMessage(R.string.delete_message)
                    .setIcon(R.drawable.ic_warning_black_24dp)
                    .setPositiveButton(R.string.delete_ok) { _, _ ->
                        if (video.exists()) {
                            // Remove file from device
                            if (video.delete()) {
                                // Add deleted file position and send it as Extra
                                val fileToDelete = positionIntent
                                val intent = Intent()
                                intent.putExtra("file", fileToDelete)
                                // Set result of activity to 1 -> File deleted
                                setResult(1, intent)
                                // Remove image from MediaStore
                                sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(File(videoPath!!))))
                                // Finish activity
                                finish()
                            }
                        }
                    }
                    .setNegativeButton(R.string.delete_no) { _, _ ->
                        // Do nothing
                    }
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun initExoPlayer() {
        // Produces DataSource instances through which media data is loaded.
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "Gallery"))
        // This is the MediaSource representing the media to be played.
        val videoSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.fromFile(File(videoPath!!)))
        // Prepare the player with the source.
        player!!.prepare(videoSource)
        player!!.playWhenReady = true
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        video_view!!.hideController()
        // Hide UI first
        val actionBar = supportActionBar
        actionBar?.hide()
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar
        mHideHandler.removeCallbacks(mShowRunnable)
        mHideHandler.post(mHideRunnable)
    }

    private fun show() {
        video_view!!.showController()
        // Show the system bar
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.post(mShowRunnable)
    }
}