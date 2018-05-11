package com.labis.mycl.contents;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
    public TextView mSeason;
    public TextView mIndex;

    public RecyclerViewHolder(View itemView) {
        super(itemView);

        mGen = (TextView) itemView.findViewById(R.id.gentext);
        mImgView = (ImageView) itemView.findViewById(R.id.imageView);
        mName = (TextView) itemView.findViewById(R.id.name);
        mNameOrg = (TextView) itemView.findViewById(R.id.name2);
        mConAddBtn = (TextView) itemView.findViewById(R.id.ConAddBtn);
        mConMinusBtn = (TextView) itemView.findViewById(R.id.ConMinusBtn);
        mSeason = (TextView) itemView.findViewById(R.id.season);
        mIndex = (TextView) itemView.findViewById(R.id.index);
    }
}
