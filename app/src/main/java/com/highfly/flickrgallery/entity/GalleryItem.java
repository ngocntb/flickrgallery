package com.highfly.flickrgallery.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 *    Created By: Ann Ngoc Nguyen
 *    Description: Photo Information
 **/
public class GalleryItem implements Parcelable{

    private String mId;
    private String mThumbnailUrl;
    private Author mAuthor;
    private PhotoDetails mDetails;

    public GalleryItem(String id, String caption, String url, String userId){
        mId = id;
        mThumbnailUrl = url;
        mDetails = new PhotoDetails(caption);
        mAuthor = new Author(userId);
    }

    protected GalleryItem(Parcel in) {
        mId = in.readString();
        mThumbnailUrl = in.readString();
        mAuthor = in.readParcelable(Author.class.getClassLoader());
        mDetails = in.readParcelable(PhotoDetails.class.getClassLoader());
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mThumbnailUrl);
        dest.writeParcelable(mAuthor, flags);
        dest.writeParcelable(mDetails, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GalleryItem> CREATOR = new Creator<GalleryItem>() {
        @Override
        public GalleryItem createFromParcel(Parcel in) {
            return new GalleryItem(in);
        }

        @Override
        public GalleryItem[] newArray(int size) {
            return new GalleryItem[size];
        }
    };

    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }

    public String getThumbnailUrl() {
        return mThumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.mThumbnailUrl = thumbnailUrl;
    }

    public Author getAuthor() {
        return mAuthor;
    }

    public void setAuthor(Author author) {
        this.mAuthor = author;
    }

    public PhotoDetails getDetails(){
        return mDetails;
    }

    public void setDetails(PhotoDetails details){
        mDetails = details;
    }

    public String getPhotoUrl(){
        if(mThumbnailUrl == null || mThumbnailUrl.isEmpty()) return "";
        return mThumbnailUrl.substring(0,((mThumbnailUrl.length()-1)-5))+"_b.jpg";
    }
}
