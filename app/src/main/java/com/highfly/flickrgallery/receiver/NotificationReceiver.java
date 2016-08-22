package com.highfly.flickrgallery.receiver;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created By: Ann Ngoc Nguyen
 * Description: Receiver to listen for new Photos event
 */
public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received result: " + getResultCode());
        if(getResultCode() != Activity.RESULT_OK) return;

        int requestCode = intent.getIntExtra("REQUEST_CODE", 0);
        Notification notification = (Notification) intent.getParcelableExtra("NOTIFICATION");

        NotificationManager notificationManager = (NotificationManager) context
                                                    .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(requestCode, notification);
    }
}
