package com.asha.vrlib.model;

import android.util.Log;

import com.asha.vrlib.common.GLUtil;
import com.asha.vrlib.model.position.MDMutablePosition;

import static android.content.ContentValues.TAG;

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

    private static class MDOriginalPosition extends MDPosition {

        @Override
        public float[] getMatrix() {
            return GLUtil.identityMatrix();
        }

        @Override
        public void setRotationMatrix(float[] rotation) {
            Log.e(TAG, "call setRotationMatrix to MDOriginalPosition");
        }
    }


}
