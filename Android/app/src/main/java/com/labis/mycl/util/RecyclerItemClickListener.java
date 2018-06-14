package com.labis.mycl.util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.labis.mycl.contents.ContentsActivity;

/**
 * Created by Jaison on 21/10/15.
 */
public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener
{
    public static interface OnItemClickListener
    {
        public void onItemClick(View view, int position);
        public void onItemLongClick(View view, int position);
    }

    private OnItemClickListener mListener;
    private GestureDetector mGestureDetector;
    private ContentsActivity mActivity;
    private float x1,x2,y1,y2;
    static final int MIN_DISTANCE = 200;
    static final int Y_TOUCH_HEIGHT = 100;

    public RecyclerItemClickListener(ContentsActivity activity, Context context, final RecyclerView recyclerView, OnItemClickListener listener)
    {
        mListener = listener;
        mActivity = activity;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener()
        {
            @Override
            public boolean onSingleTapUp(MotionEvent e)
            {
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e)
            {
                View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());

                if(childView != null && mListener != null)
                {
                    mListener.onItemLongClick(childView, recyclerView.getChildPosition(childView));
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e)
    {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = e.getX();
                y1 = e.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = e.getX();
                y2 = e.getY();
                float deltaX = x2 - x1;
                float deltaY = Math.abs(y2 - y1);
                if (Math.abs(deltaX) > MIN_DISTANCE && deltaY < Y_TOUCH_HEIGHT) {
                    if (mActivity.modeStatus == "MY") {
                        mActivity.loadTotalContent();
                    } else if (mActivity.modeStatus == "TOTAL") {
                        mActivity.loadMyContents();
                    }
                    return true;
                }
                break;
            default:
                break;
        }

        View childView = view.findChildViewUnder(e.getX(), e.getY());

        if(childView != null && mListener != null && mGestureDetector.onTouchEvent(e))
        {
            mListener.onItemClick(childView, view.getChildPosition(childView));
        }

        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView view, MotionEvent event) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}