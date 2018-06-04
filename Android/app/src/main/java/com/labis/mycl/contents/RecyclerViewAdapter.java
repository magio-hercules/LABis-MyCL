package com.labis.mycl.contents;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
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
import com.labis.mycl.util.PicassoTransformations;
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

        return new RecyclerViewHolder(v);
    }

    // 필수 오버라이드 : 재활용되는 View 가 호출, Adapter 가 해당 position 에 해당하는 데이터를 결합
    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, final int position) {

        if (mActivity.editContents.contains(mItems.get(position))) {
            //holder.mTitleDiv.setBackgroundColor(ContextCompat.getColor(mContext, R.color.actionBar));
        } else {
          //  holder.mTitleDiv.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        }

        // 해당 position에 해당하는 데이터 결합
        mPosition = position;

        // 스페셜 줄임말 처리
        String genreText = mActivity.genreMap.get(mItems.get(position).gen_id);
        if(genreText != null) {
            if (genreText.equals("한국 드라마")) {
                genreText = "한드";
            } else if (genreText.equals("미국 드라마")) {
                genreText = "미드";
            } else if (genreText.equals("일본 드라마")) {
                genreText = "일드";
            } else if (genreText.equals("애니메이션")) {
                genreText = "애니";
            }
        }
        holder.mGen.setText(genreText);
        Picasso.get().load(mItems.get(position).image).transform(PicassoTransformations.resizeTransformation).into(holder.mImgView);
        holder.mName.setText(mItems.get(position).name + "  ");
        holder.mNameOrg.setText(mItems.get(position).name_org);

        if(mActivity.modeStatus == "MY") {
            holder.mGen.setBackground(mActivity.getResources().getDrawable(R.color.colorPrimary));

            // My List Season Info
            if(mItems.get(position).season > 0) {
                holder.mMyListSeason.setVisibility(View.VISIBLE);
                holder.mMyListSeason.setText("시즌" + String.valueOf(mItems.get(position).season));
            }

            // Chapter Info
            if(mItems.get(position).chapter > 0) {
                holder.mThirdDivMy.setVisibility(View.VISIBLE);
                holder.mIndex.setText(String.valueOf(mItems.get(position).chapter) + "화");
            }

        } else if(mActivity.modeStatus == "TOTAL") {
            holder.mGen.setBackground(mActivity.getResources().getDrawable(R.color.actionBar));

            // Total List Season Info
            if(mItems.get(position).season > 0) {
                holder.mThirdDivTotal.setVisibility(View.VISIBLE);
                holder.mTotalListSeason.setText("시즌" + mItems.get(position).season);
            }
        }

       holder.mTitleDiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterDetailPage(position);
            }
        });

        holder.mImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterDetailPage(position);
            }
        });

        holder.mConMinusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mItems.get(position).chapter > 1) {
                    mItems.get(position).setChapter(mItems.get(position).chapter - 1);
                    updateChapter(position, mItems.get(position).getChapter());
                } else {
                    Toast.makeText(mContext, "더 이상 안됩니다 -_-;;", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.mConAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mItems.get(position).chapter < 999) {
                    mItems.get(position).setChapter(mItems.get(position).chapter + 1);
                    updateChapter(position, mItems.get(position).getChapter());
                } else {
                    Toast.makeText(mContext, "더 이상 안됩니다 -_-;;", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void enterDetailPage(int position) {
        Content data = mItems.get(position);
        if (!data.equals(null) && !mActivity.isMultiSelect) {
            Intent i = new Intent(mActivity, DetailActivity.class);
            i.putExtra("CONTENT", mItems.get(position));
            i.putExtra("MODE", mActivity.modeStatus);
            mActivity.startActivity(i);
            mActivity.overridePendingTransition(R.anim.rightin_activity, R.anim.no_move_activity);
        }
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
                mActivity.updateItemDataView(position);
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
