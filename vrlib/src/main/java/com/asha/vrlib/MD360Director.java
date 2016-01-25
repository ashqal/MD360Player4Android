package com.asha.vrlib;

import android.opengl.GLES20;
import android.opengl.Matrix;

/**
 * Created by hzqiujiadi on 16/1/22.
 * hzqiujiadi ashqalcn@gmail.com
 *
 * response for model * view * projection
 */
public class MD360Director {

    private static final String TAG = "MD360Director";
    private float[] mModelMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];

    private float[] mMVMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private float mEyeZ = 12.5f;
    private float mAngle = 0;
    private float mRatio = 1.5f;
    private float mNear = 1.55f;

    public MD360Director() {

    }

    public void prepare(){
        initCamera();
        initModel();
    }

    private void initCamera() {
        // View Matrix
        updateCameraDistance(mEyeZ);
    }

    private void initModel(){
        // Model Matrix
        updateModelRotate(mAngle);
    }

    public void shot(MD360Program program) {
        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        // This multiplies the model view matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0);

        // Pass in the model view matrix
        GLES20.glUniformMatrix4fv(program.getMVMatrixHandle(), 1, false, mMVMatrix, 0);

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(program.getMVPMatrixHandle(), 1, false, mMVPMatrix, 0);
    }

    public void updateCameraDistance(float z) {
        mEyeZ = z;
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = mEyeZ;
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = 0.0f;
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
    }

    public void updateModelRotate(float a) {
        mAngle = a;
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setRotateM(mModelMatrix,0,a,0,1,0);
    }

    public void updateProjection(int width, int height){
        // Projection Matrix
        mRatio = width * 1.0f / height;
        updateProjectionNear(mNear);
    }

    public void updateProjectionNear(float near){
        mNear = near;
        final float left = -mRatio;
        final float right = mRatio;
        final float bottom = -0.5f;
        final float top = 0.5f;
        final float far = 500;
        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, mNear, far);
    }

    public float getEyeZ() {
        return mEyeZ;
    }

    public float getAngle() {
        return mAngle;
    }

    public float getNear() {
        return mNear;
    }

    @Override
    public String toString() {
        return "MD360Director{" +
                "eyeZ=" + mEyeZ +
                ", angle=" + mAngle +
                ", near=" + mNear +
                '}';
    }
}
