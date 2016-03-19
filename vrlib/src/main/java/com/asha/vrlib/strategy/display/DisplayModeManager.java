package com.asha.vrlib.strategy.display;

import android.app.Activity;
import android.opengl.GLSurfaceView;

import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.strategy.ModeManager;

import java.util.List;

/**
 * Created by hzqiujiadi on 16/3/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class DisplayModeManager extends ModeManager<AbsDisplayStrategy> implements IDisplayMode {

    private List<GLSurfaceView> mGLSurfaceViews;
    public DisplayModeManager(int mode, List<GLSurfaceView> glSurfaceViews) {
        super(mode);
        this.mGLSurfaceViews = glSurfaceViews;
    }

    @Override
    public void switchMode(Activity activity) {
        int nextMode = getMode() == MDVRLibrary.DISPLAY_MODE_NORMAL ?
                MDVRLibrary.DISPLAY_MODE_GLASS
                : MDVRLibrary.DISPLAY_MODE_NORMAL;
        switchMode(activity,nextMode);
    }

    @Override
    protected AbsDisplayStrategy createStrategy(int mode) {
        switch (mode){
            case MDVRLibrary.DISPLAY_MODE_GLASS:
                return new GlassStrategy(mGLSurfaceViews);
            case MDVRLibrary.DISPLAY_MODE_NORMAL:
            default:
                return new NormalStrategy(mGLSurfaceViews);
        }
    }

    @Override
    public int getVisibleSize() {
        return getStrategy().getVisibleSize();
    }

}
