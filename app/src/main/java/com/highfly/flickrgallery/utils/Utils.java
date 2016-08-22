package com.highfly.flickrgallery.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created By: Ann Ngoc Nguyen
 * Description: supported functions
 */
public class Utils {

    private final static int SMALL_WIDTH = 2; //2 inches
    private final static int MEDIUM_WIDTH = 4; //4 inches
    private final static int LARGE_WIDTH = 7; //8 inches
    private final static int EXTRA_LARGE_WIDTH = 10; //8 inches

    /*
     * Get the screen width
     */
    public static double getScreenWidth(Activity activity){
        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);

        return dm.widthPixels/dm.xdpi;
    }

    /*
     * Return the number of Columns for PhotoGalleryFragment
     */
    public static int getSpanCount(Activity activity){
        double screenWidth = getScreenWidth(activity);
        if(screenWidth <= SMALL_WIDTH) return 1; // 1 column for Width <= 2 inches
        else if(screenWidth > SMALL_WIDTH && screenWidth <= MEDIUM_WIDTH) return 2; //2 colums for Width <= 4 inches and > 2 inches
        else if(screenWidth > MEDIUM_WIDTH && screenWidth <= LARGE_WIDTH ) return 3; // 3 columns for 7 >= Width > 4 inches (tablet)
        else if(screenWidth > LARGE_WIDTH && screenWidth <= EXTRA_LARGE_WIDTH) return 4; // 4 columns for 10 >= Width > 7 inches (tablet landscape)
        else return 5; //laptop screen
    }


    /*
     * Drawable to Bitmap
     */
    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /*
     * Bitmap to Drawable
     */
    public static Drawable bitmapToDrawable(Bitmap bitmap, Resources resources){
        return new BitmapDrawable(resources, bitmap);
    }
}
