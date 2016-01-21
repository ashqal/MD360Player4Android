package com.asha.md360player4android.common;

import android.opengl.Matrix;

/**
 * Created by hzqiujiadi on 16/1/10.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class GLKMatrixUtil {
    public static float GLKMathDegreesToRadians(float degrees) {
        return (float) (degrees * (Math.PI / 180));
    }

    public static float GLKMathRadiansToDegrees(float angle) {
        return (float) (angle * 180 / Math.PI );
    }

    public static void GLKMatrix4MakePerspective(float fovyRadians, float aspect, float nearZ, float farZ, float[] out) {
        float cotan = (float) (1.0f / Math.tan(fovyRadians / 2.0f));
        out[0] = cotan / aspect;
        out[1] = 0.0f;
        out[2] = 0.0f;
        out[3] = 0.0f;

        out[4] = 0.0f;
        out[5] = cotan;
        out[6] = 0.0f;
        out[7] = 0.0f;

        out[8] = 0.0f;
        out[9] = 0.0f;
        out[10] = (farZ + nearZ) / (nearZ - farZ);
        out[11] = -1.0f;

        out[12] = 0.0f;
        out[13] = 0.0f;
        out[14] = (2.0f * farZ * nearZ) / (nearZ - farZ);
        out[15] = 0.0f;
    }

    public static void GLKMatrix4Identity(float[] out){
        out[0] = 1.0f;
        out[1] = 0.0f;
        out[2] = 0.0f;
        out[3] = 0.0f;

        out[4] = 0.0f;
        out[5] = 1.0f;
        out[6] = 0.0f;
        out[7] = 0.0f;

        out[8] = 0.0f;
        out[9] = 0.0f;
        out[10] = 1.0f;
        out[11] = 0.0f;

        out[12] = 0.0f;
        out[13] = 0.0f;
        out[14] = 0.0f;
        out[15] = 1.0f;
    }

    public static float[] GLKMatrix4Scale(float[] m, float sx, float sy, float sz){
        float[] a = {m[0] * sx,m[1] * sx, m[2] * sx, m[3] * sx,
                m[4] * sy, m[5] * sy, m[6] * sy, m[7] * sy,
                m[8] * sz, m[9] * sz, m[10] * sz, m[11] * sz,
                m[12], m[13], m[14], m[15]};
        return a;
    }

    public static void GLKMatrix4Multiply(float[] matrixLeft, float[] matrixRight, float[] out){
        Matrix.multiplyMM(out,0,matrixLeft,0,matrixRight,0);
    }

    public static void GLKMatrix4Rotate(float[] matrix, float radians, float x, float y, float z){
        Matrix.rotateM(matrix,0,GLKMathRadiansToDegrees(radians),x,y,z);
    }


    public static float[] getScreenScaleRatioRotation(float fTilt, float fScale, int iWidth, int iHeight) {
        final float[]   transform = new float[4];

        transform[0]    =   fTilt;
        transform[1]    =   1.0f*fScale;
        transform[2]    =   (float)iHeight/(float)iWidth*fScale;
        transform[3]    =   1.0f;

        return transform;
    }
}
