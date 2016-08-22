package com.highfly.flickrgallery.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.highfly.flickrgallery.R;

/**
 *    Created By: Ann Ngoc Nguyen
 *    Description: This is ViewHolder for BottomSheetPagerAdapter
 **/
public class BottomSheetViewHolder extends RecyclerView.ViewHolder
                                    implements View.OnClickListener{
    private final static String TAG = "BottomSheetViewHolder";
    private TextView mTextView;
    private ImageView mImageView;
    private OnItemClickListener mListener;

    private String mPackName;
    private String mActivityName;

    public interface OnItemClickListener {
        void onItemClick(BottomSheetViewHolder item);
    }

    public BottomSheetViewHolder(View view, OnItemClickListener listener) {
        super(view);
        mListener = listener;
        view.setOnClickListener(this);
        mImageView = (ImageView) view.findViewById(R.id.imageView);
        mTextView = (TextView) view.findViewById(R.id.textView);
    }

    public ImageView getImageView(){
        return mImageView;
    }

    public TextView getTextView(){
        return mTextView;
    }


    public String getPackName() {
        return mPackName;
    }

    public void setPackName(String mPackName) {
        this.mPackName = mPackName;
    }

    public String getActivityName() {
        return mActivityName;
    }

    public void setActivityName(String packName) {
        this.mActivityName = packName;
    }

    @Override
    public void onClick(View v) {
        Log.i(TAG, "Item is clicked!");
        mListener.onItemClick(this);
    }
}
