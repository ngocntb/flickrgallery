package com.highfly.flickrgallery.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.widget.ImageView;

import com.highfly.flickrgallery.SwipeFragment;
import com.highfly.flickrgallery.entity.ParcelableItem;
import com.highfly.flickrgallery.thread.ImageCacheDownloader;
import com.highfly.flickrgallery.thread.ImageDownloader;
import com.highfly.flickrgallery.utils.ImageCache;

import java.util.ArrayList;

/**
 *    Created By: Ann Ngoc Nguyen
 *    Description: This is Adapter for ViewPager in PhotoActivity
 **/
public class PhotoFragmentPagerAdapter extends FragmentStatePagerAdapter {
    private ArrayList<ParcelableItem> mItems;
    private ImageDownloader<ImageView> mImageDownloader;
    private ImageCacheDownloader mCacheDownloader;

    public PhotoFragmentPagerAdapter(FragmentManager fm,
                                     ArrayList<ParcelableItem> items,
                                     ImageDownloader<ImageView> imageDownloader,
                                     ImageCacheDownloader cacheDownloader) {
        super(fm);
        mItems = items;
        mImageDownloader = imageDownloader;
        mCacheDownloader = cacheDownloader;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Fragment getItem(int position) {
        ParcelableItem item = mItems.get(position);
        SwipeFragment fragment = SwipeFragment.newInstance(item.getId(),
                item.getAuthorId(),
                item.getPhotoUrl());
        fragment.setCacheDownloader(mCacheDownloader);
        fragment.setImageDownloader(mImageDownloader);

        for (int i = position; i < position + 10 && i < mItems.size(); i++) {
            String photoUrl = mItems.get(i).getPhotoUrl();
            if (ImageCache.getBitmapFromCache(photoUrl) == null) {
                mCacheDownloader.queueCacheThumbnail(mItems.get(i).getPhotoUrl());
            }
        }

        return fragment;
    }
}
