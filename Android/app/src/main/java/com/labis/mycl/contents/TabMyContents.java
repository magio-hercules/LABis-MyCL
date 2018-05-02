package com.labis.mycl.contents;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.labis.mycl.R;

import java.util.ArrayList;

/**
 * Created by Junyoung on 2016-06-23.
 */

public class TabMyContents extends Fragment {

    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    RecyclerViewAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_fragment_mycontents, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.myContentsView);
        mLayoutManager = new LinearLayoutManager(this.getContext());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        // ArrayList 에 Item 객체(데이터) 넣기
        ArrayList<Item> items = new ArrayList();
        items.add(new Item("1", "하나"));
        items.add(new Item("2", "둘"));
        items.add(new Item("3", "셋"));
        items.add(new Item("4", "넷"));
        items.add(new Item("5", "다섯"));
        items.add(new Item("1", "하나"));
        items.add(new Item("2", "둘"));
        items.add(new Item("3", "셋"));
        items.add(new Item("4", "넷"));
        items.add(new Item("5", "다섯"));
        items.add(new Item("1", "하나"));
        items.add(new Item("2", "둘"));
        items.add(new Item("3", "셋"));
        items.add(new Item("4", "넷"));
        items.add(new Item("5", "다섯"));
        // LinearLayout으로 설정
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Animation Defualt 설정
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Decoration 설정
        mRecyclerView.addItemDecoration(new RecyclerViewDecoration(this.getContext(), RecyclerViewDecoration.VERTICAL_LIST));

        // Adapter 생성
        mAdapter = new RecyclerViewAdapter(items);
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }
}
