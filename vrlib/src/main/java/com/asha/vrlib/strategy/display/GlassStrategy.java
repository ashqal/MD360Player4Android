package com.asha.vrlib.strategy.display;

import android.app.Activity;
import android.opengl.GLSurfaceView;

import java.util.List;

/**
 * Created by hzqiujiadi on 16/3/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class GlassStrategy extends AbsDisplayStrategy {

    public GlassStrategy(List<GLSurfaceView> glSurfaceViewList) {
        super(glSurfaceViewList);
    }


    @Override
    public void on(Activity activity) {
        int max = getGLSurfaceViewList().size();
       setVisibleSize(max);
    }

    @Override
    public void off(Activity activity) {}

    @Override
    public int getVisibleSize() {
        return getGLSurfaceViewList().size();
    }
}
