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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.AdSettings;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.NativeBannerAd;
import com.labis.mycl.R;
import com.labis.mycl.model.Content;
import com.labis.mycl.rest.RetroCallback;
import com.labis.mycl.util.PicassoTransformations;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";

    public ArrayList<Content> mItems;
    Context mContext;
    private ContentsActivity mActivity;
    RecyclerViewHolder mHolder;

    public RecyclerViewAdapter(ContentsActivity activity, ArrayList itemList) {
        mActivity = activity;
        mItems = itemList;
    }

    // 필수 오버라이드 : View 생성
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();

        View view;
        RecyclerView.ViewHolder viewHolder;
        if(viewType == 1){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
            viewHolder =  new RecyclerViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_native_banner_ad, parent, false);
            viewHolder = new RecyclerNativeAdViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        // AD, In-App Purchase (Remove All ADS)
        if(mActivity.RemoveAD) {
            return 1;
        }

        // 2 : Native AD Return (9th Item List)
        if(position > 0 && position % 9 == 0) {
            return 2;
        }

        // 1 : Normal content List
        return 1;
    }

    // 필수 오버라이드 : 재활용되는 View 가 호출, Adapter 가 해당 position 에 해당하는 데이터를 결합
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        switch (holder.getItemViewType()) {
            case 1:
                // 일반 리스트
                RecyclerViewHolder h1 = (RecyclerViewHolder)holder;
                onBindRecyclerViewHolder(h1, position);
                break;
            case 2:
                // 네이티브 광고 리스트
                RecyclerNativeAdViewHolder h2 = (RecyclerNativeAdViewHolder)holder;
                onBindRecyclerNativeAdViewHolder(h2, position);
                break;
        }
    }

    // 네이티브 광고 바인딩
    private void onBindRecyclerNativeAdViewHolder(final RecyclerNativeAdViewHolder holder, final int position) {
        final NativeBannerAd nativeBannerAd = new NativeBannerAd(mContext, mContext.getString(R.string.facebook_native_contents_all));

        inflateDefaultAD(holder, nativeBannerAd);

        nativeBannerAd.setAdListener(new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
                // Native ad finished downloading all assets
                Log.e(TAG, "Native ad finished downloading all assets.");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Native ad failed to load
                Log.e(TAG, "Native ad failed to load: " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Native ad is loaded and ready to be displayed
                Log.d(TAG, "Native ad is loaded and ready to be displayed!");

                // Race condition, load() called again before last ad was displayed
                if (nativeBannerAd == null || nativeBannerAd != ad) {
                    return;
                }
                // Inflate Native Banner Ad into Container
                inflateAd(holder, nativeBannerAd);
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Native ad clicked
                Log.d(TAG, "Native ad clicked!");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Native ad impression
                Log.d(TAG, "Native ad impression logged!");
            }
        });

        // load the ad
        nativeBannerAd.loadAd();
    }

    private void inflateDefaultAD(RecyclerNativeAdViewHolder holder, NativeBannerAd nativeBannerAd) {
        // Set the Text.
        holder.nativeAdCallToAction.setVisibility(View.INVISIBLE);
        holder.nativeAdTitle.setText("Facebook Advertiser");
        holder.nativeAdSocialContext.setText("Get it on Google Play");
        holder.sponsoredLabel.setText("Sponsored");

        // Register the Title and CTA button to listen for clicks.
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(holder.nativeAdTitle);
        clickableViews.add(holder.nativeAdCallToAction);
        nativeBannerAd.registerViewForInteraction(holder.nativeAdLayout, holder.nativeAdIconView, clickableViews);

        if(mActivity.modeStatus == "MY") {
            holder.mGen.setBackground(mActivity.getResources().getDrawable(R.color.colorPrimary));
        } else {
            holder.mGen.setBackground(mActivity.getResources().getDrawable(R.color.actionBar));
        }
    }

    private void inflateAd(RecyclerNativeAdViewHolder holder, NativeBannerAd nativeBannerAd) {
        // Unregister last ad
        nativeBannerAd.unregisterView();

        // Add the AdChoices icon
        AdOptionsView adOptionsView = new AdOptionsView(mContext, nativeBannerAd, holder.nativeAdLayout);
        holder.adChoicesContainer.removeAllViews();
        holder.adChoicesContainer.addView(adOptionsView, 0);

        // Set the Text.
        holder.nativeAdCallToAction.setText(nativeBannerAd.getAdCallToAction());
        holder.nativeAdCallToAction.setVisibility(nativeBannerAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        holder.nativeAdTitle.setText(nativeBannerAd.getAdvertiserName());
        holder.nativeAdSocialContext.setText(nativeBannerAd.getAdSocialContext());
        holder.sponsoredLabel.setText(nativeBannerAd.getSponsoredTranslation());

        // Register the Title and CTA button to listen for clicks.
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(holder.nativeAdTitle);
        clickableViews.add(holder.nativeAdCallToAction);
        nativeBannerAd.registerViewForInteraction(holder.nativeAdLayout, holder.nativeAdIconView, clickableViews);

        if(mActivity.modeStatus == "MY") {
            holder.mGen.setBackground(mActivity.getResources().getDrawable(R.color.colorPrimary));
        } else {
            holder.mGen.setBackground(mActivity.getResources().getDrawable(R.color.actionBar));
        }
    }



    // 일반 아이템 바인딩
    private void onBindRecyclerViewHolder(RecyclerViewHolder holder, final int position) {
        if (mActivity.editContents.contains(mItems.get(position))) {
            holder.mContentItemDiv.setBackgroundColor(Color.parseColor("#F0F0F0"));
        } else {
            holder.mContentItemDiv.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }

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
        if(mItems.get(position).image != null && mItems.get(position).image.length() > 0) {
            holder.mImgView.setVisibility(View.VISIBLE);
            Picasso.get().load(mItems.get(position).image).into(holder.mImgView);
        } else {
            holder.mImgView.setVisibility(View.GONE);
            holder.mNullText.setVisibility(View.VISIBLE);
        }
        holder.mName.setText(mItems.get(position).name);
        if(mItems.get(position).name_org != null && mItems.get(position).name_org.length() > 0) {
            holder.mNameOrg.setVisibility(View.VISIBLE);
            holder.mNameOrg.setText(mItems.get(position).name_org);
        } else {
            holder.mNameOrg.setVisibility(View.GONE);
        }

        if(mActivity.modeStatus == "MY") {
            holder.mGen.setBackground(mActivity.getResources().getDrawable(R.color.colorPrimary));

            // My List Season Info
            if(mItems.get(position).season > 0) {
                holder.mMyListSeason.setVisibility(View.VISIBLE);
                holder.mMyListSeason.setText("시즌" + String.valueOf(mItems.get(position).season));
            } else {
                holder.mMyListSeason.setVisibility(View.GONE);
            }

            // Chapter Info
            String genID = mItems.get(position).gen_id;
            if(!isNoChapterGenre(genID)) {
                // 완결 여부 0 :  미결 / 1 : 완결
                if(mItems.get(position).score == 0) {
                    holder.mThirdDivMyStamp.setVisibility(View.GONE);
                    holder.mThirdDivMy.setVisibility(View.VISIBLE);
                    String eof = "화";
                    if(genID.indexOf('A') != -1) eof = "권";
                    holder.mIndex.setText(String.valueOf(mItems.get(position).chapter) + eof);
                } else {
                    holder.mThirdDivMy.setVisibility(View.GONE);
                    holder.mThirdDivMyStamp.setVisibility(View.VISIBLE);
                }
            } else {
                holder.mThirdDivMyStamp.setVisibility(View.GONE);
                holder.mThirdDivMy.setVisibility(View.GONE);
            }

        } else if(mActivity.modeStatus == "TOTAL") {
            holder.mGen.setBackground(mActivity.getResources().getDrawable(R.color.actionBar));

            // Total List Season Info
            if(mItems.get(position).season > 0) {
                holder.mThirdDivTotal.setVisibility(View.VISIBLE);
                holder.mTotalListSeason.setText("시즌" + mItems.get(position).season);
            } else {
                holder.mThirdDivTotal.setVisibility(View.GONE);
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

    private Boolean isNoChapterGenre(String gen_id) {
        // 영화
        if(gen_id.equals("B02")) {
            return true;
        }
        return false;
    }

    private void enterDetailPage(int position) {
        mActivity.editPosition = position;
        Content data = mItems.get(position);
        if (!data.equals(null) && !mActivity.isMultiSelect) {
            Intent i = new Intent(mActivity, DetailActivity.class);
            i.putExtra("CONTENT", mItems.get(position));
            i.putExtra("MODE", mActivity.modeStatus);
            i.putExtra("USER", mActivity.userData.id);
            i.putExtra("GUESTMODE", mActivity.bGuestMode);
            mActivity.startActivityForResult(i, mActivity.PICK_EDIT_REQUEST);
            mActivity.overridePendingTransition(R.anim.rightin_activity, R.anim.no_move_activity);
        }
    }

    private void updateChapter(final int position, int chapter) {
        final ProgressDialog progressDoalog;
        progressDoalog = new ProgressDialog(mContext);
        progressDoalog.setMessage("잠시만 기다리세요....");
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDoalog.show();

        mActivity.retroClient.postUpdateMyContents(mItems.get(position).id, mActivity.userData.id, chapter, mItems.get(position).favorite, mItems.get(position).score, mItems.get(position).comment, new RetroCallback() {

            @Override
            public void onError(Throwable t) {
                Toast.makeText(mContext, "서버 접속에 실패 하였습니다.", Toast.LENGTH_SHORT).show();
                progressDoalog.dismiss();
            }

            @Override
            public void onSuccess(int code, Object receivedData) {
                mActivity.myContentsRefresh = true;
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
