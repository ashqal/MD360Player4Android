package com.asha.vrlib.plugins;

import android.content.Context;

/**
 * Created by hzqiujiadi on 16/8/7.
 * hzqiujiadi ashqalcn@gmail.com
 */
public abstract class MDAbsLinePipe {
    abstract public void takeOver(int totalWidth, int totalHeight, int size);
    abstract public void commit(int totalWidth, int totalHeight, int size);
    abstract protected void init(Context context);

    private boolean mIsInit;
    private long mTid;

    // MDPosition position = MDPosition.sOriginalPosition;

    public final void setup(Context context){
        long tid = Thread.currentThread().getId();
        if (mTid != tid) {
            mTid = tid;
            mIsInit = false;
        }

        if (!mIsInit){
            init(context);
            mIsInit = true;
        }
    }


}
