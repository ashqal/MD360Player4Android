package com.asha.vrlib.plugins;

import android.content.Context;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.model.MDPosition;

/**
 * Created by hzqiujiadi on 16/7/21.
 * hzqiujiadi ashqalcn@gmail.com
 */
public abstract class MDAbsPlugin {

    abstract public void init(Context context);

    abstract public void renderer(int index, int width, int height, MD360Director director);

    abstract public void destroy();

    abstract protected MDPosition getModelPosition();
}
