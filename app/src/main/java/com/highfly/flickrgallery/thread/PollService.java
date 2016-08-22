package com.highfly.flickrgallery.thread;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.highfly.flickrgallery.R;
import com.highfly.flickrgallery.entity.GalleryItem;
import com.highfly.flickrgallery.network.FlickrFetchr;

import java.util.ArrayList;

/**
 * Created By: Ann Ngoc Nguyen
 * Description: Showing background Notification when there are new Flick photos
 */
public class PollService extends IntentService {
    private static final String TAG = "PollService";
    private static final int POLL_INTERVAL = 1000 * 15;
    public static final String PREF_IS_ALARM_ON = "isAlarmOn";
    public static final String ACTION_SHOW_NOTIFICATION = "com.highfly.flickrgallery.SHOW_NOTIFICATION";
    public static final String PERM_PRIVATE = "com.highfly.flickrgallery.PRIVATE";

    private static final int DEFAULT_PAGE = 1;

    public PollService(){
        super(TAG);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onHandleIntent(Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if(cm.getBackgroundDataSetting() == false || cm.getActiveNetworkInfo() == null)
            return;

        Log.i(TAG, "Received an intent: " + intent);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String query = sharedPreferences.getString(FlickrFetchr.PREF_SEARCH_QUERY, null);
        String lastResultId = sharedPreferences.getString(FlickrFetchr.PREF_LAST_RESULT_ID, null);

        ArrayList<GalleryItem> items;

        if (query != null) {
            items = new FlickrFetchr().SearchItems(query, DEFAULT_PAGE);
        } else {
            items = new FlickrFetchr().FetchItems(DEFAULT_PAGE);
        }

        if(items.size() == 0) return;

        String resultId = items.get(0).getId();
        if(!resultId.equals(lastResultId)){
            Log.i(TAG, "Got a new result Id: " +  resultId);
            Resources r = getResources();
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, PollService.class), 0);
            Notification notification = new NotificationCompat.Builder(this)
                                        .setTicker(r.getString(R.string.new_picture_title))
                                        .setSmallIcon(android.R.drawable.ic_menu_report_image)
                                        .setContentText(r.getString(R.string.new_picture_text))
                                        .setContentTitle(r.getString(R.string.new_picture_title))
                                        .setContentIntent(pendingIntent)
                                        .setAutoCancel(true)
                                        .build();
        /*    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0, notification);
            sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION), PERM_PRIVATE);*/
            showBackgroundNotification(0, notification);
        }
        else{
            Log.i(TAG, "Got a old result Id: " + resultId);
        }

        sharedPreferences.edit().putString(FlickrFetchr.PREF_LAST_RESULT_ID, resultId).commit();
    }

    public static void setServiceAlarm(Context context, boolean isOn){
        Intent i = new Intent(context, PollService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if(isOn) {
            alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), POLL_INTERVAL, pendingIntent);
        }
        else{
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                        .putBoolean(PollService.PREF_IS_ALARM_ON, isOn)
                        .commit();
    }

    public static boolean isServiceAlarmOn(Context context){
        Intent i = new Intent(context, PollService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pendingIntent != null;
    }

    public void showBackgroundNotification(int requestCode, Notification notification){
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra("REQUEST_CODE", requestCode);
        i.putExtra("NOTIFICATION", notification);
        sendOrderedBroadcast(i, PERM_PRIVATE, null, null, Activity.RESULT_OK, null, null);
    }
}
