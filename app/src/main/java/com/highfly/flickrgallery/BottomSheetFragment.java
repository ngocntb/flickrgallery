package com.highfly.flickrgallery;

import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.highfly.flickrgallery.adapter.BottomSheetPagerAdapter;
import com.highfly.flickrgallery.adapter.BottomSheetViewHolder;

import java.util.ArrayList;

/**
 *    Created By: Ann Ngoc Nguyen
 *    Layout: bottomsheet_share
 *    Description: This is Bottom Sheet Dialog Fragment
 *    Input: List of BottomSheetPagerFragment
 *    This Dialog as View Pager to shows all possible sending apps
 *    Adapter for the ViewPager is a list of BottomSheetPagerFragment
 **/

public class BottomSheetFragment extends BottomSheetDialogFragment {
    public static final String TAG = "BottomSheetFragment";
    public static final int NUMBER_PER_PAGE = 6;

    private View mView;
    private ViewPager mBottomSheetViewPager;
    //Pager - Paging
    private LinearLayout mPagerIndicator;
    //Sharing Intent to send photo
    private ArrayList<BottomSheetPagerFragment> mFragments;
    private BottomSheetViewHolder.OnItemClickListener mListener;

   @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.bottomsheet_share, container, false);

        mBottomSheetViewPager = (ViewPager) mView.findViewById(R.id.bottomsheet_pager);
        mBottomSheetViewPager.setAdapter(new BottomSheetPagerAdapter(getChildFragmentManager(), mFragments));

        mPagerIndicator = (LinearLayout) mView.findViewById(R.id.viewPagerCountDots);
        setUiPageViewController();

        return mView;
    }

    public void setListener( BottomSheetViewHolder.OnItemClickListener listener){
        mListener = listener;
    }

    public void setFragments(ArrayList<BottomSheetPagerFragment> fragments){
        mFragments = fragments;
    }

    /* Display the dots at the bottom */
    private void setUiPageViewController() {
        int pagerCount = mFragments.size();
        for (int i = 0; i < pagerCount; i++) {
            ImageView dot = new ImageView(mView.getContext());
            if(i == 0) {
                dot.setImageResource(R.drawable.selecteditem_dot);
            }else {
                dot.setImageResource(R.drawable.nonselecteditem_dot);
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(4, 0, 4, 0);

            mPagerIndicator.addView(dot, params);
        }


    }
}
