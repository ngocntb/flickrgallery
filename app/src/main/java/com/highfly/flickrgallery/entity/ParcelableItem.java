package com.highfly.flickrgallery.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created By: Ann Ngoc Nguyen
 * Description: ParcelableItem is hybrid version of GalleryItem to pass between Activities
 */
public class ParcelableItem implements Parcelable {
    private String mId;
    private String mPhotoUrl;
    private String mAuthorId;

    public ParcelableItem(String id, String photoUrl, String authorId){
        mId = id;
        mPhotoUrl = photoUrl;
        mAuthorId = authorId;
    }

    public ParcelableItem(GalleryItem item){
        mId = item.getId();
        mPhotoUrl = item.getPhotoUrl();
        mAuthorId = item.getAuthor().getId();
    }

    protected ParcelableItem(Parcel in) {
        mId = in.readString();
        mPhotoUrl = in.readString();
        mAuthorId = in.readString();
    }

    public static final Creator<ParcelableItem> CREATOR = new Creator<ParcelableItem>() {
        @Override
        public ParcelableItem createFromParcel(Parcel in) {
            return new ParcelableItem(in);
        }

        @Override
        public ParcelableItem[] newArray(int size) {
            return new ParcelableItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mPhotoUrl);
        dest.writeString(mAuthorId);
    }

    public String getAuthorId() {
        return mAuthorId;
    }

    public void setAuthorId(String mAuthorId) {
        this.mAuthorId = mAuthorId;
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public void setPhotoUrl(String mPhotoUrl) {
        this.mPhotoUrl = mPhotoUrl;
    }

    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }
}
