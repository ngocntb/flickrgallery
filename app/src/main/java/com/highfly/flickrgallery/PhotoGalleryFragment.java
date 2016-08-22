package com.highfly.flickrgallery;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import java.util.ArrayList;

import android.support.v7.widget.RecyclerView;

import com.highfly.flickrgallery.adapter.PhotoGalleryViewAdapter;
import com.highfly.flickrgallery.entity.GalleryItem;
import com.highfly.flickrgallery.network.FlickrFetchr;
import com.highfly.flickrgallery.thread.ImageCacheDownloader;
import com.highfly.flickrgallery.thread.ImageDownloader;
import com.highfly.flickrgallery.thread.PollService;
import com.highfly.flickrgallery.utils.RecyclerViewScrollListener;
import com.highfly.flickrgallery.utils.Utils;

/**
 *    Created By: Ann Ngoc Nguyen
 *    Layout: photo_gallery_fragment
 *    Description: this fragment belongs to PhotoGalleryFragment
 *    This fragment has a recycler View
 *    RecyclerView uses Grid layout. The number of grid cols is calculated based on the screen width
 *    ImageDownloader is a thread to download thumbnail images, CacheDownload is another thread to download thumbnail images before it is needed
 *    FlickrFetch performs fetching data from Flickr api
 **/
public class PhotoGalleryFragment extends VisibleFragment{

    private final static String TAG = "PhotoGalleryFragment";

    private final static String INITIAL_QUERY = "";
    //For paging
    private final static Integer ITEMS_PER_PAGE = 100;

    private RecyclerView mRecyclerView;
    private StaggeredGridLayoutManager mStaggeredGridLayoutManager;
    private ImageDownloader<ImageView> mImageDownloader;
    private ImageCacheDownloader mCacheDownloader;
    private SwipeRefreshLayout mSwipeRefreshWidget;
    private RecyclerViewScrollListener mScrollListener;
    private PhotoGalleryViewAdapter mAdapter;
    private String mQuery = INITIAL_QUERY;
    private boolean mIsReset = false;
    private int mCurrentPage = 1;
    private int mSpanCount = 0;
    private int mMaxPage = 1;
    private FlickrFetchr mFlickrFetchr;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mFlickrFetchr = new FlickrFetchr();
        updateItems();

        mImageDownloader = new ImageDownloader<>(new Handler(), getActivity());

        //Whenever the image is downloaded, it is eet into imageView
        mImageDownloader.setListen(new ImageDownloader.Listener<ImageView>() {
            @Override
            public void onThumbnailDownloaded(ImageView imageView,
                                              Bitmap thumbnail,
                                              AnimationDrawable loadingAnim,
                                              ImageView loadingView) {
                if (isVisible()) {
                    imageView.setImageBitmap(thumbnail);
                }
            }
        });
        mImageDownloader.start();
        mImageDownloader.getLooper();

        mCacheDownloader = new ImageCacheDownloader(new Handler(), getContext());
        mCacheDownloader.setPriority(Thread.MIN_PRIORITY);
        mCacheDownloader.start();
        mCacheDownloader.getLooper();

        Log.i(TAG, "Background Thread started");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.photo_gallery_fragment, parent, false);

        mSpanCount = Utils.getSpanCount(getActivity());
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerview);
        mStaggeredGridLayoutManager = new StaggeredGridLayoutManager(mSpanCount, 1);
        mRecyclerView.setLayoutManager(mStaggeredGridLayoutManager);

        //Perform refresh the view
        mSwipeRefreshWidget = (SwipeRefreshLayout)v.findViewById(R.id.swipe_refresh_main);
        mSwipeRefreshWidget.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                mCurrentPage = 1;
                new FetchItemsTask().execute(mCurrentPage);

            }
        });

        //if go to the end of the view --> Load more
        mScrollListener = new RecyclerViewScrollListener(mStaggeredGridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if(mCurrentPage == mMaxPage) {
                    Log.i(TAG, "No more pictures");
                    return;
                }
                mCurrentPage = page;
                Log.i(TAG, "Current Page: " + mCurrentPage);
                new FetchItemsTask().execute(mCurrentPage);
            }
        };

        mRecyclerView.addOnScrollListener(mScrollListener);

        return v;
    }

    /*
     * Handle the search box on the menu bar
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);

        MenuItem menuItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) menuItem.getActionView();
        //Search Submit
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mIsReset = true;
                mCurrentPage = 1;
                searchView.clearFocus();
                mQuery = query;
                updateItems();
                Log.i(TAG, "New Query is submitted: " + query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        //Search Cancel
        MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                mIsReset = true;
                mCurrentPage = 1;
                mQuery = INITIAL_QUERY;
                updateItems();
                return true;
            }
        });

    }

    /*
     * Handle Polling Request - Turn on/off
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
                setMenuOptions(item);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu){
        super.onPrepareOptionsMenu(menu);
        MenuItem toogleItem = menu.findItem(R.id.menu_item_toggle_polling);
        setMenuOptions(toogleItem);
    }

    private void setMenuOptions(MenuItem item){
        if(PollService.isServiceAlarmOn(getActivity())){
            item.setTitle(R.string.stop_polling);
        }
        else{
            item.setTitle(R.string.start_polling);
        }
    }

    public void updateItems(){
        new FetchItemsTask().execute(mCurrentPage);
    }

    private void setupAdapter(ArrayList<GalleryItem> items){
        if(getActivity() == null || mRecyclerView == null) return;
        if(items != null){
            mAdapter = new PhotoGalleryViewAdapter(items, this, mImageDownloader);
            mRecyclerView.setAdapter(mAdapter);
        } else{
            mRecyclerView.setAdapter(null);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mImageDownloader.quit();
        mCacheDownloader.quit();
        Log.i(TAG, "Background thread destroy");
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        mImageDownloader.clearQueue();
        mCacheDownloader.clearCacheQueue();
        Log.i(TAG, "OnDestroyView");
    }

    /*
     * AsyncTask perform fetch Flickr Recent Photos from Flichr api
     * If mQuery is empty --> Perform fetch recent Photos
     * Else --> Perform search Photos
     */

    private class FetchItemsTask extends AsyncTask<Integer, Void, ArrayList<GalleryItem>>{
        private String totalResults = null;
        @Override
        protected ArrayList<GalleryItem> doInBackground(Integer... params){
            Activity activity = getActivity();
            if(activity == null){
                return new ArrayList<>();
            }
            ArrayList<GalleryItem> items = new ArrayList<>();
            if (mQuery != INITIAL_QUERY) {
                items = mFlickrFetchr.SearchItems(mQuery, params[0]);
            } else {
                items = mFlickrFetchr.FetchItems(params[0]);
            }

            totalResults = mFlickrFetchr.getTotalResult();
            mMaxPage = (int) Math.ceil(Integer.parseInt(totalResults)/ITEMS_PER_PAGE);
            Log.i(TAG, "Total Result: " + totalResults + ", Max Pages: " + mMaxPage);
            return items;
        }

        @Override
        protected void onPostExecute(ArrayList<GalleryItem> items)
        {
            if(mQuery != INITIAL_QUERY && mCurrentPage == 1){
                Toast.makeText(getActivity(), totalResults,Toast.LENGTH_LONG).show();
            }
            if(mAdapter == null){
                setupAdapter(items);
                Log.i(TAG, "Dataset is setting up...");
            }
            //If user performs refresh the list
            else if(mSwipeRefreshWidget.isRefreshing()){
                mAdapter.onReset(items);
                mScrollListener.onReset();
                mSwipeRefreshWidget.setRefreshing(false);
                Log.i(TAG, "Refreshing dataset...");
            }
            //If user switchs from search to recent or from recent to search
            else if(mIsReset == true){
                mAdapter.onReset(items);
                mScrollListener.onReset();
                mRecyclerView.smoothScrollToPosition(0);
                mIsReset = false;
                Log.i(TAG, "Reset dataset...");
            }
            //If user perform scrolling down
            else{
                mAdapter.updateDataSet(items);
                Log.i(TAG, "New Items are added to dataset");
            }

        }
    }

}
