package com.asha.vrlib.strategy.interactive;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;

import com.asha.vrlib.MD360Director;

import java.util.List;

/**
 * Created by hzqiujiadi on 16/3/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class TouchStrategy extends AbsInteractiveStrategy {
    public TouchStrategy(List<MD360Director> directorList) {
        super(directorList);
    }

    @Override
    public void onResume(Context context) {}

    @Override
    public void onPause(Context context) {}

    @Override
    public boolean handleTouchEvent(MotionEvent event) {
        boolean handled = false;
        for (MD360Director director : getDirectorList()){
            handled |= director.handleTouchEvent(event);
        }
        return handled;
    }

    @Override
    public void on(Activity activity) {
        for (MD360Director director : getDirectorList()){
            director.reset();
        }
    }

    @Override
    public void off(Activity activity) {}
}
