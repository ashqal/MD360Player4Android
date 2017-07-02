package com.asha.vrlib.strategy.display;

import android.content.Context;

/**
 * Created by hzqiujiadi on 16/3/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class NormalStrategy extends AbsDisplayStrategy {

    @Override
    public void turnOnInGL(Context context) {}

    @Override
    public void turnOffInGL(Context context) {}

    @Override
    public boolean isSupport(Context context) {
        return true;
    }

    @Override
    public void onResume(Context context) {

    }

    @Override
    public void onPause(Context context) {

    }

    @Override
    public int getVisibleSize() {
        return 1;
    }
}
