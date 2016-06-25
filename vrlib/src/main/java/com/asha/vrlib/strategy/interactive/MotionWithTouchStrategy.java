package com.asha.vrlib.strategy.interactive;

import android.content.res.Resources;

import com.asha.vrlib.MD360Director;

/**
 * Created by hzqiujiadi on 16/6/10.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MotionWithTouchStrategy extends MotionStrategy {

    private static final float sDensity =  Resources.getSystem().getDisplayMetrics().density;

    private static final float sDamping = 0.2f;

    public MotionWithTouchStrategy(InteractiveModeManager.Params params) {
        super(params);
    }

    @Override
    public boolean handleDrag(int distanceX, int distanceY) {
        for (MD360Director director : getDirectorList()){
            director.setDeltaX(director.getDeltaX() - distanceX / sDensity * sDamping);
            // director.setDeltaY(director.getDeltaY() - distanceY / sDensity * sDamping);
        }
        return false;
    }
}
