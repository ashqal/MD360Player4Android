package com.asha.vrlib.strategy;


import android.content.Context;

import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.common.MDGLHandler;
import com.asha.vrlib.common.MDMainHandler;

import java.util.Arrays;

import static com.asha.vrlib.common.VRUtil.checkMainThread;

/**
 * Created by hzqiujiadi on 16/3/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
public abstract class ModeManager<T extends IModeStrategy> {
    private int mMode;
    private T mStrategy;
    private MDVRLibrary.INotSupportCallback mCallback;
    private MDGLHandler mGLHandler;

    public ModeManager(int mode, MDGLHandler handler) {
        this.mGLHandler = handler;
        this.mMode = mode;
    }

    /**
     * must call after new instance
     * @param context context
     */
    public void prepare(Context context, MDVRLibrary.INotSupportCallback callback){
        mCallback = callback;
        initMode(context,mMode);
    }

    abstract protected T createStrategy(int mode);

    abstract protected int[] getModes();

    private void initMode(Context context, final int mode){
        if (mStrategy != null){
            off(context);
        }
        mStrategy = createStrategy(mode);
        if (!mStrategy.isSupport(context)){
            MDMainHandler.sharedHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null) {
                        mCallback.onNotSupport(mode);
                    }
                }
            });
            return;
        }

        // t
        on(context);
    }

    public void switchMode(final Context context){
        int[] modes = getModes();
        int mode = getMode();
        int index = Arrays.binarySearch(modes, mode);
        int nextIndex = (index + 1) %  modes.length;
        int nextMode = modes[nextIndex];

        switchMode(context, nextMode);
    }

    public void switchMode(final Context context, final int mode){
        if (mode == getMode()) return;
        mMode = mode;

        initMode(context, mMode);
    }

    public void on(final Context context) {
        checkMainThread("strategy on must call from main thread!");

        final T tmpStrategy = mStrategy;
        if (tmpStrategy.isSupport(context)){
            getGLHandler().post(new Runnable() {
                @Override
                public void run() {
                    tmpStrategy.turnOnInGL(context);
                }
            });
        }
    }

    public void off(final Context context) {
        checkMainThread("strategy off must call from main thread!");

        final T tmpStrategy = mStrategy;
        if (tmpStrategy.isSupport(context)){
            getGLHandler().post(new Runnable() {
                @Override
                public void run() {
                    tmpStrategy.turnOffInGL(context);
                }
            });
        }
    }

    protected T getStrategy() {
        return mStrategy;
    }

    public int getMode() {
        return mMode;
    }

    public MDGLHandler getGLHandler() {
        return mGLHandler;
    }
}
