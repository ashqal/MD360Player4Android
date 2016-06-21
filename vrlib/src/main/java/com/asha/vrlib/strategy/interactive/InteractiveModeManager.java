package com.asha.vrlib.strategy.interactive;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorEventListener;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.strategy.ModeManager;

import java.util.Arrays;
import java.util.List;

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
        public List<MD360Director> mDirectorList;
        public int mMotionDelay;
        public SensorEventListener mSensorListener;
    }

    private boolean mIsResumed;
    private Params mParams;
    public InteractiveModeManager(int mode, Params params) {
        super(mode);
        mParams = params;
    }

    @Override
    public void switchMode(Activity activity) {
        int mode = getMode();
        int index = Arrays.binarySearch(sModes, mode);
        int nextIndex = (index + 1) %  sModes.length;
        int nextMode = sModes[nextIndex];

        switchMode(activity,nextMode);
    }

    @Override
    public void switchMode(Activity activity, int mode) {
        super.switchMode(activity, mode);
        if (mIsResumed) onResume(activity);
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

    @Override
    public void onResume(Context context) {
        mIsResumed = true;
        if (getStrategy().isSupport((Activity)context)){
            getStrategy().onResume(context);
        }
    }

    @Override
    public void onPause(Context context) {
        mIsResumed = false;
        if (getStrategy().isSupport((Activity)context)){
            getStrategy().onPause(context);
        }
    }

    /**
     * handle touch touch to rotate the model
     *
     * @param distanceX
     * @param distanceY
     * @return true if handled.
     */
    @Override
    public boolean handleDrag(int distanceX, int distanceY) {
        return getStrategy().handleDrag(distanceX,distanceY);
    }
}
