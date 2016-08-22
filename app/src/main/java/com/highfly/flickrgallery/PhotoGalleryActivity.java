package com.highfly.flickrgallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.highfly.flickrgallery.adapter.PhotoGalleryAdapter;
import com.highfly.flickrgallery.thread.PollService;


/**
*    Created By: Ann Ngoc Nguyen
*    Layout: fragment_activity
*    Description: This is the Main Activity.
*    The activity has a View Pager
*    View Pager adapter has Photo Gallery Fragment (PhotoGalleryFragment)
**/

public class PhotoGalleryActivity extends AppCompatActivity{
    public static final String TAG = "PhotoGalleryActivity";
    private Snackbar mSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        PhotoGalleryAdapter adapter = new PhotoGalleryAdapter(getSupportFragmentManager());

        Fragment fragment = new PhotoGalleryFragment();
        adapter.addFragment(fragment, getString(R.string.app_name));

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        registerReceiver(mConnReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    }

    /*
    Show snack bar at the bottom of activity
     */
    private void showSnackBar(View view, String message) {
        mSnackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE);
        mSnackbar.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.translucent_red));

        TextView tv = (TextView) mSnackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        mSnackbar.show();
    }

    private void hideSnackBar(){
        if(mSnackbar != null)
            mSnackbar.dismiss();
    }

    @Override
    public void onResume(){
        super.onResume();
        IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        registerReceiver(mConnReceiver, filter, PollService.PERM_PRIVATE, null);
        Log.i(TAG, "On Resume");
    }

    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(mConnReceiver);
        Log.i(TAG, "On Pause");
    }

    /* When the internet connection has problem, snack bar will be shown
    *  mConnReceiver is registered at onCreate, onResume, and will be un registered at onPause
    */
    private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo == null || !networkInfo.isConnected()) {
                showSnackBar(findViewById(R.id.main_content), "No Internet Connection!");
            } else{
                hideSnackBar();
            }
        }
    };
}
