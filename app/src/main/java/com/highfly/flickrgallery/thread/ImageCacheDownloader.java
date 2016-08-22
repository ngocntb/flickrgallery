package com.highfly.flickrgallery.thread;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.highfly.flickrgallery.network.FlickrFetchr;
import com.highfly.flickrgallery.utils.ImageCache;

import java.io.IOException;

/**
 * Created By: Ann Ngoc Nguyen
 * Description: Handler Thread to download Image before it is needed, then put into ImageCache
 */
public class ImageCacheDownloader extends HandlerThread {
    private static final String TAG = "ImageCacheDownloader";
    private static final int MESSAGE_CACHE_DOWNLOADED = 1;
    private Handler mHandler;
    private Context mContext;

    @Override
    public void onLooperPrepared() {
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                if (msg.what == MESSAGE_CACHE_DOWNLOADED) {
                    String url = (String) msg.obj;
                    handleRequest(url);
                }
            }
        };
    }

    public ImageCacheDownloader(Handler handler, Context context){
        super(TAG);
        mContext = context;
    }

    public void queueCacheThumbnail(String url){
        Log.i(TAG, "Got an URL for cache: " + url);
        mHandler.obtainMessage(MESSAGE_CACHE_DOWNLOADED, url).sendToTarget();
    }


    private void handleRequest(final String url){
        try{
            if (url == null && ImageCache.getBitmapFromCache(url) != null) return;
            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
            if(bitmapBytes == null) return;
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            ImageCache.putBitmapToCache(url, bitmap);
            Log.i(TAG, "Bitmap is added to cache: " + url);
        }
        catch(IOException ioe){
            Log.e(TAG, "Error downloading image ", ioe);
        }
    }

    public void clearCacheQueue() {
        mHandler.removeMessages(MESSAGE_CACHE_DOWNLOADED);
    }
}
