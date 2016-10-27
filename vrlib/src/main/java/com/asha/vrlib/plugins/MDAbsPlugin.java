package com.asha.vrlib.plugins;

import android.content.Context;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.model.MDPosition;

/**
 * Created by hzqiujiadi on 16/7/21.
 * hzqiujiadi ashqalcn@gmail.com
 */
public abstract class MDAbsPlugin {

    private boolean mIsInit;

    private MDPosition mPosition = MDPosition.sOriginalPosition;

    public final void setup(Context context){
        if (!mIsInit){
            init(context);
            mIsInit = true;
        }
    }

    abstract protected void init(Context context);

    abstract public void beforeRenderer(int totalWidth, int totalHeight);

    abstract public void renderer(int index, int itemWidth, int itemHeight, MD360Director director);

    abstract public void destroy();

    protected MDPosition getModelPosition(){
        return mPosition;
    }

    public void setModelPosition(MDPosition position) {
        this.mPosition = position;
    }

    abstract protected boolean removable();

}
