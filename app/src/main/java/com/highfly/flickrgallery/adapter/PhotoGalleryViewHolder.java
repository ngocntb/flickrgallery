package com.highfly.flickrgallery.adapter;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.highfly.flickrgallery.R;

/**
 *    Created By: Ann Ngoc Nguyen
 *    Description: This is ViewHolder for PhotoGalleryViewAdapter
 **/
public class PhotoGalleryViewHolder extends RecyclerView.ViewHolder {
    private ImageView mImageView;
    private Bitmap mBitmap;


    public PhotoGalleryViewHolder(View view) {
        super(view);
        mImageView = (ImageView) view.findViewById(R.id.gallery_item_imageView);
    }

    public ImageView getImageView(){
        return mImageView;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setPhotoPageUrl(Bitmap bitmap) {
        mBitmap = bitmap;
    }
}
