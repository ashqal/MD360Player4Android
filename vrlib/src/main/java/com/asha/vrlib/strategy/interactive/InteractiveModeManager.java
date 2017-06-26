package com.asha.vrlib.strategy.interactive;

import android.content.Context;
import android.hardware.SensorEventListener;

import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.common.MDGLHandler;
import com.asha.vrlib.strategy.ModeManager;
import com.asha.vrlib.strategy.projection.ProjectionModeManager;

/**
 * Created by hzqiujiadi on 16/3/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class InteractiveModeManager extends ModeManager<AbsInteractiveStrategy> implements IInteractiveMode {

    private boolean mIsResumed;

    private static int[] sModes = {MDVRLibrary.INTERACTIVE_MODE_MOTION,
            MDVRLibrary.INTERACTIVE_MODE_TOUCH,
            MDVRLibrary.INTERACTIVE_MODE_MOTION_WITH_TOUCH,
            MDVRLibrary.INTERACTIVE_MODE_CARDBORAD_MOTION,
    };

    public static class Params{
        public int mMotionDelay;
        public SensorEventListener mSensorListener;
        public ProjectionModeManager projectionModeManager;
        public MDGLHandler glHandler;
    }

    private Params mParams;

    public InteractiveModeManager(int mode, MDGLHandler handler, Params params) {
        super(mode, handler);
        mParams = params;
        mParams.glHandler = getGLHandler();
    }

    @Override
    protected int[] getModes() {
        return sModes;
    }

    @Override
    protected AbsInteractiveStrategy createStrategy(int mode) {
        switch (mode){
            case MDVRLibrary.INTERACTIVE_MODE_MOTION:
                return new MotionStrategy(mParams);
            case MDVRLibrary.INTERACTIVE_MODE_MOTION_WITH_TOUCH:
                return new MotionWithTouchStrategy(mParams);
            case MDVRLibrary.INTERACTIVE_MODE_CARDBORAD_MOTION:
                return new CardboardMotionStrategy(mParams);
            case MDVRLibrary.INTERACTIVE_MODE_CARDBORAD_MOTION_WITH_TOUCH:
                return new CardboardMTStrategy(mParams);
            case MDVRLibrary.INTERACTIVE_MODE_TOUCH:
            default:
                return new TouchStrategy(mParams);
        }
    }

    private UpdateDragRunnable updateDragRunnable = new UpdateDragRunnable();

    /**
     * handle touch touch to rotate the model
     *
     * @param distanceX x
     * @param distanceY y
     * @return true if handled.
     */
    @Override
    public boolean handleDrag(final int distanceX, final int distanceY) {
        updateDragRunnable.handleDrag(distanceX, distanceY);
        getGLHandler().post(updateDragRunnable);
        return false;
    }

    @Override
    public void onOrientationChanged(final Context context) {
        getGLHandler().post(new Runnable() {
            @Override
            public void run() {
                getStrategy().onOrientationChanged(context);
            }
        });
    }

    private class UpdateDragRunnable implements Runnable {
        private int distanceX;
        private int distanceY;

        private void handleDrag(int distanceX, int distanceY){
            this.distanceX = distanceX;
            this.distanceY = distanceY;
        }

        @Override
        public void run() {
            getStrategy().handleDrag(distanceX, distanceY);
        }
    }

    public void onResume(Context context) {
        mIsResumed = true;
        if (getStrategy().isSupport(context)){
            getStrategy().onResume(context);
        }
    }

    @Override
    public void on(Context context) {
        super.on(context);

        if (mIsResumed){
            onResume(context);
        }
    }

    public void onPause(Context context) {
        mIsResumed = false;
        if (getStrategy().isSupport(context)){
            getStrategy().onPause(context);
        }
    }
}
