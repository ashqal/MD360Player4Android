package com.asha.vrlib.model;

import android.animation.TimeInterpolator;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by hzqiujiadi on 2017/5/15.
 * hzqiujiadi ashqalcn@gmail.com
 */

public class MDFlingConfig {

    private TimeInterpolator mInterpolator = new DecelerateInterpolator();

    private long mDuring = 400;

    private float mSensitivity = 1.0f;


    public MDFlingConfig setInterpolator(TimeInterpolator i) {
        this.mInterpolator = i;
        return this;
    }

    /***
     * @param during in ms
     * */
    public MDFlingConfig setDuring(long during) {
        this.mDuring = during;
        return this;
    }

    /***
     * @param sensitivity default is 1.0f, 10.0 is faster than 0.1f
     * */
    public MDFlingConfig setSensitivity(float sensitivity) {
        this.mSensitivity = sensitivity;
        return this;
    }

    public TimeInterpolator getInterpolator() {
        return mInterpolator;
    }

    public long getDuring() {
        return mDuring;
    }

    public float getSensitivity() {
        return mSensitivity;
    }
}
