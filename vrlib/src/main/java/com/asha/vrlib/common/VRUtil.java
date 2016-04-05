package com.asha.vrlib.common;

import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.view.Surface;

/**
 * Created by hzqiujiadi on 16/3/13.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class VRUtil {

    private static float[] mTmp = new float[16];

    public static void sensorRotationVector2Matrix(SensorEvent event, int rotation, float[] output) {
        float[] values = event.values;
        switch (rotation){
            case Surface.ROTATION_0:
            case Surface.ROTATION_180: /* Notice: not supported for ROTATION_180! */
                SensorManager.getRotationMatrixFromVector(output, values);
                break;
            case Surface.ROTATION_90:
                SensorManager.getRotationMatrixFromVector(mTmp, values);
                SensorManager.remapCoordinateSystem(mTmp, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, output);
                break;
            case Surface.ROTATION_270:
                SensorManager.getRotationMatrixFromVector(mTmp, values);
                SensorManager.remapCoordinateSystem(mTmp, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X, output);
                break;
        }
        Matrix.rotateM(output, 0, 90.0F, 1.0F, 0.0F, 0.0F);
    }

    public static void notNull(Object object, String error){
        if (object == null) throw new RuntimeException(error);
    }


}
