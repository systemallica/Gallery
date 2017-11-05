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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.Locale;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.systemallica.gallery.Utils.dpToPx;

class GridViewAdapterFolders extends ArrayAdapter<FolderItem> {
    private Context context;
    private int layoutResourceId;
    private ArrayList data = new ArrayList();
    private int columns;

    GridViewAdapterFolders(Context context, int layoutResourceId, ArrayList<FolderItem> data, int n_columns) {
        super(context, 0, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        this.columns = n_columns;
    }

    @Override
    @NonNull public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.imageTitle = row.findViewById(R.id.folder_name);
            holder.imageCount = row.findViewById(R.id.n_images);
            holder.image = row.findViewById(R.id.image);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        LinearLayout lv = row.findViewById(R.id.text_layout);

        // Get device screen size
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        //float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int pxWidth = dpToPx((int)dpWidth, getContext());

        // Change image size
        holder.image.getLayoutParams().height = pxWidth/columns;
        holder.image.getLayoutParams().width = pxWidth/columns;
        // Change text container size
        lv.getLayoutParams().width = pxWidth/columns;

        // Get current FolderItem
        FolderItem item = (FolderItem) data.get(position);
        Log.e("position: ", Integer.toString(position));
        // Set title and count
        holder.imageTitle.setText(item.getTitle());
        holder.imageCount.setText(String.format(Locale.ENGLISH, "%d", item.getCount()));
        Log.e("image to load: ", item.getImage().getAbsolutePath());
        // Set image, thumbnail to 0.1x resolution, center-cropped, cached
        GlideApp
                .with(context)
                .load(item.getImage())
                .thumbnail(0.1f)
                .centerCrop()
                .transition(withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(holder.image);

        return row;
    }

    private static class ViewHolder {
        ImageView image;
        TextView imageTitle;
        TextView imageCount;
    }
}