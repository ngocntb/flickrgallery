package com.highfly.flickrgallery.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

import com.highfly.flickrgallery.thread.PollService;

/**
 * Created By: Ann Ngoc Nguyen
 * Description: Receiver to listen for BOOT_COMPLETED event
 */
public class StartupReceiver extends BroadcastReceiver {
    private static final String TAG = "StartupReceiver";
    @Override
    public void onReceive(Context context, Intent intent){
        Log.i(TAG, "Received broadcast intent:" + intent.getAction());

        boolean isOn = PreferenceManager.getDefaultSharedPreferences(context)
                                        .getBoolean(PollService.PREF_IS_ALARM_ON, false);
        PollService.setServiceAlarm(context, isOn);
    }
}
