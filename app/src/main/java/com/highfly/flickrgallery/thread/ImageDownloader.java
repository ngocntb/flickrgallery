package com.highfly.flickrgallery.thread;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import com.highfly.flickrgallery.network.FlickrFetchr;
import com.highfly.flickrgallery.utils.ImageCache;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created By: Ann Ngoc Nguyen
 * Description: Handler Thread to download Image, then put into ImageCache
 */
public class ImageDownloader<Token> extends HandlerThread{

    private static final String TAG = "ImageDownloader";
    private static final int MESSAGE_DOWNLOADED = 0;
    Handler mHandler;
    Map<Token, String> requestMap = Collections.synchronizedMap(new HashMap<Token, String>());
    Handler mResponseHandler;
    Listener<Token> mListener;
    private AnimationDrawable mLoadingAnim = null;
    private ImageView mLoadingView = null;
    private Context mContext;

    public interface Listener<Token>{
        void onThumbnailDownloaded(Token token, Bitmap thumbnail,
                                   AnimationDrawable loadingAnim, ImageView loadingView);
    }

    public void setListen(Listener<Token> listener){
        mListener = listener;
    }

    @Override
    public void onLooperPrepared() {
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                if(msg.what == MESSAGE_DOWNLOADED){
                    Token token = (Token) msg.obj;
                    Log.i(TAG, "Got a request for url: " + requestMap.get(token));
                    handleRequest(token, mLoadingAnim, mLoadingView);
                }
            }
        };
    }

    public ImageDownloader(Handler handler, Context context){
        super(TAG);
        mResponseHandler = handler;
        mContext = context;

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int memoryClass = am.getMemoryClass() * 1024 * 1024;
        ImageCache.getInstance(memoryClass / 8);
    }

    public void queueThumbnail(Token token, String url, AnimationDrawable loadingAnim, ImageView loadingView){
        Log.i(TAG, "Got an URL: " + url);
        mLoadingAnim = loadingAnim;
        mLoadingView = loadingView;
        requestMap.put(token, url);
        mHandler.obtainMessage(MESSAGE_DOWNLOADED, token).sendToTarget();
    }

    private void handleRequest(final Token token, final AnimationDrawable loadingAnim, final ImageView loadingView) {
        final String url = requestMap.get(token);
        try{

            final Bitmap bitmap;
            if(url == null) return;
            if(ImageCache.getBitmapFromCache(url) != null){
                bitmap = ImageCache.getBitmapFromCache(url);
            }
            else {
                byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
                if(bitmapBytes == null) return;
                bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                ImageCache.putBitmapToCache(url, bitmap);
                Log.i(TAG, "Bitmap is added to cache: " + url);
            }
            Log.i(TAG, "Bitmap created");
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (requestMap.get(token) != url) return;
                    requestMap.remove(token);
                    mListener.onThumbnailDownloaded(token, bitmap, loadingAnim, loadingView);
                }
            });
        }
        catch(IOException ioe){
            Log.e(TAG, "Error downloading image ", ioe);
        }
    }

    public void clearQueue(){
        mHandler.removeMessages(MESSAGE_DOWNLOADED);
        requestMap.clear();
    }
}
