package com.asha.vrlib.strategy.display;

import android.app.Activity;

import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.strategy.ModeManager;

/**
 * Created by hzqiujiadi on 16/3/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class DisplayModeManager extends ModeManager<AbsDisplayStrategy> implements IDisplayMode {

    public DisplayModeManager(int mode) {
        super(mode);
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
                return new GlassStrategy();
            case MDVRLibrary.DISPLAY_MODE_NORMAL:
            default:
                return new NormalStrategy();
        }
    }

    @Override
    public int getVisibleSize() {
        return getStrategy().getVisibleSize();
    }

}
