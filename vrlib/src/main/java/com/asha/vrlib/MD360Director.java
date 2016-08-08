package com.asha.vrlib;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.asha.vrlib.model.MDPosition;

/**
 * Created by hzqiujiadi on 16/1/22.
 * hzqiujiadi ashqalcn@gmail.com
 *
 * response for model * view * projection
 */
public class MD360Director {

    private static final String TAG = "MD360Director";
    private static final float sNear = 0.7f;

    // private float[] mModelMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];

    private float[] mMVMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private float mEyeX = 0f;
    private float mEyeY = 0f;
    private float mEyeZ = 0f;
    private float mLookX = 0f;
    private float mLookY = 0f;
    private float mRatio = 0f;
    private float mNearScale = 0f;
    private int mViewportWidth = 2;
    private int mViewportHeight = 1;

    private float[] mCurrentRotation = new float[16];
    private float[] mSensorMatrix = new float[16];
    private float[] mTempMatrix = new float[16];

    private float mPreviousX;
    private float mPreviousY;

    private float mDeltaX;
    private float mDeltaY;

    private boolean mViewMatrixInvalidate = true;

    protected MD360Director(Builder builder) {
        this.mRatio = builder.mRatio;
        this.mNearScale = builder.mNearScale;
        this.mEyeX = builder.mEyeX;
        this.mEyeY = builder.mEyeY;
        this.mEyeZ = builder.mEyeZ;
        this.mLookX = builder.mLookX;
        this.mLookY = builder.mLookY;
        initCamera();
        initModel();
    }

    public float getDeltaY() {
        return mDeltaY;
    }

    public void setDeltaY(float mDeltaY) {
        this.mDeltaY = mDeltaY;
        mViewMatrixInvalidate = true;
    }

    public float getDeltaX() {
        return mDeltaX;
    }

    public void setDeltaX(float mDeltaX) {
        this.mDeltaX = mDeltaX;
        mViewMatrixInvalidate = true;
    }

    public float getPreviousY() {
        return mPreviousY;
    }

    public void setPreviousY(float mPreviousY) {
        this.mPreviousY = mPreviousY;
    }

    public float getPreviousX() {
        return mPreviousX;
    }

    public void setPreviousX(float mPreviousX) {
        this.mPreviousX = mPreviousX;
    }

    private void initCamera() {
        // View Matrix
        updateViewMatrix();
    }

    private void initModel(){
        Matrix.setIdentityM(mSensorMatrix, 0);
    }

    public void shot(MD360Program program) {
        shot(program, MDPosition.sOriginalPosition);
    }

    public void shot(MD360Program program, MDPosition modelPosition) {


        if (mViewMatrixInvalidate){
            updateViewMatrix();
            mViewMatrixInvalidate = false;
        }

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, modelPosition.getMatrix(), 0);

        // This multiplies the model view matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0);

        // Pass in the model view matrix
        GLES20.glUniformMatrix4fv(program.getMVMatrixHandle(), 1, false, mMVMatrix, 0);

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(program.getMVPMatrixHandle(), 1, false, mMVPMatrix, 0);
    }

    public void updateViewport(int width, int height){
        // Projection Matrix
        mViewportWidth = width;
        mViewportHeight = height;
        mRatio = width * 1.0f / height;
        updateProjection();
    }

    protected void updateProjectionNearScale(float scale){
        mNearScale = scale;
        updateProjection();
    }

    protected void updateProjection(){
        final float left = -mRatio/2;
        final float right = mRatio/2;
        final float bottom = -0.5f;
        final float top = 0.5f;
        final float far = 500;
        Matrix.frustumM(getProjectionMatrix(), 0, left, right, bottom, top, getNear(), far);
    }

    protected float getNear(){
        return mNearScale * sNear;
    }

    protected float getRatio(){
        return mRatio;
    }

    public float[] getProjectionMatrix(){
        return mProjectionMatrix;
    }

    public int getViewportWidth() {
        return mViewportWidth;
    }

    public int getViewportHeight() {
        return mViewportHeight;
    }

    public float[] getViewMatrix() {
        return mViewMatrix;
    }

    private void updateViewMatrix() {
        final float eyeX = mEyeX;
        final float eyeY = mEyeY;
        final float eyeZ = mEyeZ;
        final float lookX = mLookX;
        final float lookY = mLookY;
        final float lookZ = -1.0f;
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        Matrix.setIdentityM(mCurrentRotation, 0);
        Matrix.rotateM(mCurrentRotation, 0, -mDeltaY, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mCurrentRotation, 0, -mDeltaX, 0.0f, 1.0f, 0.0f);
        Matrix.multiplyMM(mTempMatrix, 0, mSensorMatrix, 0, mCurrentRotation, 0);
        System.arraycopy(mTempMatrix, 0, mCurrentRotation, 0, 16);

        Matrix.multiplyMM(mTempMatrix, 0, mViewMatrix, 0, mCurrentRotation, 0);
        System.arraycopy(mTempMatrix, 0, mViewMatrix, 0, 16);
    }

    public void updateSensorMatrix(float[] sensorMatrix) {
        System.arraycopy(sensorMatrix,0,mSensorMatrix,0,16);
        mViewMatrixInvalidate = true;
    }

    public void reset(){
        mDeltaX = mDeltaY = mPreviousX = mPreviousY = 0;
        Matrix.setIdentityM(mSensorMatrix,0);
        mViewMatrixInvalidate = true;
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {
        private float mEyeX = 0f;
        private float mEyeY = 0f;
        private float mEyeZ = 0f;
        private float mRatio = 1.5f;
        private float mNearScale = 1f;
        private float mLookX = 0f;
        private float mLookY = 0f;

        public Builder setLookX(float mLookX) {
            this.mLookX = mLookX;
            return this;
        }

        public Builder setLookY(float mLookY) {
            this.mLookY = mLookY;
            return this;
        }

        public Builder setEyeX(float mEyeX) {
            this.mEyeX = mEyeX;
            return this;
        }

        public Builder setEyeY(float mEyeY) {
            this.mEyeY = mEyeY;
            return this;
        }

        public Builder setEyeZ(float mEyeZ) {
            this.mEyeZ = mEyeZ;
            return this;
        }

        /**
         * Deprecated since 1.4.0, please use {@link #setAngleX(float)}
         * @param a
         * @return
         */
        @Deprecated
        public Builder setAngle(float a) {
            return setAngleX(a);
        }

        @Deprecated
        public Builder setAngleX(float mAngle) {
            throw new RuntimeException("please use MDPosition");
        }

        @Deprecated
        public Builder setAngleY(float mAngle) {
            throw new RuntimeException("please use MDPosition");
        }

        public Builder setRatio(float mRatio) {
            this.mRatio = mRatio;
            return this;
        }

        public Builder setNearScale(float scale) {
            this.mNearScale = scale;
            return this;
        }

        public MD360Director build(){
            return new MD360Director(this);
        }
    }
}
