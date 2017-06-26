package com.asha.vrlib.strategy.display;

import android.content.Context;

/**
 * Created by hzqiujiadi on 16/3/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class GlassStrategy extends AbsDisplayStrategy {

    public GlassStrategy() {}

    @Override
    public void turnOnInGL(Context context) {}

    @Override
    public void turnOffInGL(Context context) {}

    @Override
    public boolean isSupport(Context context) {
        return true;
    }

    @Override
    public int getVisibleSize() {
        return 2;
    }
}
