package com.asha.vrlib.strategy.display;

import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.strategy.ModeManager;

/**
 * Created by hzqiujiadi on 16/3/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class DisplayModeManager extends ModeManager<AbsDisplayStrategy> implements IDisplayMode {

    public static int[] sModes = {MDVRLibrary.DISPLAY_MODE_NORMAL, MDVRLibrary.DISPLAY_MODE_GLASS};

    public DisplayModeManager(int mode) {
        super(mode);
    }

    @Override
    protected int[] getModes() {
        return sModes;
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
