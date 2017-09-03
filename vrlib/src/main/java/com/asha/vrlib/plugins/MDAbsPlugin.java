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

    private long mTid;

    private MDPosition mPosition = MDPosition.getOriginalPosition();

    public final void setupInGL(Context context){
        long tid = Thread.currentThread().getId();
        if (tid != mTid) {
            mTid = tid;
            mIsInit = false;
        }

        if (!mIsInit){
            initInGL(context);
            mIsInit = true;
        }
    }

    abstract protected void initInGL(Context context);

    abstract public void destroyInGL();

    abstract public void beforeRenderer(int totalWidth, int totalHeight);

    abstract public void renderer(int index, int itemWidth, int itemHeight, MD360Director director);

    protected MDPosition getModelPosition(){
        return mPosition;
    }

    public void setModelPosition(MDPosition position) {
        this.mPosition = position;
    }

    abstract protected boolean removable();

}
