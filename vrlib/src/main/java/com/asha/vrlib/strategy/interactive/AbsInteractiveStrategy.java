package com.asha.vrlib.strategy.interactive;

import android.os.Handler;
import android.os.Looper;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.strategy.IModeStrategy;

import java.util.List;

/**
 * Created by hzqiujiadi on 16/3/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
public abstract class AbsInteractiveStrategy implements IModeStrategy, IInteractiveMode {

    private InteractiveModeManager.Params params;

    private Handler mMainHandler = null;

    protected Handler getMainHandler() {
        if (null == mMainHandler) {
            synchronized (this) {
                if (null == mMainHandler) {
                    mMainHandler = new android.os.Handler(Looper.getMainLooper());
                }
            }
        }
        return mMainHandler;
    }

    protected void runOnUiThread(Runnable runnable) {
        getMainHandler().post(runnable);
    }

    public AbsInteractiveStrategy(InteractiveModeManager.Params params) {
        this.params = params;
    }

    public InteractiveModeManager.Params getParams() {
        return params;
    }

    protected List<MD360Director> getDirectorList() {
        return params.projectionModeManager.getDirectors();
    }
}
