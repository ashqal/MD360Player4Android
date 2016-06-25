package com.asha.vrlib.strategy.interactive;

import android.app.Activity;
import android.hardware.SensorEventListener;

import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.strategy.ModeManager;
import com.asha.vrlib.strategy.projection.ProjectionModeManager;

/**
 * Created by hzqiujiadi on 16/3/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class InteractiveModeManager extends ModeManager<AbsInteractiveStrategy> implements IInteractiveMode {

    private static int[] sModes = {MDVRLibrary.INTERACTIVE_MODE_MOTION,
            MDVRLibrary.INTERACTIVE_MODE_TOUCH,
            MDVRLibrary.INTERACTIVE_MODE_MOTION_WITH_TOUCH,
    };

    public static class Params{
        public int mMotionDelay;
        public SensorEventListener mSensorListener;
        public ProjectionModeManager projectionModeManager;
    }

    private Params mParams;

    public InteractiveModeManager(int mode, Params params) {
        super(mode);
        mParams = params;
    }

    @Override
    protected int[] getModes() {
        return sModes;
    }

    @Override
    public void switchMode(Activity activity, int mode) {
        super.switchMode(activity, mode);
        if (isResumed()) onResume(activity);
    }

    @Override
    protected AbsInteractiveStrategy createStrategy(int mode) {
        switch (mode){
            case MDVRLibrary.INTERACTIVE_MODE_MOTION:
                return new MotionStrategy(mParams);
            case MDVRLibrary.INTERACTIVE_MODE_MOTION_WITH_TOUCH:
                return new MotionWithTouchStrategy(mParams);
            case MDVRLibrary.INTERACTIVE_MODE_TOUCH:
            default:
                return new TouchStrategy(mParams);
        }
    }

    /**
     * handle touch touch to rotate the model
     *
     * @param distanceX x
     * @param distanceY y
     * @return true if handled.
     */
    @Override
    public boolean handleDrag(int distanceX, int distanceY) {
        return getStrategy().handleDrag(distanceX,distanceY);
    }
}
