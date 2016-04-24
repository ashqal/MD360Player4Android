package com.asha.vrlib.strategy.display;

import android.app.Activity;
import android.opengl.GLSurfaceView;

import java.util.List;

/**
 * Created by hzqiujiadi on 16/3/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class NormalStrategy extends AbsDisplayStrategy {

    public NormalStrategy(List<GLSurfaceView> glSurfaceViewList) {
        super(glSurfaceViewList);
    }

    @Override
    public void on(Activity activity) {
        setVisibleSize(1);
    }

    @Override
    public void off(Activity activity) {}

    @Override
    public boolean isSupport(Activity activity) {
        return true;
    }

    @Override
    public int getVisibleSize() {
        return 1;
    }
}
