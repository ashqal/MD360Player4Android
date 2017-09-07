package com.asha.vrlib.model;

/**
 * Created by hzqiujiadi on 16/9/29.
 * hzqiujiadi ashqalcn@gmail.com
 */

public class MDPinchConfig {
    private float max = 5;
    private float min = 1;
    private float defaultValue = 1;
    private float mSensitivity = 1;

    public MDPinchConfig setMax(float max) {
        this.max = max;
        return this;
    }

    public MDPinchConfig setMin(float min) {
        this.min = min;
        return this;
    }

    public MDPinchConfig setDefaultValue(float defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public MDPinchConfig setSensitivity(float mSensitivity) {
        this.mSensitivity = mSensitivity;
        return this;
    }

    public float getSensitivity() {
        return mSensitivity;
    }

    public float getMax() {
        return max;
    }

    public float getMin() {
        return min;
    }

    public float getDefaultValue() {
        return defaultValue;
    }
}
