package com.highfly.flickrgallery.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.highfly.flickrgallery.PhotoActivity;
import com.highfly.flickrgallery.R;
import com.highfly.flickrgallery.entity.GalleryItem;
import com.highfly.flickrgallery.entity.ParcelableItem;
import com.highfly.flickrgallery.thread.ImageDownloader;
import com.highfly.flickrgallery.utils.ImageCache;

import java.util.ArrayList;

/**
 *    Created By: Ann Ngoc Nguyen
 *    Description: This is Adapter for RecyclerView in PhotoGalleryFragment
 **/
public class PhotoGalleryViewAdapter extends RecyclerView.Adapter<PhotoGalleryViewHolder> {
    private ArrayList<GalleryItem> mItems = new ArrayList<>();
    private ArrayList<ParcelableItem> mParcelables;
    private Fragment mCurrentFragment;
    private ImageDownloader mImageDownloader;
    private int lastPosition = RecyclerView.NO_POSITION;
    private Context mContext;
    private static final int MAX_ITEMS_PER_SIDE = 1000;

    public PhotoGalleryViewAdapter(ArrayList<GalleryItem> items,
                                   Fragment currentFragment,
                                   ImageDownloader imageDownloader) {
        mImageDownloader = imageDownloader;
        mCurrentFragment = currentFragment;
        mItems = items;
    }

    @Override
    public PhotoGalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.gallery_item, parent, false);
        return new PhotoGalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PhotoGalleryViewHolder holder, final int position) {
        ImageView imageView = holder.getImageView();
        imageView.setImageDrawable(null);
        GalleryItem item = mItems.get(position);
        setAnimation(imageView, position);

        Bitmap bitmap = ImageCache.getBitmapFromCache(item.getThumbnailUrl());
        if(bitmap == null) {
            mImageDownloader.queueThumbnail(imageView, item.getThumbnailUrl(), null, null);
        }
        else{
            if(mCurrentFragment.isVisible()){
                imageView.setImageBitmap(bitmap);
            }
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //    Uri photoGalleryUri = Uri.parse(holder.getPhotoPageUrl());
                Intent i = new Intent(mContext, PhotoActivity.class);
                int newPos = setParcelableArrayListExtra(position);
                i.putParcelableArrayListExtra(PhotoActivity.EXTRA_ITEMS, mParcelables);
                i.putExtra(PhotoActivity.EXTRA_SELECTED_POS, newPos);
                mContext.startActivity(i);
            }
        });
    }

    public void onReset(ArrayList<GalleryItem> items){
        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
        lastPosition = RecyclerView.NO_POSITION;
    }

    public int setParcelableArrayListExtra(int position){
        mParcelables = new ArrayList<>();
        int rightEnd = (mItems.size()- 1 - position) > MAX_ITEMS_PER_SIDE ?
                                    MAX_ITEMS_PER_SIDE + position : (mItems.size()- 1);
        int leftStart = position > (MAX_ITEMS_PER_SIDE) ? (position - MAX_ITEMS_PER_SIDE) : 0;

        for(int pos = leftStart; pos <= rightEnd; pos++){
            GalleryItem galleryItem = mItems.get(pos);
            ParcelableItem parcelableItem = new ParcelableItem(galleryItem);
            mParcelables.add(parcelableItem);
        }

        return (position-leftStart);
    }

    public void updateDataSet(ArrayList<GalleryItem> items){
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount(){
        return mItems.size();
    }


    private void setAnimation(ImageView viewToAnimate, int position){
        if (position > lastPosition){
            Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

}
