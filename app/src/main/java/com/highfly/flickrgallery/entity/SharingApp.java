package com.highfly.flickrgallery.entity;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created By: Ann Ngoc Nguyen
 * Description: SharingApp is hybrid version of ResolveInfo to pass between fragments
 */
public class SharingApp implements Parcelable{
    private Bitmap mAppIcon;
    private String mAppLabel;
    private String mPackageName;
    private String mClassName;


    protected SharingApp(Parcel in) {
        mAppIcon = in.readParcelable(Bitmap.class.getClassLoader());
        mAppLabel = in.readString();
        mPackageName = in.readString();
        mClassName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mAppIcon, flags);
        dest.writeString(mAppLabel);
        dest.writeString(mPackageName);
        dest.writeString(mClassName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SharingApp> CREATOR = new Creator<SharingApp>() {
        @Override
        public SharingApp createFromParcel(Parcel in) {
            return new SharingApp(in);
        }

        @Override
        public SharingApp[] newArray(int size) {
            return new SharingApp[size];
        }
    };

    public Bitmap getAppIcon() {
        return mAppIcon;
    }

    public String getAppLabel() {
        return mAppLabel;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getClassName() {
        return mClassName;
    }

    public SharingApp(Bitmap appIcon, String appLabel, String packageName, String className){
        mAppIcon = appIcon;
        mAppLabel = appLabel;
        mPackageName = packageName;
        mClassName = className;
    }
}
