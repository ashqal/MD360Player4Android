package com.asha.vrlib.plugins;

import android.content.Context;

import com.asha.vrlib.MD360Director;

/**
 * Created by hzqiujiadi on 2017/5/13.
 * hzqiujiadi ashqalcn@gmail.com
 */

public class MDPluginAdapter extends MDAbsPlugin {
    @Override
    protected void initInGL(Context context) {

    }

    @Override
    public void destroyInGL() {

    }

    @Override
    public void beforeRenderer(int totalWidth, int totalHeight) {

    }

    @Override
    public void renderer(int index, int itemWidth, int itemHeight, MD360Director director) {

    }

    @Override
    protected boolean removable() {
        return false;
    }
}
