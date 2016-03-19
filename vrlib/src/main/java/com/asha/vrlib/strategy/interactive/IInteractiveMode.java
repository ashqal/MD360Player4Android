package com.asha.vrlib.strategy.interactive;

import android.content.Context;
import android.view.MotionEvent;

/**
 * Created by hzqiujiadi on 16/3/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
public interface IInteractiveMode {
    void onResume(Context context);
    void onPause(Context context);
    boolean handleTouchEvent(MotionEvent event);
}
