package com.asha.vrlib.strategy;

import android.app.Activity;

/**
 * Created by hzqiujiadi on 16/3/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
public abstract class ModeManager<T extends IModeStrategy> implements IModeStrategy {
    private int mMode;
    private T mStrategy;

    public ModeManager(int mode) {
        this.mMode = mode;
    }

    /**
     * must call after new instance
     * @param activity activity
     */
    public void prepare(Activity activity){
        initMode(activity,mMode);
    }

    abstract public void switchMode(Activity activity);

    abstract protected T createStrategy(int mode);

    private void initMode(Activity activity, int mode){
        if (mStrategy != null) mStrategy.off(activity);
        mStrategy = createStrategy(mode);
        mStrategy.on(activity);
    }

    protected void switchMode(Activity activity, int mode){
        if (mode == getMode()) return;
        mMode = mode;
        initMode(activity,mMode);
    }

    @Override
    public void on(Activity activity) {
        mStrategy.on(activity);
    }

    @Override
    public void off(Activity activity) {
        mStrategy.off(activity);
    }

    protected T getStrategy() {
        return mStrategy;
    }

    public int getMode() {
        return mMode;
    }
}
