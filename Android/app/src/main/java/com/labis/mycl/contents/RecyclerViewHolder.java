package com.labis.mycl.contents;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.labis.mycl.R;

/**
 * Created by owner on 2017-10-11.
 */

public class RecyclerViewHolder extends RecyclerView.ViewHolder {
    public ImageView mImgView;
    public TextView mName;
    public TextView mNameOrg;
    public TextView mSeason;
    public TextView mIndex;

    public RecyclerViewHolder(View itemView) {
        super(itemView);

        mImgView = (ImageView) itemView.findViewById(R.id.imageView);
        mName = (TextView) itemView.findViewById(R.id.name);
        mNameOrg = (TextView) itemView.findViewById(R.id.name2);
        mSeason = (TextView) itemView.findViewById(R.id.season);
        mIndex = (TextView) itemView.findViewById(R.id.index);
    }
}
