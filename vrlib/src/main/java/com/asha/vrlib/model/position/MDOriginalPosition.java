package com.asha.vrlib.model.position;

import com.asha.vrlib.common.GLUtil;
import com.asha.vrlib.model.MDPosition;

/**
 * Created by hzqiujiadi on 2017/4/11.
 * hzqiujiadi ashqalcn@gmail.com
 */

public class MDOriginalPosition extends MDPosition {

    @Override
    public float[] getMatrix() {
        return GLUtil.identityMatrix();
    }

    @Override
    public void setRotationMatrix(float[] rotation) {
        throw new RuntimeException("setRotationMatrix is not support in the OriginalPosition class");
    }
}
