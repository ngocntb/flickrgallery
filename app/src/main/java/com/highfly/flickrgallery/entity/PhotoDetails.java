package com.highfly.flickrgallery.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 *    Created By: Ann Ngoc Nguyen
 *    Description: Photo Details
 **/
public class PhotoDetails implements Parcelable{
    private String mTitle;
    private String mDescription;

    public PhotoDetails(){}

    public PhotoDetails(String title){
        mTitle = title;
    }

    protected PhotoDetails(Parcel in) {
        mTitle = in.readString();
        mDescription = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeString(mDescription);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PhotoDetails> CREATOR = new Creator<PhotoDetails>() {
        @Override
        public PhotoDetails createFromParcel(Parcel in) {
            return new PhotoDetails(in);
        }

        @Override
        public PhotoDetails[] newArray(int size) {
            return new PhotoDetails[size];
        }
    };

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description){
        mDescription = description;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title){
        mTitle = title;
    }

}
