package com.asha.md360player4android;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.asha.vrlib.common.TouchFixHelper;

/**
 * Created by hzqiujiadi on 16/4/24.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class TouchFixLinearLayout extends LinearLayout {

    private TouchFixHelper mHelper = new TouchFixHelper();

    public TouchFixLinearLayout(Context context) {
        super(context);
    }

    public TouchFixLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchFixLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TouchFixLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mHelper.onInterceptTouchEvent(this, event);
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        super.setOnTouchListener(l);
        mHelper.setOnTouchListener(l);
    }
}
