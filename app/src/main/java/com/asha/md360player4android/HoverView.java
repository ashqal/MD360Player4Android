package com.asha.md360player4android;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Created by hzqiujiadi on 2017/4/21.
 * hzqiujiadi ashqalcn@gmail.com
 */

public class HoverView extends View {

    private Paint paint;

    private float x;

    private float y;

    private float radius;

    public HoverView(Context context) {
        super(context);
        initView();
    }

    public HoverView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public HoverView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HoverView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        paint = new Paint();
        paint.setColor(0xFF0000FF);
    }

    @Override
    public boolean onHoverEvent(MotionEvent event) {
        super.onHoverEvent(event);

        final int action = event.getActionMasked();
        switch (action){
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
                x = event.getX();
                y = event.getY();
                radius = (event.getEventTime() - event.getDownTime()) / 100 + 1;
                break;

            case MotionEvent.ACTION_HOVER_EXIT:
                radius = 0;
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (radius != 0){
            canvas.drawCircle(x, y, radius, paint);
        }
    }
}
