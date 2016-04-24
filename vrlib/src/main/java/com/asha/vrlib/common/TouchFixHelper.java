package com.asha.vrlib.common;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Created by hzqiujiadi on 16/4/24.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class TouchFixHelper {

    private Threshold mThreshold;
    private boolean mBeginDragging;
    private View.OnTouchListener mOnTouchListener;

    public void setOnTouchListener(View.OnTouchListener onTouchListener) {
        this.mOnTouchListener = onTouchListener;
    }

    private static class Threshold {
        private boolean mInit;
        private float mPrevX;
        private float mPrevY;
        private float mThreshold;

        public Threshold(float threshold) {
            this.mThreshold = threshold * threshold;
        }

        private boolean checkAbsOverflow(float x, float y){
            float distance = (x - mPrevX) * (x - mPrevX) + (y - mPrevY) * (y - mPrevY);
            if (distance > mThreshold) return true;
            else return false;
        }

        public boolean absOverflow(float x,float y){
            if (!mInit){
                mPrevX = x;
                mPrevY = y;
                mInit = true;
                return false;
            }
            return checkAbsOverflow(x,y);
        }

        public void reset(){
            mInit = false;
            mPrevX = 0;
            mPrevY = 0;
        }
    }

    public boolean onInterceptTouchEvent(View view, MotionEvent event) {
        ensureThreshold(view.getContext());
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_DOWN){

            // pass touch down.
            if (mOnTouchListener != null) mOnTouchListener.onTouch(view,event);

            mThreshold.reset();
            mBeginDragging = false;
            mThreshold.absOverflow(event.getX(),event.getY());
            return false;
        }

        if (mBeginDragging) return true;

        if (action ==  MotionEvent.ACTION_MOVE){
            if ( mThreshold.absOverflow(event.getX(),event.getY()) ) {
                mBeginDragging = true;
            }
        }
        return mBeginDragging;
    }

    private void ensureThreshold(Context context){
        if (mThreshold == null){
            final ViewConfiguration configuration = ViewConfiguration.get(context);
            mThreshold = new Threshold(configuration.getScaledTouchSlop());
        }
    }
}
