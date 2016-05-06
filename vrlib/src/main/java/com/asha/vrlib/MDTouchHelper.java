package com.asha.vrlib;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by hzqiujiadi on 16/5/6.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDTouchHelper {
    private static final String TAG = "MDTouchHelper";
    private MDVRLibrary.IGestureListener mGestureListener;
    private MDVRLibrary.IDragListener mDragListener;
    private GestureDetector mGestureDetector;

    public MDTouchHelper(Context context) {
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (mGestureListener != null)
                    mGestureListener.onClick(e);
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (mDragListener != null)
                    mDragListener.onDrag(distanceX,distanceY);
                return true;
            }
        });
    }

    public boolean handleTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    public void setGestureListener(MDVRLibrary.IGestureListener mGestureListener) {
        this.mGestureListener = mGestureListener;
    }

    public void setDragListener(MDVRLibrary.IDragListener mDragListener) {
        this.mDragListener = mDragListener;
    }
}
