package com.example.android.myapplication;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.HashMap;


public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private String[] mMovieId;
    private String[] mPosterUrl;

    public ImageAdapter(Context c, String[] movieId, String[] posterUrl) {
        mContext = c;
        mMovieId = movieId;
        mPosterUrl = posterUrl;
    }

    public int getCount() {
        return mMovieId.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    800));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(2, 2, 2, 2);
            imageView.setTag(mMovieId[position]);
        } else {
            imageView = (ImageView) convertView;
        }

        String base_url = "http://image.tmdb.org/t/p/w185/";

        Picasso
                .with(mContext)
                .load(base_url + mPosterUrl[position])
                .fit()
                .centerCrop()
                .into(imageView);

        return imageView;
    }

}
