package com.highfly.flickrgallery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.highfly.flickrgallery.adapter.BottomSheetViewHolder;
import com.highfly.flickrgallery.entity.Author;
import com.highfly.flickrgallery.entity.PhotoDetails;
import com.highfly.flickrgallery.entity.SharingApp;
import com.highfly.flickrgallery.network.FlickrFetchr;
import com.highfly.flickrgallery.thread.ImageCacheDownloader;
import com.highfly.flickrgallery.thread.ImageDownloader;
import com.highfly.flickrgallery.utils.ImageCache;
import com.highfly.flickrgallery.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 *    Created By: Ann Ngoc Nguyen
 *    Layout: swipe_fragment
 *    Description: This is the fragment for PhotoActivity
 *    The fragment has photo in center, author information on the top, and the title on the bottom
 *    There is a Share button on the top bar to show Sharing Apps within Bottom Sheet Fragment
 *    There are 2 callbacks methods:
 *      1. Close  --> to close the view, and return to PhotoGalleryActivity
 *                --> When close is tapped, send callback to PhotoActivity
 *      2. Tab on fragment --> to hide/view the top and bottom bar which displays Author information and photo title
 *                         --> when user tap on fragment, send callback to PhotoActivity to get the real status of the bar (ishided - true/false)
 **/

public class SwipeFragment extends VisibleFragment{

    public static final String TAG = "SwipeFragment";
    public static final String EXTRA_AUTHOR_ID = "photogallery.author_id";
    public static final String EXTRA_PHOTO_ID = "photogallery.photo_id";
    public static final String EXTRA_URL = "photogallery.item_url";
    public static final int REQUEST_WRITE_STORAGE = 1;

    private String mPhotoUrl;
    private String mPhotoId;
    private ImageDownloader<ImageView> mImageDownloader;
    private ImageCacheDownloader mCacheDownloader;
    private Callbacks mCallbacks;
    private String mAuthorId;
    private ImageView mImageView;
    private PhotoDetails mDetails;
    private Author mAuthor;

    //Photo Details on the Top & Bottom
    private CircleImageView mAvatar;
    private TextView mUsername;
    private TextView mTitle;
    private Toolbar mToolbar;
    private ImageView mClose;
    private ImageView mShare;

    ////////Bottom Sheet//////////////////////
    //Sharing Intent to send photo
    private Intent mIntent;
    private BottomSheetViewHolder mSelectedOption = null;
    private String mFilePath;

    /* The callback methods to send to PhotoActivity */
    public interface Callbacks{
        void onClose();
        void onHideBars(boolean isInit);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
        Log.i(TAG, "Call back method is attached to Activity!");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;

        //Delete the shared image if exists
        if(mFilePath != null) {
            File file = new File(mFilePath);
            file.delete();
        }

        Log.i(TAG, "Call back method is detached to Activity!");
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mPhotoUrl = getArguments().getString(EXTRA_URL);
        mAuthorId = getArguments().getString(EXTRA_AUTHOR_ID);
        mPhotoId = getArguments().getString(EXTRA_PHOTO_ID);

        new FetchAuthorTask().execute(mAuthorId);
        new FetchDetailsTask().execute(mPhotoId);

        Log.i(TAG, "Photo URL: " + mPhotoUrl);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View swipeView = inflater.inflate(R.layout.swipe_fragment, container, false);
        //Photo Details - Top & Bottom
        mToolbar = (Toolbar) swipeView.findViewById(R.id.toolbar);

        mAvatar = (CircleImageView) mToolbar.findViewById(R.id.avatar);
        mUsername = (TextView) mToolbar.findViewById(R.id.username);

        mClose = (ImageView) mToolbar.findViewById(R.id.close_button);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallbacks.onClose();
            }
        });

        //Bottom Sheet Fragment to share the photo
        mShare = (ImageView) mToolbar.findViewById(R.id.share_button);
        mShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();

                BottomSheetViewHolder.OnItemClickListener listener = new BottomSheetViewHolder.OnItemClickListener() {
                    @Override
                    public void onItemClick(BottomSheetViewHolder selectedOption) {
                        mSelectedOption = selectedOption;
                        launchSharingApp(mSelectedOption);
                        bottomSheetFragment.dismiss();

                    }
                };

                bottomSheetFragment.setListener(listener);
                bottomSheetFragment.setFragments(setSharingPagerAdapter(listener));
                bottomSheetFragment.show(getFragmentManager(), "BottomSheetFragment");

            }
        });

        mTitle = (TextView) swipeView.findViewById(R.id.title);
        hideTopBottom(true);
        mCallbacks.onHideBars(true);

        setAuthor();
        setPhotoDetails();

        final ImageView loadingView = (ImageView) swipeView.findViewById(R.id.loading);
        final AnimationDrawable loadingAnim = (AnimationDrawable) loadingView.getDrawable();

        //If user tap somewhere in Photo area --> show/hide the top and bottom bars
        mImageView = (ImageView) swipeView.findViewById(R.id.imageView);
        mImageView.setImageDrawable(null);

        if(mImageDownloader != null){
            Bitmap bitmap = ImageCache.getBitmapFromCache(mPhotoUrl);
            if(bitmap == null){
                loadingView.setVisibility(View.VISIBLE);
                loadingAnim.start();
                mImageDownloader.queueThumbnail(mImageView, mPhotoUrl, loadingAnim, loadingView);
                Log.i(TAG, "Added to Queue: " + mPhotoUrl);
            } else{
                Log.i(TAG, "Bitmap exists on Cache");
                mImageView.setImageBitmap(bitmap);
            }
        }

        swipeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallbacks.onHideBars(false);

            }
        });
        Log.i(TAG, "On Create View");
        return swipeView;
    }

    /*
     * The fragment need 3 information: PhotoId, PhotoURL and AuthorId
     */
    public static SwipeFragment newInstance(String photoId, String authorId, String photoUrl) {
        SwipeFragment swipeFragment = new SwipeFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_PHOTO_ID, photoId);
        bundle.putString(EXTRA_URL, photoUrl);
        bundle.putString(EXTRA_AUTHOR_ID, authorId);
        swipeFragment.setArguments(bundle);
        return swipeFragment;
    }

    public void setImageDownloader(ImageDownloader imageDownloader){
        mImageDownloader = imageDownloader;
    }

    public void setCacheDownloader(ImageCacheDownloader cacheDownloader){
        mCacheDownloader = cacheDownloader;
    }

    private void setAuthor(){
        if(mAuthor != null){
            mImageDownloader.queueThumbnail(mAvatar, mAuthor.getBuddyicon(), null, null);
            Log.i(TAG, "Avatar - Added to Queue: " + mAuthor.getBuddyicon());

            mUsername.setText(mAuthor.getUsername());
        }
    }

    private void setPhotoDetails(){
        if(mDetails != null){
            mTitle.setText(mDetails.getTitle());
        }
    }

    public void hideTopBottom(boolean isHide){
        if(mTitle == null && mToolbar == null){
            Log.e(TAG, "Toolbar and Title hasn't been inited yet!");
            return;
        }
        if(isHide){
            mTitle.setVisibility(View.INVISIBLE);
            mToolbar.setVisibility(View.INVISIBLE);
        } else{
            mTitle.setVisibility(View.VISIBLE);
            mToolbar.setVisibility(View.VISIBLE);
        }
    }

    //////////////////// FOR BOTTOM SHEET - SHARING APPS ////////////////////////
    private void launchSharingApp(BottomSheetViewHolder selectedOption){
        //Check WRITE_EXTERNAL_STORAGE permission firstly
        if(isStoragePermissionGranted()) {
            ByteArrayOutputStream bytes = null;
            try {
                // Get the bitmap from cache and save into device storage
                Bitmap bitmap = ImageCache.getBitmapFromCache(mPhotoUrl);
                if(bitmap == null){
                    Log.e(TAG, "There's no BITMAP in Cache!");
                    return;
                }
                bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

                mFilePath = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(),
                                                                    bitmap, "Title", null);
                //Put the file into the intent --> start intent (gmail or viber or ..)
                Uri uri = Uri.parse(mFilePath);
                mIntent.putExtra(android.content.Intent.EXTRA_STREAM, uri);

                mIntent.setClassName(selectedOption.getPackName(), selectedOption.getActivityName());
                startActivity(mIntent);
            }catch (Exception e){
                Log.e(TAG, e.getMessage());
            } finally {
                try {
                    if (bytes != null) {
                        bytes.flush();
                        bytes.close();
                    }
                }catch (IOException ioe){
                    Log.e(TAG, ioe.getMessage());
                }
            }
        }
    }

    /*
     * Get all possible sharing photo apps from the device and show to Fragment
     */
    private ArrayList<BottomSheetPagerFragment> setSharingPagerAdapter(BottomSheetViewHolder.OnItemClickListener listener){
        mIntent = new Intent(android.content.Intent.ACTION_SEND);
        mIntent.setType("image/*");

        int numPerPage = BottomSheetFragment.NUMBER_PER_PAGE;
        PackageManager pm = getActivity().getPackageManager();

        //Get all possible sharing photo apps
        List<ResolveInfo> apps = pm.queryIntentActivities(mIntent, 0);

        // Perform paging to display apps list into Bottom sheet fragment
        int pagerCount = (int) Math.ceil(apps.size() / numPerPage);
        ArrayList<BottomSheetPagerFragment> fragments = new ArrayList<>();
        for(int i = 0; i < pagerCount; i++) {
            int j = numPerPage*i + numPerPage < apps.size() ? numPerPage*i + numPerPage : apps.size();
            List<ResolveInfo> pageList = apps.subList(numPerPage*i, j);
            ArrayList<SharingApp> mSharingApps = new ArrayList<>();
            for(ResolveInfo info : pageList){
                SharingApp app = new SharingApp(Utils.drawableToBitmap(info.loadIcon(pm)),
                        info.loadLabel(pm).toString(),
                        info.activityInfo.packageName,
                        info.activityInfo.name);
                mSharingApps.add(app);
            }

            //Pass the Sharing App list to Bottom Sheet Pager Fragment
            BottomSheetPagerFragment fragment = BottomSheetPagerFragment.newInstance(mSharingApps);
            fragment.setListener(listener);
            fragments.add(fragment);
        }
        return fragments;
    }

    /*
     * In order to share the photo, we need to save it into device storage first
     * So that we need to check WRITE_EXTERNAL_STORAGE before perform writing
     * If the permission is not granted --> Ask for permission
     * If the permission is granted --> continue ...
     */

    private  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (getActivity().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_STORAGE);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.i(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
                    launchSharingApp(mSelectedOption);
                }
                return;
            }
        }
    }

    /*
     * AsyncTask perform fetch Author Information from Flickr
     * Input: Author id
     * Output: Author
     */
    private class FetchAuthorTask extends AsyncTask<String, Void, Author> {

        @Override
        protected Author doInBackground(String... params){
            Activity activity = getActivity();
            if(activity == null){
                return new Author(params[0]);
            }
            return new FlickrFetchr().FetchAuthor(params[0]);
        }

        @Override
        protected void onPostExecute(Author author)
        {
            mAuthor = author;
            setAuthor();
        }
    }


    /*
     * AsyncTask perform fetch Photo Details Information from Flickr
     * Input: Photo id
     * Output: Photo Details
     */
    private class FetchDetailsTask extends AsyncTask<String, Void, PhotoDetails> {

        @Override
        protected PhotoDetails doInBackground(String... params){
            Activity activity = getActivity();
            if(activity == null){
                return new PhotoDetails();
            }
                return new FlickrFetchr().FetchPhotoDetails(params[0]);
            }

        @Override
        protected void onPostExecute(PhotoDetails details)
        {
            mDetails = details;
            setPhotoDetails();
        }

    }

}