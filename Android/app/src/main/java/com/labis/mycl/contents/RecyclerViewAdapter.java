package com.labis.mycl.contents;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.labis.mycl.R;
import com.labis.mycl.model.Content;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {

    private ArrayList<Content> mItems;
    HashMap<String, String> mGenre;
    private String ModeStatus = "MY";
    Context mContext;

    public RecyclerViewAdapter(ArrayList itemList, HashMap<String, String> genreMap, String Mode) {
        mItems = itemList;
        mGenre = genreMap;
        ModeStatus = Mode;
    }

    // 필수 오버라이드 : View 생성
    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        mContext = parent.getContext();

        RecyclerViewHolder holder = new RecyclerViewHolder(v);
        return holder;
    }

    // 필수 오버라이드 : 재활용되는 View 가 호출, Adapter 가 해당 position 에 해당하는 데이터를 결합
    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, final int position) {

        // 해당 position에 해당하는 데이터 결합
        holder.mGen.setText(mGenre.get(mItems.get(position).gen_id));
        Picasso.get().load(mItems.get(position).image).into(holder.mImgView);
        holder.mName.setText(mItems.get(position).name);
        holder.mNameOrg.setText(mItems.get(position).name_org);
        if(Integer.parseInt(mItems.get(position).season) > 0) {
            holder.mSeason.setText("시즌 " + mItems.get(position).season);
        }
        if(ModeStatus == "MY") {
            if(Integer.parseInt(mItems.get(position).chapter) > 0) {
                holder.mIndex.setText(mItems.get(position).chapter + " 화");
            }
            holder.mConAddBtn.setVisibility(View.GONE);
        } else if(ModeStatus == "TOTAL") {
            if(Integer.parseInt(mItems.get(position).chapter_end) > 0) {
                holder.mIndex.setText(mItems.get(position).chapter_end + " 화");
            }
            holder.mConAddBtn.setVisibility(View.VISIBLE);
        }

        // 생성된 List 중 선택된 목록번호를 Toast로 출력
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, String.format("%d 선택", position + 1), Toast.LENGTH_SHORT).show();
            }
        });

        holder.mConAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "EVOL", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 필수 오버라이드 : 데이터 갯수 반환
    @Override
    public int getItemCount() {
        return mItems.size();
    }

}
