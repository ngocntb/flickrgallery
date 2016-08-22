package com.highfly.flickrgallery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.highfly.flickrgallery.adapter.BottomSheetAdapter;
import com.highfly.flickrgallery.adapter.BottomSheetViewHolder;
import com.highfly.flickrgallery.entity.SharingApp;

import java.util.ArrayList;
import java.util.List;

/**
 *    Created By: Ann Ngoc Nguyen
 *    Layout: bottomsheet_pager_item
 *    Description: This is the fragment for PhotoActivity
 *    Input: Each pager will have maximum 6 apps as input
 *    Fragment has a RecyclerView, and RecylerView adapter is BottomSheetAdapter
 **/
public class BottomSheetPagerFragment extends Fragment {
    public final static String APP_LIST = "com.highfly.flickgallery.app_list";
    private final static int VERTICAL = 1;
    private final static int NUM_COL = 3;

    private List<SharingApp> mApps;
    private BottomSheetViewHolder.OnItemClickListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mApps = getArguments().getParcelableArrayList(APP_LIST);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.bottomsheet_pager_item, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        BottomSheetAdapter adapter = new BottomSheetAdapter(container.getContext(), mApps, mListener);
        recyclerView.setLayoutManager( new StaggeredGridLayoutManager(NUM_COL, VERTICAL));
        recyclerView.setAdapter(adapter);

        return view;
    }

    public void setListener(BottomSheetViewHolder.OnItemClickListener listener){
        mListener = listener;
    }

    public static BottomSheetPagerFragment newInstance(ArrayList<SharingApp> apps){
        BottomSheetPagerFragment fragment = new BottomSheetPagerFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(APP_LIST, apps);

        fragment.setArguments(bundle);
        return fragment;
    }
}
