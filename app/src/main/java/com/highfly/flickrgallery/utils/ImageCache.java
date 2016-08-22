package com.highfly.flickrgallery.utils;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created By: Ann Ngoc Nguyen
 * Description: Image Cache for reuse
 */
public class ImageCache extends LruCache<String, Bitmap> {
    public static ImageCache sImageCache;

    private ImageCache(int maxSize) {
        super(maxSize);
    }

    public static ImageCache getInstance(int maxSize){
        if(sImageCache == null){
            sImageCache = new ImageCache(maxSize);
        }

        return sImageCache;
    }

    @Override
    protected int sizeOf(String key, Bitmap value){
        return value.getByteCount();
    }

    public static void putBitmapToCache(String url, Bitmap value){
        if(sImageCache == null) return;

        sImageCache.put(url, value);
    }

    public static Bitmap getBitmapFromCache(String url){
        if(sImageCache == null) return null;
        if(url == null) return null;
        return sImageCache.get(url);
    }
}
