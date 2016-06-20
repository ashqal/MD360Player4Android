package com.asha.vrlib.strategy;

import android.app.Activity;

import com.asha.vrlib.MDVRLibrary;

/**
 * Created by hzqiujiadi on 16/3/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
public abstract class ModeManager<T extends IModeStrategy> {
    private int mMode;
    private T mStrategy;
    private MDVRLibrary.INotSupportCallback mCallback;

    public ModeManager(int mode) {
        this.mMode = mode;
    }

    /**
     * must call after new instance
     * @param activity activity
     */
    public void prepare(Activity activity, MDVRLibrary.INotSupportCallback callback){
        mCallback = callback;
        initMode(activity,mMode);
    }

    abstract public void switchMode(Activity activity);

    abstract protected T createStrategy(int mode);

    private void initMode(Activity activity, int mode){
        if (mStrategy != null) mStrategy.off(activity);
        mStrategy = createStrategy(mode);
        if (!mStrategy.isSupport(activity)){
            if (mCallback != null) mCallback.onNotSupport(mode);
        } else {
            mStrategy.on(activity);
        }
    }

    public void switchMode(Activity activity, int mode){
        if (mode == getMode()) return;
        mMode = mode;
        initMode(activity,mMode);
    }

    public void on(Activity activity) {
        if (mStrategy.isSupport(activity))
            mStrategy.on(activity);
    }

    public void off(Activity activity) {
        if (mStrategy.isSupport(activity))
            mStrategy.off(activity);
    }

    protected T getStrategy() {
        return mStrategy;
    }

    public int getMode() {
        return mMode;
    }
}
