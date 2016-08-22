package com.highfly.flickrgallery.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 *    Created By: Ann Ngoc Nguyen
 *    Description: This is Adapter for ViewPager in PhotoGalleryActivity
 **/
public class PhotoGalleryAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragments = new ArrayList<>();

    public PhotoGalleryAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(Fragment fragment, String title) {
        mFragments.add(fragment);
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
