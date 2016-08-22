package com.highfly.flickrgallery.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.highfly.flickrgallery.BottomSheetPagerFragment;

import java.util.List;

/**
 *    Created By: Ann Ngoc Nguyen
 *    Description: This is Adapter for ViewPager in BottomSheetDialogFragment
 **/
public class BottomSheetPagerAdapter extends FragmentPagerAdapter {

    private final static String TAG = "BottomSheetPagerAdapter";
    private List<BottomSheetPagerFragment> mFragments;

    public BottomSheetPagerAdapter(FragmentManager fm,
                                   List<BottomSheetPagerFragment> fragments){
        super(fm);
        mFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}
