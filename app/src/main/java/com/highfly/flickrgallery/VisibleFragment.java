package com.highfly.flickrgallery;

import android.support.v4.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import com.highfly.flickrgallery.thread.PollService;


/**
 *    Created By: Ann Ngoc Nguyen
 *    Layout: bottomsheet_pager_item
 *    Description: All Fragments that extends from VisiableFragment can be received Broadcast message
 **/

public abstract class VisibleFragment extends Fragment{
    public static final String TAG = "VisibleFragment";

    public BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getActivity(), "Got a Broadcast: " + intent.getAction(), Toast.LENGTH_LONG)
                        .show();
        }
    };
    @Override
    public void onResume(){
        super.onResume();
        IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mOnShowNotification, filter, PollService.PERM_PRIVATE, null);
        Log.i(TAG, "On Resume");
    }

    @Override
    public void onPause(){
        super.onPause();
        getActivity().unregisterReceiver(mOnShowNotification);
        Log.i(TAG, "On Pause");
    }
}
