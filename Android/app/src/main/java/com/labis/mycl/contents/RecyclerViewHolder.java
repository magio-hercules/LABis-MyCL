package com.labis.mycl.contents;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.labis.mycl.R;

/**
 * Created by owner on 2017-10-11.
 */

public class RecyclerViewHolder extends RecyclerView.ViewHolder {

    public TextView mIndex;
    public TextView mName;

    public RecyclerViewHolder(View itemView) {
        super(itemView);

        mIndex = (TextView) itemView.findViewById(R.id.index);
        mName = (TextView) itemView.findViewById(R.id.name);
    }
}
