package com.labis.mycl.contents;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.labis.mycl.R;

/**
 * Created by owner on 2017-10-11.
 */

public class RecyclerViewHolder extends RecyclerView.ViewHolder {
    public TextView mGen;
    public ImageView mImgView;
    public TextView mName;
    public TextView mNameOrg;
    public TextView mConAddBtn;
    public TextView mConMinusBtn;
    public TextView mMyListSeason;
    public TextView mIndex;

    public LinearLayout mContentItemDiv;
    public LinearLayout mThirdDivMy;
    public LinearLayout mTitleDiv;
    public LinearLayout mThirdDivTotal;
    public TextView mTotalListSeason;

    public RecyclerViewHolder(View itemView) {
        super(itemView);

        mContentItemDiv = (LinearLayout)itemView.findViewById(R.id.contentItemDiv);

        mGen = (TextView) itemView.findViewById(R.id.content_genre_text);
        mImgView = (ImageView) itemView.findViewById(R.id.content_image);

        mTitleDiv = (LinearLayout)itemView.findViewById(R.id.secondDiv);
        mName = (TextView) itemView.findViewById(R.id.content_title);
        mNameOrg = (TextView) itemView.findViewById(R.id.content_sub_title);

        mConAddBtn = (TextView) itemView.findViewById(R.id.ConAddBtn);
        mConMinusBtn = (TextView) itemView.findViewById(R.id.ConMinusBtn);

        mMyListSeason = (TextView) itemView.findViewById(R.id.content_mylist_season);
        mIndex = (TextView) itemView.findViewById(R.id.index);
        mThirdDivMy = (LinearLayout)itemView.findViewById(R.id.thirdDivMyList);

        mThirdDivTotal = (LinearLayout)itemView.findViewById(R.id.thirdDivTtotalList);
        mTotalListSeason = (TextView) itemView.findViewById(R.id.content_totallist_season);
    }
}
