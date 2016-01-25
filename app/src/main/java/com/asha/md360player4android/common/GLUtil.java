package com.asha.md360player4android.common;

import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

/**
 * Created by hzqiujiadi on 16/1/25.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class GLUtil {

    private static final String TAG = "GLUtil";

    public static void glCheck(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + GLUtils.getEGLErrorString(error));
        }
    }
}
