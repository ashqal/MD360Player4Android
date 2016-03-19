package com.asha.vrlib.strategy.interactive;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.strategy.ModeManager;

import java.util.List;

/**
 * Created by hzqiujiadi on 16/3/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class InteractiveModeManager extends ModeManager<AbsInteractiveStrategy> implements IInteractiveMode {

    private List<MD360Director> mDirectorList;
    private boolean mIsResumed;
    public InteractiveModeManager(int mode, List<MD360Director> directorList) {
        super(mode);
        this.mDirectorList = directorList;
    }

    @Override
    public void switchMode(Activity activity) {
        int nextMode = getMode() == MDVRLibrary.INTERACTIVE_MODE_MOTION ?
                MDVRLibrary.INTERACTIVE_MODE_TOUCH
                : MDVRLibrary.INTERACTIVE_MODE_MOTION;
        switchMode(activity,nextMode);
        if (mIsResumed) onResume(activity);
    }

    @Override
    protected AbsInteractiveStrategy createStrategy(int mode) {
        switch (mode){
            case MDVRLibrary.INTERACTIVE_MODE_MOTION:
                return new MotionStrategy(mDirectorList);
            case MDVRLibrary.INTERACTIVE_MODE_TOUCH:
            default:
                return new TouchStrategy(mDirectorList);
        }
    }

    @Override
    public void onResume(Context context) {
        mIsResumed = true;
        getStrategy().onResume(context);
    }

    @Override
    public void onPause(Context context) {
        mIsResumed = false;
        getStrategy().onPause(context);
    }

    @Override
    public boolean handleTouchEvent(MotionEvent event) {
        return getStrategy().handleTouchEvent(event);
    }
}
