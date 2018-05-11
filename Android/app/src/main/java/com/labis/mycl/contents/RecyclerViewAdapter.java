package com.labis.mycl.contents;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.labis.mycl.R;
import com.labis.mycl.model.Content;
import com.labis.mycl.rest.RetroCallback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {

    public ArrayList<Content> mItems;
    Context mContext;
    private ContentsActivity mActivity;
    RecyclerViewHolder mHolder;
    int mPosition;

    public RecyclerViewAdapter(ContentsActivity activity, ArrayList itemList) {
        mActivity = activity;
        mItems = itemList;
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
        mPosition = position;
        holder.mGen.setText(mActivity.genreMap.get(mItems.get(position).gen_id));
        Picasso.get().load(mItems.get(position).image).into(holder.mImgView);
        holder.mName.setText(mItems.get(position).name);
        holder.mNameOrg.setText(mItems.get(position).name_org);
        if(Integer.parseInt(mItems.get(position).season) > 0) {
            holder.mSeason.setText("시즌" + mItems.get(position).season);
        }
        if(mActivity.modeStatus == "MY") {
            holder.mConMinusBtn.setVisibility(View.VISIBLE);
            holder.mConAddBtn.setVisibility(View.VISIBLE);
            holder.mIndex.setGravity(Gravity.CENTER);
            if(mItems.get(position).chapter > 0) {
                holder.mIndex.setText(String.valueOf(mItems.get(position).chapter));
            }
        } else if(mActivity.modeStatus == "TOTAL") {
            holder.mConMinusBtn.setVisibility(View.GONE);
            holder.mConAddBtn.setVisibility(View.GONE);
            holder.mIndex.setGravity(Gravity.RIGHT);
            if(mItems.get(position).chapter_end > 0) {
                holder.mIndex.setText(String.valueOf(mItems.get(position).chapter_end));
            }
            //holder.mConAddBtn.setVisibility(View.VISIBLE);
        }

        // 생성된 List 중 선택된 목록번호를 Toast로 출력
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(mContext, String.format("%d 선택", position + 1), Toast.LENGTH_SHORT).show();
            }
        });

        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("EVOL", String.format("%d 선택", position + 1));
                mActivity.touchContentItem = mItems.get(position);
                return false;
            }
        });

        holder.mConMinusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItems.get(position).setChapter(mItems.get(position).chapter - 1);
                updateChapter(position, mItems.get(position).getChapter());
            }
        });

        holder.mConAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItems.get(position).setChapter(mItems.get(position).chapter + 1);
                updateChapter(position, mItems.get(position).getChapter());
            }


        });

    }

    private void updateChapter(final int position, int value) {
        final ProgressDialog progressDoalog;
        progressDoalog = new ProgressDialog(mContext);
        progressDoalog.setMessage("잠시만 기다리세요....");
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDoalog.show();

        mActivity.retroClient.postUpdateMyContents(mItems.get(position).id, mActivity.userData.id, value, new RetroCallback() {

            @Override
            public void onError(Throwable t) {
                Toast.makeText(mContext, "서버 접속에 실패 하였습니다.", Toast.LENGTH_SHORT).show();
                progressDoalog.dismiss();
            }

            @Override
            public void onSuccess(int code, Object receivedData) {
                mActivity.refrshList(mItems);
                progressDoalog.dismiss();
            }

            @Override
            public void onFailure(int code) {
                progressDoalog.dismiss();
            }
        });
    }


    // 필수 오버라이드 : 데이터 갯수 반환
    @Override
        public int getItemCount() {
        return mItems.size();
    }

}
