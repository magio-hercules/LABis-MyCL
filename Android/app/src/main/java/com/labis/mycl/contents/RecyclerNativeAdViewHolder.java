package com.labis.mycl.contents;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.ads.AdIconView;
import com.facebook.ads.NativeAdLayout;
import com.labis.mycl.R;


public class RecyclerNativeAdViewHolder extends RecyclerView.ViewHolder {

    public NativeAdLayout nativeAdLayout;
    public TextView mGen;
    public AdIconView nativeAdIconView;
    public RelativeLayout adChoicesContainer;
    public TextView nativeAdTitle;
    public TextView nativeAdSocialContext;
    public TextView sponsoredLabel;
    public Button nativeAdCallToAction;

    public RecyclerNativeAdViewHolder(View itemView) {
        super(itemView);

        nativeAdLayout = (NativeAdLayout) itemView.findViewById(R.id.native_banner_ad_container);
        mGen = (TextView) itemView.findViewById(R.id.native_content_genre_text);
        nativeAdIconView = (AdIconView) itemView.findViewById(R.id.native_icon_view);
        adChoicesContainer = (RelativeLayout) itemView.findViewById(R.id.ad_choices_container);

        nativeAdTitle = (TextView) itemView.findViewById(R.id.native_ad_title);
        nativeAdSocialContext = (TextView) itemView.findViewById(R.id.native_ad_social_context);
        sponsoredLabel = (TextView) itemView.findViewById(R.id.native_ad_sponsored_label);
        nativeAdCallToAction = (Button) itemView.findViewById(R.id.native_ad_call_to_action);
    }
}
