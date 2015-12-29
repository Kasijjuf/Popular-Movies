package com.kasijjuf.udacity.popularmovies;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

class ImageAdapter extends BaseAdapter {

    /*// DEBUG Log tag
    private final String LOG_TAG = ImageAdapter.class.getSimpleName();*/

    private final Context mContext;
    private final String[] mImageURLs;

    public ImageAdapter(Context c, String[] imageURLs) {
        super();
        mContext = c;
        mImageURLs = imageURLs;
    }

    @Override
    public int getCount() {
        return mImageURLs.length;
    }

    @Override
    public Object getItem(int position) {  // HMMM Do I need to implement this method?
        return mImageURLs[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //TODO Maybe rework this method to not declare the imageView
        ImageView imageView;

        if (convertView == null) {
            imageView = new ImageView(mContext);

            //TODO initialize other ImageView attributes here as necessary
            imageView.setPadding(0, 0, 0, 0);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE); // HMMM Which ScaleType do I need?

            /*// DEBUG getAdjustViewBounds default state
            if (imageView.getAdjustViewBounds()) {
                Log.d(LOG_TAG, "android:adjustViewBounds is true");
            } else {
                Log.d(LOG_TAG, "android:adjustViewBounds is false");
            }*/

        } else {
            imageView = (ImageView) convertView;
        }

        Picasso.with(mContext)
                .load(mImageURLs[position])
                .into(imageView);

        return imageView;
    }
}
