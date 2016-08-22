package com.highfly.flickrgallery.adapter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.highfly.flickrgallery.R;
import com.highfly.flickrgallery.entity.SharingApp;
import com.highfly.flickrgallery.utils.Utils;

import java.util.List;

/**
 *    Created By: Ann Ngoc Nguyen
 *    Layout: bottomsheet_pager_item
 *    Description: This is Adapter for RecyclerView in BottomSheetPagerFragment
 **/
public class BottomSheetAdapter extends RecyclerView.Adapter<BottomSheetViewHolder> {

    private List<SharingApp> mApps;
    private PackageManager mPackManager;
    private Resources mResources;
   private BottomSheetViewHolder.OnItemClickListener mListener;

    public BottomSheetAdapter(Context context, List<SharingApp> apps, BottomSheetViewHolder.OnItemClickListener listener) {
        mApps = apps;
        mPackManager = context.getPackageManager();
        mResources = context.getResources();
        mListener = listener;
    }

    @Override
    public BottomSheetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.bottomsheet_item, parent, false);
        return new BottomSheetViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(BottomSheetViewHolder holder, int position) {
        SharingApp info = mApps.get(position);
        holder.getImageView().setImageDrawable(Utils.bitmapToDrawable(info.getAppIcon(), mResources));
        holder.getTextView().setText(info.getAppLabel());
        holder.setPackName(info.getPackageName());
        holder.setActivityName(info.getClassName());

    }

    @Override
    public int getItemCount() {
        return mApps.size();
    }

}
