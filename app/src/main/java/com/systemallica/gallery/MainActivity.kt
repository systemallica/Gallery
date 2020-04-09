package com.systemallica.gallery

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.AbsListView
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.systemallica.gallery.Utils.MediaFileFilter
import com.systemallica.gallery.Utils.SortFoldersByName
import kotlinx.android.synthetic.main.content_main.*
import java.io.File
import java.util.*
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private val permissionRequestCode = 114
    private var columns = 2
    private var folderPosition = 0
    private var listOfFolders = ArrayList<FolderItem>()
    private var listOfFoldersI = ArrayList<FolderItem>()
    private var listOfFoldersV = ArrayList<FolderItem>()
    private var listOfFolderNames = ArrayList<String?>()
    private var listOfFolderNamesI = ArrayList<String>()
    private var listOfFolderNamesV = ArrayList<String>()
    private var gridAdapter: GridViewAdapterFolders? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Check for sdk >= 23
        if (Build.VERSION.SDK_INT >= 23) {
            // Check CAMERA and MEDIA permission
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        permissionRequestCode)
                // When permissions are granted
            } else {
                setFABListener()
                loadFolders(columns)
            }
        } else {
            setFABListener()
            loadFolders(columns)
        }
        // Set on swipe refresh listener
        swipelayout?.setOnRefreshListener { startRefresh() }

        // Change navBar colour
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val appPrimary = ContextCompat.getColor(this, R.color.app_primary)
            window.navigationBarColor = appPrimary
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        return when (id) {
            R.id.action_settings -> {
                Snackbar.make(findViewById(R.id.main), "Settings", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
                true
            }
            R.id.action_increase_column -> {
                if (columns < 6) {
                    columns++
                    loadFolders(columns)
                }
                true
            }
            R.id.action_decrease_column -> {
                if (columns > 1) {
                    columns--
                    loadFolders(columns)
                }
                true
            }
            R.id.action_about ->                 // Start AboutActivity
                // TODO
                true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == permissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                setFABListener()
                loadFolders(columns)
            } else if (grantResults.isNotEmpty() && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(findViewById(R.id.main), "Camera button won't work",
                        Snackbar.LENGTH_LONG).setAction("Action", null).show()
                loadFolders(columns)
            } else {
                Snackbar.make(findViewById(R.id.main), "App can't work... closing",
                        Snackbar.LENGTH_LONG).setAction("Action", null).show()
                exitProcess(0)
            }
        }
    }

    private fun startRefresh() {
        swipelayout!!.isRefreshing = true
        // Refresh data
        loadFolders(columns)
        // Stop the refreshing indicator
        swipelayout!!.isRefreshing = false
    }

    private fun loadFolders(columns: Int) {

        // Define the cursor and get path and bitmap of images
        var cursor: Cursor?
        var columnIndexData: Int
        var columnIndexFolderName: Int
        var pathOfImage: String?
        var pathOfVideo: String?
        var folderName: String
        var isEmpty = false

        // Initialisations
        listOfFolders.clear()
        listOfFoldersI.clear()
        listOfFoldersV.clear()
        listOfFolderNames.clear()
        listOfFolderNamesI.clear()
        listOfFolderNamesV.clear()

        // Images-----------------------------------------------------------------------------------
        var uri: Uri? = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projectionImage = arrayOf(MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        cursor = contentResolver.query(uri!!, projectionImage, null, null,
                MediaStore.MediaColumns.DATE_ADDED + " DESC")
        if (cursor != null) {
            if (cursor.count != 0) {
                // Get path of image
                columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                // Get folder name
                columnIndexFolderName = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    pathOfImage = cursor.getString(columnIndexData)
                    folderName = cursor.getString(columnIndexFolderName)
                    if (!listOfFolderNamesI.contains(folderName)) {
                        listOfFolderNamesI.add(folderName)
                        val imgFile = File(pathOfImage!!)
                        // Get number of media in folder
                        var files_in_folder: Int
                        if (Objects.requireNonNull(imgFile.parentFile).listFiles(MediaFileFilter()) != null) {
                            files_in_folder = imgFile.parentFile?.listFiles(MediaFileFilter())!!.size
                        } else {
                            files_in_folder = 0
                        }
                        // Add to list
                        listOfFoldersI.add(FolderItem(imgFile, folderName, files_in_folder))
                    }
                }
                // Close the cursor
                cursor.close()
            } else {
                isEmpty = true
            }
        }

        // Videos-----------------------------------------------------------------------------------
        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projectionVideo = arrayOf(MediaStore.MediaColumns.DATA,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
        cursor = contentResolver.query(uri, projectionVideo, null, null,
                MediaStore.MediaColumns.DATE_ADDED + " DESC")
        if (cursor != null) {
            if (cursor.count != 0) {
                isEmpty = false
                // Get path of video
                columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                // Get folder name
                columnIndexFolderName = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    pathOfVideo = cursor.getString(columnIndexData)
                    folderName = cursor.getString(columnIndexFolderName)
                    if (!listOfFolderNamesV.contains(folderName)) {
                        listOfFolderNamesV.add(folderName)
                        val imgFile = File(pathOfVideo!!)
                        // Get number of media in folder
                        var filesInFolder: Int
                        filesInFolder = if (imgFile.parentFile?.listFiles(MediaFileFilter()) != null) {
                            imgFile.parentFile.listFiles(MediaFileFilter()).size
                        } else {
                            0
                        }
                        // Add to list
                        listOfFoldersV.add(FolderItem(imgFile, folderName, filesInFolder))
                    }
                }
                // Close the cursor
                cursor.close()
            } else {
                isEmpty = true
            }
        }
        if (isEmpty) {
            Snackbar.make(findViewById(R.id.main), getString(R.string.no_media), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        } else {
            // Compare the results of both queries and join them together in a single sorted list
            var match: Boolean

            // For every folder with videos
            for (j in listOfFoldersV.indices) {
                match = false
                // For every folder with images
                for (i in listOfFoldersI.indices) {
                    // If the folder contains both videos and images
                    if (listOfFoldersV[j].title == listOfFoldersI[i].title) {
                        match = true
                        // If the video is more recent than the image
                        if (listOfFoldersV[j].image.lastModified() > listOfFoldersI[i].image.lastModified()) {
                            // Add video to list
                            listOfFolders.add(listOfFoldersV[j])
                            listOfFolderNames.add(listOfFoldersV[j].title)
                            break
                        } else {
                            // Add image to list
                            listOfFolders.add(listOfFoldersI[i])
                            listOfFolderNames.add(listOfFoldersI[i].title)
                            break
                        }
                    }
                }
                // If the folder only contains videos
                if (!match) {
                    // Add video to list
                    listOfFolders.add(listOfFoldersV[j])
                    listOfFolderNames.add(listOfFoldersV[j].title)
                }
            }
            // For every folder with images
            for (z in listOfFoldersI.indices) {
                // If the folder only contains images
                if (!listOfFolderNames.contains(listOfFoldersI[z].title)) {
                    // Add image to list
                    listOfFolders.add(listOfFoldersI[z])
                    listOfFolderNames.add(listOfFoldersI[z].title)
                }
            }
            Collections.sort(listOfFolders, SortFoldersByName())
            // Set number of columns
            gridView.numColumns = columns
            // Create and set the adapter (context, layout_of_image, listOfFolders)
            gridAdapter = GridViewAdapterFolders(this@MainActivity,
                    R.layout.grid_item_layout_folder,
                    listOfFolders,
                    columns)
            gridView.adapter = gridAdapter
            gridAdapter!!.notifyDataSetChanged()
            // OnClick listener
            gridView.onItemClickListener = OnItemClickListener { _, _, position, _ ->
                folderPosition = position
                // Create intent
                val intent = Intent(baseContext, FolderActivity::class.java)
                intent.putExtra("folder", listOfFolders[position].title)
                // Start activity
                startActivityForResult(intent, 1)
            }
            setFABScrollListener()
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == 1) {
                listOfFolders.removeAt(folderPosition)
                gridAdapter!!.notifyDataSetChanged()
            }
            if (resultCode == 2) {
                startRefresh()
            }
        }
    }

    private fun setFABListener() {
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent("android.media.action.IMAGE_CAPTURE")
            startActivity(intent)
        }
    }

    private fun setFABScrollListener() {
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        gridView.setOnScrollListener(object : AbsListView.OnScrollListener {
            var mLastFirstVisibleItem = 0

            // Called at end of scrolling action
            override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int,
                                  totalItemCount: Int) {
                // Do nothing
            }

            // Called at beginning of scrolling action
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                if (view.id == view.id) {
                    val currentFirstVisibleItem = view.firstVisiblePosition
                    if (currentFirstVisibleItem > mLastFirstVisibleItem) {
                        // Scrolling down
                        fab.hide()
                    } else if (currentFirstVisibleItem < mLastFirstVisibleItem) {
                        // Scrolling up
                        fab.show()
                    }
                    mLastFirstVisibleItem = currentFirstVisibleItem
                }
            }
        })
    }
}