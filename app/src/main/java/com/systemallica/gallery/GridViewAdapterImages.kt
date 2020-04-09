package com.systemallica.gallery

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import java.io.File
import java.util.*

class GridViewAdapterImages(private val cont: Context, private val layoutResourceId: Int, data: ArrayList<File>, n_columns: Int) : ArrayAdapter<File>(cont, 0, data) {

    private val data: ArrayList<*>
    private val columns: Int

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        val holder: ViewHolder
        if (row == null) {
            val inflater = (cont as Activity).layoutInflater
            row = inflater.inflate(layoutResourceId, parent, false)
            holder = ViewHolder()
            holder.image = row.findViewById(R.id.inside_imageview)
            holder.overlay = row.findViewById(R.id.outside_imageview)
            holder.check = row.findViewById(R.id.check_imageview)
            row.tag = holder
        } else {
            holder = row.tag as ViewHolder
        }

        // Get device screen size
        val displayMetrics = context.resources.displayMetrics
        //float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        val pxWidth = Utils.dpToPx(dpWidth.toInt(), context)

        // Change image and overlay size
        holder.image!!.layoutParams.height = pxWidth / columns
        holder.image!!.layoutParams.width = pxWidth / columns
        holder.overlay!!.layoutParams.height = pxWidth / (columns + 5)
        holder.overlay!!.layoutParams.width = pxWidth / (columns + 5)
        holder.check!!.layoutParams.height = pxWidth / (columns + 5)
        holder.check!!.layoutParams.width = pxWidth / (columns + 5)

        // Get current file
        val item = data[position] as File

        // If it's a video, add overlay
        if (Utils.isVideo(item.name)) {
            holder.overlay!!.visibility = View.VISIBLE
        } else {
            holder.overlay!!.visibility = View.INVISIBLE
        }

        // Set image, thumbnail to 0.1x resolution, center-cropped, cached
        Glide
             .with(cont)
             .load(item)
             .thumbnail(0.1f)
             .centerCrop()
             .transition(DrawableTransitionOptions.withCrossFade())
             .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
             .into(holder.image!!)
        return row!!
    }

    private class ViewHolder {
        var image: ImageView? = null
        var overlay: ImageView? = null
        var check: ImageView? = null
    }

    init {
        this.data = data
        columns = n_columns
    }
}