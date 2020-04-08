package com.systemallica.gallery;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.systemallica.gallery.Utils.dpToPx;

class GridViewAdapterImages extends ArrayAdapter<File> {
    private Context context;
    private int layoutResourceId;
    private ArrayList data;
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
            holder.image = row.findViewById(R.id.inside_imageview);
            holder.overlay = row.findViewById(R.id.outside_imageview);
            holder.check = row.findViewById(R.id.check_imageview);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        // Get device screen size
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        //float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int pxWidth = dpToPx((int)dpWidth, getContext());

        // Change image and overlay size
        holder.image.getLayoutParams().height = pxWidth/columns;
        holder.image.getLayoutParams().width = pxWidth/columns;
        holder.overlay.getLayoutParams().height = pxWidth/(columns+5);
        holder.overlay.getLayoutParams().width = pxWidth/(columns+5);
        holder.check.getLayoutParams().height = pxWidth/(columns+5);
        holder.check.getLayoutParams().width = pxWidth/(columns+5);

        // Get current file
        File item = (File) data.get(position);

        // If it's a video, add overlay
        if(Utils.isVideo(item.getName())){
            holder.overlay.setVisibility(View.VISIBLE);
        }else{
            holder.overlay.setVisibility(View.INVISIBLE);
        }

        // Set image, thumbnail to 0.1x resolution, center-cropped, cached
        Glide
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
        ImageView overlay;
        ImageView check;
    }
}
