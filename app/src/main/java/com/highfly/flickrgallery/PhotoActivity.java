package com.highfly.flickrgallery;

import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.highfly.flickrgallery.adapter.PhotoFragmentPagerAdapter;
import com.highfly.flickrgallery.entity.ParcelableItem;
import com.highfly.flickrgallery.thread.ImageCacheDownloader;
import com.highfly.flickrgallery.thread.ImageDownloader;
import java.util.ArrayList;

/**
 *    Created By: Ann Ngoc Nguyen
 *    Layout: photo_activity
 *    Description: User goes from PhotoGalleryActivity to PhotoActivity (From the list to details)
 *    This activity has a View Pager that allow user to swipe to left or right to see other photos
 *    ImageDownloader is a thread to download image, CacheDownload is another thread to download images before it is needed
 *    By default, the toolbar is hided, when user tap on the screen, it is shown
 **/

public class PhotoActivity extends AppCompatActivity implements SwipeFragment.Callbacks{

    private static final String TAG = "PhotoActivity";
    public static final String EXTRA_ITEMS = "photogallery.items";
    public static final String EXTRA_SELECTED_POS = "photogallery.selected_pos";
    public static final int REQUEST_WRITE_STORAGE = 1;

    private PhotoFragmentPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;
    private ArrayList<ParcelableItem> mItems;

    private ImageDownloader<ImageView> mImageDownloader;
    private ImageCacheDownloader mCacheDownloader;
    private int mSelectedPos = 0;
    private boolean mIsHide = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_activity);

        mItems = getIntent().getParcelableArrayListExtra(EXTRA_ITEMS);
        mSelectedPos = getIntent().getIntExtra(EXTRA_SELECTED_POS, 0);

        mImageDownloader = new ImageDownloader<>(new Handler(), this);
        mImageDownloader.setListen(new ImageDownloader.Listener<ImageView>() {
            @Override
            public void onThumbnailDownloaded(ImageView imageView,
                                              Bitmap photo,
                                              AnimationDrawable loadingAnim,
                                              ImageView loadingView) {
                if(loadingAnim != null && loadingView != null) {
                    loadingAnim.stop();
                    loadingView.setVisibility(View.GONE);
                }
                imageView.setImageBitmap(photo);
            }

        });
        mImageDownloader.start();
        mImageDownloader.getLooper();

        mCacheDownloader = new ImageCacheDownloader(new Handler(), this);
        mCacheDownloader.setPriority(Thread.MIN_PRIORITY);
        mCacheDownloader.start();
        mCacheDownloader.getLooper();

        mPagerAdapter = new PhotoFragmentPagerAdapter(getSupportFragmentManager(),
                mItems,
                mImageDownloader,
                mCacheDownloader);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(mSelectedPos);
    }

     @Override
    public void onDestroy(){
        super.onDestroy();
        mImageDownloader.quit();
        mCacheDownloader.quit();
        Log.i(TAG, "Background thread destroy");
    }

    @Override
    public void onClose() {
        onBackPressed();
    }

    @Override
    public void onHideBars(boolean isInit) {
        if(!isInit) {
            mIsHide = !mIsHide;
        }

        int currentPos = mViewPager.getCurrentItem();
        SwipeFragment currentFragment = (SwipeFragment) mPagerAdapter.instantiateItem(mViewPager, currentPos);
        currentFragment.hideTopBottom(mIsHide);
    }

}
