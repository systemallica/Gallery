package com.systemallica.gallery;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.systemallica.gallery.Utils.dpToPx;

class GridViewAdapterImages extends ArrayAdapter<File> {
    private Context context;
    private int layoutResourceId;
    private ArrayList data = new ArrayList();
    private int columns;

    GridViewAdapterImages(Context context, int layoutResourceId, ArrayList<File> data, int n_columns) {
        super(context, 0, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        this.columns = n_columns;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.image = row.findViewById(R.id.image);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        // Get device screen size
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        //float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int pxWidth = dpToPx((int)dpWidth, getContext());

        // Change image size
        holder.image.getLayoutParams().height = pxWidth/columns;
        holder.image.getLayoutParams().width = pxWidth/columns;

        // Get current file
        File item = (File) data.get(position);

        // Set image, thumbnail to 0.1x resolution, center-cropped, cached
        GlideApp
                .with(context)
                .load(item)
                .thumbnail(0.1f)
                .centerCrop()
                .transition(withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(holder.image);

        return row;
    }

    private static class ViewHolder {
        ImageView image;
    }
}
