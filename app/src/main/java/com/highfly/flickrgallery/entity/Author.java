package com.highfly.flickrgallery.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 *    Created By: Ann Ngoc Nguyen
 *    Description: Author
 **/
public class Author implements Parcelable{
    private final static String DEFAULT_AVATAR = "https://www.flickr.com/images/buddyicon.gif";
    private String mId;
    private String mUsername;
    private int mIconFarm = 0;
    private int mIconServer = 0;

    public Author(String id){
        mId = id;
    }

    protected Author(Parcel in) {
        mId = in.readString();
        mUsername = in.readString();
        mIconFarm = in.readInt();
        mIconServer = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mUsername);
        dest.writeInt(mIconFarm);
        dest.writeInt(mIconServer);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Author> CREATOR = new Creator<Author>() {
        @Override
        public Author createFromParcel(Parcel in) {
            return new Author(in);
        }

        @Override
        public Author[] newArray(int size) {
            return new Author[size];
        }
    };

    public String getId() {
        return mId;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public int getIconServer() {
        return mIconServer;
    }

    public void setIconServer(int iconServer) {
        this.mIconServer = iconServer;
    }

    public int getIconFarm() {
        return mIconFarm;
    }

    public void setIconFarm(int iconFarm) {
        mIconFarm = iconFarm;
    }

    public String getBuddyicon() {
        if(mIconServer > 0)
            return String.format("http://farm%s.staticflickr.com/%s/buddyicons/%s.jpg", mIconFarm, mIconServer, mId );
        else return DEFAULT_AVATAR;
    }

}
