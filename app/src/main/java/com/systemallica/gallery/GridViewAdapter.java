package com.systemallica.gallery;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

class GridViewAdapter extends ArrayAdapter<ImageItem> {
    private Context context;
    private int layoutResourceId;
    private ArrayList data = new ArrayList();

    GridViewAdapter(Context context, int layoutResourceId, ArrayList<ImageItem> data) {
        super(context, 0, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
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

        // Change image size
        //holder.image.getLayoutParams().height = dpToPx((int)dpWidth, getContext());
        //holder.image.getLayoutParams().width = dpToPx((int)dpWidth, getContext());
        // Change text container size
        //lv.getLayoutParams().width = dpToPx((int)dpWidth, getContext());

        ImageItem item = (ImageItem) data.get(position);
        holder.imageTitle.setText(item.getTitle());
        holder.imageCount.setText(Integer.toString(item.getCount()));
        holder.image.setImageBitmap(item.getImage());
        return row;
    }

    private static class ViewHolder {
        ImageView image;
        TextView imageTitle;
        TextView imageCount;
    }

    private static int dpToPx(int dp, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}