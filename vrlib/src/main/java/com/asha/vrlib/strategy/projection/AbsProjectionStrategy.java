package com.asha.vrlib.strategy.projection;

import android.content.Context;

import com.asha.vrlib.MD360DirectorFactory;
import com.asha.vrlib.strategy.IModeStrategy;

/**
 * Created by hzqiujiadi on 16/6/25.
 * hzqiujiadi ashqalcn@gmail.com
 */
public abstract class AbsProjectionStrategy implements IModeStrategy, IProjectionMode {

    @Override
    public void onResume(Context context) {

    }

    @Override
    public void onPause(Context context) {

    }

    protected MD360DirectorFactory hijackDirectorFactory(){ return null; }
}
