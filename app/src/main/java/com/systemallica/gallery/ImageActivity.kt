package com.systemallica.gallery

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import kotlinx.android.synthetic.main.activity_image.*
import kotlinx.android.synthetic.main.rename_dialog.*
import java.io.File
import java.util.*

class ImageActivity : AppCompatActivity() {
    var listOfImages = ArrayList<String>()
    var mPagerAdapter: PagerAdapter? = null
    var positionArray = 0
    private val mHideHandler = Handler()
    private val mHideRunnable = Runnable {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }
    private val mShowRunnable = Runnable { // Display of UI elements
        val actionBar = supportActionBar
        actionBar?.show()
    }
    private var mVisible = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val intent = intent
        // Get all the images in the folder
        listOfImages = intent.getStringArrayListExtra("listOfImages")
        // Get position
        val positionIntent = intent.getIntExtra("position", 0)
        // Instantiate a ViewPager and a PagerAdapter.
        mPagerAdapter = CustomPagerAdapter()
        // Set offScreenLimit
        pager.offscreenPageLimit = 4
        // Set the adapter
        pager.adapter = mPagerAdapter
        // Set current position
        pager.currentItem = positionIntent
        // Set onPageChangeListener
        pager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                // Set title to current image's name
                positionArray = pager.currentItem
                toolbar.title = Utils.getBaseName(File(listOfImages[positionArray]))
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        if (supportActionBar != null) {
            // Display arrow to return to previous activity
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            // Hide it by default
            supportActionBar!!.hide()
            // Set current title
            supportActionBar!!.title = Utils.getBaseName(File(listOfImages[positionIntent]))
        }

        // Make navBar translucent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val translucentBackground = ContextCompat.getColor(this, R.color.translucent_background)
            window.navigationBarColor = translucentBackground
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide()
        hide()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 99) {
            if (resultCode == 1) {
                // Update UI
                deleteVideo()
            } else if (resultCode == 2) {
                // Change toolbar text
                val name = data!!.getStringExtra("name")
                val toolbar = findViewById<Toolbar>(R.id.toolbar)
                toolbar.title = name
                mPagerAdapter!!.notifyDataSetChanged()
                // Reload images when leaving activity
                setResult(5)
            }
        }
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        val actionBar = supportActionBar
        actionBar?.hide()
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar
        mHideHandler.removeCallbacks(mShowRunnable)
        mHideHandler.post(mHideRunnable)
    }

    private fun show() {
        // Show the system bar
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.post(mShowRunnable)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    internal inner class CustomPagerAdapter : PagerAdapter() {
        override fun getCount(): Int {
            return listOfImages.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_NONE
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val image = File(listOfImages[position])

            // Inflate layout
            val inflater = layoutInflater
            val layout = inflater.inflate(R.layout.view_pager_item, container, false)
            // Get overlay ImageView
            val overlay = layout.findViewById<ImageView>(R.id.outside_imageview)

            // Video/gif thumbnail
            if (Utils.isVideoOrGif(image.name)) {
                val imageView = layout.findViewById<ImageView>(R.id.inside_imageview)
                Glide
                        .with(applicationContext)
                        .load(image)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(imageView)
                if (Utils.isVideo(image.name)) {
                    val subSampling: SubsamplingScaleImageView = layout.findViewById(R.id.inside_imageview_sub)

                    // Add overlay
                    overlay.visibility = View.VISIBLE
                    // Play video on tap
                    overlay.setOnClickListener { // Create video intent
                        val intent = Intent(this@ImageActivity, VideoActivity::class.java)
                        // Pass arrayList of image paths
                        intent.putExtra("position", position)
                        intent.putExtra("videoPath", image.absolutePath)
                        // Start activity
                        startActivityForResult(intent, 99)
                    }
                    // Set up the user interaction to manually show or hide the system UI.
                    subSampling.setOnClickListener { toggle() }
                    imageView.setOnClickListener { toggle() }
                } else {
                    // Set up the user interaction to manually show or hide the system UI.
                    imageView.setOnClickListener { toggle() }
                }
                // Full image display
            } else {
                // Get subSampling view
                val imageView: SubsamplingScaleImageView = layout.findViewById(R.id.inside_imageview_sub)
                // Use EXIF rotation
                imageView.orientation = SubsamplingScaleImageView.ORIENTATION_USE_EXIF
                // Allow more zooming
                imageView.setMinimumDpi(10)
                // Set image
                imageView.setImage(ImageSource.uri(image.path))
                // Hide overlay
                overlay.visibility = View.INVISIBLE
                // Set up the user interaction to manually show or hide the system UI.
                imageView.setOnClickListener { toggle() }
            }
            container.addView(layout)
            return layout
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_image, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        positionArray = pager!!.currentItem
        val file = File(listOfImages[positionArray])
        return when (id) {
            R.id.action_share -> {
                shareImage(file)
                true
            }
            R.id.action_delete -> {
                deleteImage(file)
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
            val builder = AlertDialog.Builder(this@ImageActivity)
            builder.setView(layout)
                    .setTitle(R.string.rename_rename)
                    .setPositiveButton(R.string.action_ok) { _, _ ->
                        // Change toolbar text
                        val toolbar = findViewById<Toolbar>(R.id.toolbar)
                        toolbar.title = new_name.text.toString()
                        // Build path with new name
                        val newName: String
                        val folderPath = Objects.requireNonNull(file.parentFile).path
                        newName = folderPath + "/" + new_name.text.toString() + Utils.getExtension(file)
                        val newFile = File(newName)
                        // Rename the file and set activity result
                        if (file.renameTo(newFile)) {
                            //Remove image from MediaStore
                            sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))
                            //Add new file to MediaStore
                            sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(newFile)))
                            // Reload pager
                            listOfImages.removeAt(positionArray)
                            listOfImages.add(positionArray, newFile.path)
                            mPagerAdapter!!.notifyDataSetChanged()
                            // Send intent
                            val intent = Intent()
                            intent.putExtra("file", positionArray)
                            // Set result of activity
                            setResult(4, intent)
                        }
                    }
                    .setNegativeButton(R.string.action_cancel) { _, _ ->
                        // Close AlertDialog
                    }
            val dialog = builder.create()
            dialog.show()
        }
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
                sizeText = " kB"
                if (sizeD > 1024) {
                    sizeD /= 1024 //MB
                    sizeText = " MB"
                    if (sizeD > 1024) {
                        sizeD /= 1024 //GB
                        sizeText = " GB"
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
            val builder = AlertDialog.Builder(this@ImageActivity)
            builder.setView(layout)
                    .setTitle(R.string.details_image)
                    .setIcon(R.drawable.ic_information_outline_black_48dp)
                    .setPositiveButton(R.string.action_ok) { _, _ ->
                        // Close AlertDialog
                    }
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun shareImage(image: File) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(image))
        startActivity(Intent.createChooser(shareIntent, "Share image using"))
    }

    private fun deleteImage(image: File) {
        runOnUiThread {
            val builder = AlertDialog.Builder(this@ImageActivity)
            builder.setTitle(R.string.delete_title)
                    .setMessage(R.string.delete_message)
                    .setIcon(R.drawable.ic_warning_black_24dp)
                    .setPositiveButton(R.string.delete_ok) { dialog, which ->
                        if (image.exists()) {
                            // Remove file from device
                            if (image.delete()) {
                                // Add deleted file position to ArrayList and send it as Extra
                                val fileToDelete = positionArray
                                val intent = Intent()
                                intent.putExtra("file", fileToDelete)
                                // Set result of activity to 1 -> File deleted
                                setResult(1, intent)
                                //Remove image from MediaStore
                                sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(File(listOfImages[positionArray]))))
                                // Remove image from arrayList
                                listOfImages.removeAt(positionArray)
                                // Notify data changed
                                mPagerAdapter!!.notifyDataSetChanged()
                                // Set adapter position
                                if (positionArray != 0) {
                                    val moveTo = positionArray - 1
                                    pager!!.setCurrentItem(moveTo, true)
                                } else {
                                    if (listOfImages.size == 0) {
                                        // Set result of activity to 2 -> Last file of folder was deleted
                                        setResult(2, intent)
                                        finish()
                                    }
                                }
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

    private fun deleteVideo() {
        runOnUiThread {
            // Add deleted file position to ArrayList and send it as Extra
            val fileToDelete = positionArray
            val intent = Intent()
            intent.putExtra("file", fileToDelete)
            // Set result of activity to 1 -> File deleted
            setResult(1, intent)
            // Remove image from arrayList
            listOfImages.removeAt(positionArray)
            // Notify data changed
            mPagerAdapter!!.notifyDataSetChanged()
            // Set adapter position
            if (positionArray != 0) {
                val moveTo: Int = positionArray - 1
                pager!!.setCurrentItem(moveTo, true)
            } else {
                if (listOfImages.size == 0) {
                    // Set result of activity to 2 -> Last file of folder was deleted
                    setResult(2, intent)
                    finish()
                }
            }
        }
    }
}