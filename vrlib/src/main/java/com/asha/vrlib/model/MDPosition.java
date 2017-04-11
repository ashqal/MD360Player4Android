package com.asha.vrlib.model;

import com.asha.vrlib.model.position.MDMutablePosition;
import com.asha.vrlib.model.position.MDOriginalPosition;

/**
 * Created by hzqiujiadi on 16/8/3.
 * hzqiujiadi ashqalcn@gmail.com
 */
public abstract class MDPosition {

    private static final MDPosition sOriginalPosition = new MDOriginalPosition();

    public static MDPosition getOriginalPosition() {
        return sOriginalPosition;
    }

    public static MDMutablePosition newInstance() {
        return MDMutablePosition.newInstance();
    }

    // abstract
    public abstract float[] getMatrix();

    // abstract
    public abstract void setRotationMatrix(float[] rotation);

}
