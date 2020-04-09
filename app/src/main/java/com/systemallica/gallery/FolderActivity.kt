package com.systemallica.gallery

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.systemallica.gallery.Utils.SortFilesByDate
import kotlinx.android.synthetic.main.activity_folder.*
import kotlinx.android.synthetic.main.content_folder.*
import java.io.File
import java.util.*

class FolderActivity : AppCompatActivity() {

    private val listOfFiles = ArrayList<File>()
    private val listOfPaths = ArrayList<String>()
    private var folder: String? = null
    private var gridAdapter: GridViewAdapterImages? = null
    private var columns = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_folder)
        setSupportActionBar(toolbar)

        // Get name of folder passed with the intent
        val intent = intent
        folder = intent.getStringExtra("folder")
        loadImages(folder, columns)
        if (supportActionBar != null) {
            // Set title to folder name
            supportActionBar!!.title = folder
            // Display arrow to return to previous activity
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        // Change navBar colour
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val appPrimary = ContextCompat.getColor(this, R.color.app_primary)
            window.navigationBarColor = appPrimary
        }

        // Set on swipe refresh listener
        swipelayout.setOnRefreshListener { startRefresh() }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_folder, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_increase_column -> {
                if (columns < 6) {
                    columns++
                    loadImages(folder, columns)
                }
                true
            }
            R.id.action_decrease_column -> {
                if (columns > 1) {
                    columns--
                    loadImages(folder, columns)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startRefresh() {
        swipelayout!!.isRefreshing = true
        // Refresh data
        loadImages(folder, columns)
        // Stop the refreshing indicator
        swipelayout!!.isRefreshing = false
    }

    private fun loadImages(folder: String?, columns: Int) {

        // Define the cursor and get path and bitmap of images
        var cursor: Cursor?
        var columnIndexData: Int
        var pathOfImage: String?
        var pathOfVideo: String?

        // Initialisations
        listOfFiles.clear()
        listOfPaths.clear()

        // Images-------------------------------------------------------------------------------
        var uri: Uri? = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projectionImage = arrayOf(MediaStore.MediaColumns.DATA)
        val where = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ?"
        cursor = contentResolver.query(uri!!, projectionImage, where, arrayOf(folder),
                MediaStore.MediaColumns.DATE_ADDED + " DESC")
        if (cursor != null) {
            // Get path of image
            columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            while (cursor.moveToNext()) {
                // Get file
                pathOfImage = cursor.getString(columnIndexData)
                val imgFile = File(pathOfImage!!)
                // Add to list
                listOfFiles.add(imgFile)
            }
            // Close the cursor
            cursor.close()
        }

        // Videos-------------------------------------------------------------------------------
        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projectionVideo = arrayOf(MediaStore.MediaColumns.DATA)
        cursor = contentResolver.query(uri, projectionVideo, where, arrayOf(folder),
                MediaStore.MediaColumns.DATE_ADDED + " DESC")
        if (cursor != null) {
            // Get path of video
            columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            while (cursor.moveToNext()) {
                // Get file
                pathOfVideo = cursor.getString(columnIndexData)
                val imgFile = File(pathOfVideo!!)
                // Add to list
                listOfFiles.add(imgFile)
            }
            // Close the cursor
            cursor.close()
        }

        //Collections.sort(listOfPaths, new Utils.SortFilesByDate());
        Collections.sort(listOfFiles, SortFilesByDate())
        // Create list of files' paths
        for (file in listOfFiles) {
            listOfPaths.add(file.path)
        }

        // Set number of columns
        gridViewFolder!!.numColumns = columns
        // Create and set the adapter (context, layout_of_image, list_of_images)
        gridAdapter = GridViewAdapterImages(this@FolderActivity,
                R.layout.grid_item_layout_image,
                listOfFiles,
                columns)
        gridViewFolder!!.adapter = gridAdapter
        gridAdapter!!.notifyDataSetChanged()

        // OnClick listener
        gridViewFolder!!.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            if (Utils.isVideo(listOfPaths[position])) {
                // Create video intent
                val intent = Intent(this@FolderActivity, VideoActivity::class.java)
                // Pass arrayList of image paths
                intent.putExtra("position", position)
                intent.putExtra("videoPath", listOfPaths[position])
                // Start activity
                startActivityForResult(intent, 2)
            } else {
                // Create image intent
                val intent = Intent(this@FolderActivity, ImageActivity::class.java)
                // Pass arrayList of image paths
                intent.putExtra("position", position)
                intent.putExtra("listOfImages", listOfPaths)
                // Start activity
                startActivityForResult(intent, 1)
            }
        }

        // OnLongClick listener
        gridViewFolder!!.onItemLongClickListener = OnItemLongClickListener { _, grid_item, _, _ ->
            // Get layout
            val layout = grid_item as ViewGroup
            // Get "check" ImageView
            val check = layout.getChildAt(2) as ImageView
            // Toggle "check"
            if (check.visibility == View.VISIBLE) {
                check.visibility = View.GONE
            } else {
                check.visibility = View.VISIBLE
            }
            true
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            // One image was deleted
            if (resultCode == 1) {
                val position = data!!.getIntExtra("file", 0)
                listOfPaths.remove(listOfPaths[position])
                listOfFiles.remove(listOfFiles[position])
                gridAdapter!!.notifyDataSetChanged()
                // One pic deleted
                setResult(2)
            } else if (resultCode == 2) {
                val position = data!!.getIntExtra("file", 0)
                listOfPaths.remove(listOfPaths[position])
                listOfFiles.remove(listOfFiles[position])
                gridAdapter!!.notifyDataSetChanged()
                // Reload
                startRefresh()
                // Show message
                Snackbar.make(findViewById(R.id.folder), getString(R.string.no_images), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
                // Folder emptied
                setResult(1)
            } else if (resultCode == 4) {
                // One image was renamed
                val position = data!!.getIntExtra("file", 0)
                listOfPaths.remove(listOfPaths[position])
                listOfFiles.remove(listOfFiles[position])
                gridAdapter!!.notifyDataSetChanged()
                // Reload
                startRefresh()
                setResult(2)
            } else if (resultCode == 5) {
                // Reload
                startRefresh()
                setResult(2)
            }
        } else if (requestCode == 2) {
            if (resultCode == 1) {
                // One video was deleted/renamed
                val position = data!!.getIntExtra("file", 0)
                listOfPaths.remove(listOfPaths[position])
                listOfFiles.remove(listOfFiles[position])
                gridAdapter!!.notifyDataSetChanged()
                // Reload
                startRefresh()
                // Set result
                setResult(2)
            }
        }
    }
}