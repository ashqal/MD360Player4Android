package com.asha.vrlib.model.position;

import android.opengl.Matrix;

import com.asha.vrlib.model.MDPosition;

import static com.asha.vrlib.common.VRUtil.checkGLThread;
import static com.asha.vrlib.common.VRUtil.notNull;

/**
 * Created by hzqiujiadi on 2017/4/11.
 * hzqiujiadi ashqalcn@gmail.com
 */

public class MDMutablePosition extends MDPosition {

    private float[] mModelMatrix = null;
    private float[] mRotationMatrix = null;
    private final float[] mTmpMatrix = new float[16];

    private float mX;
    private float mY;
    private float mZ;
    private float mAngleX;
    private float mAngleY;
    private float mAngleZ;
    private float mPitch; // x-axis
    private float mYaw; // y-axis
    private float mRoll; // z-axis
    private boolean changed;

    private MDMutablePosition() {
        mX = mY = mZ = 0;
        mAngleX = mAngleY = mAngleZ = 0;
        mPitch = mYaw = mRoll = 0;
        changed = true;
    }

    public float getPitch() {
        return mPitch;
    }

    public MDMutablePosition setPitch(float pitch) {
        changed |= this.mPitch != pitch;
        this.mPitch = pitch;
        return this;
    }

    public float getYaw() {
        return mYaw;
    }

    public MDMutablePosition setYaw(float yaw) {
        changed |= this.mYaw != yaw;
        this.mYaw = yaw;
        return this;
    }

    public float getRoll() {
        return mRoll;
    }

    public MDMutablePosition setRoll(float roll) {
        changed |= this.mRoll != roll;
        this.mRoll = roll;
        return this;
    }

    public float getX() {
        return mX;
    }

    public MDMutablePosition setX(float x) {
        changed |= this.mX != x;
        this.mX = x;
        return this;
    }

    public float getY() {
        return mY;
    }

    public MDMutablePosition setY(float y) {
        changed |= this.mY != y;
        this.mY = y;
        return this;
    }

    public float getZ() {
        return mZ;
    }

    public MDMutablePosition setZ(float z) {
        changed |= this.mZ != z;
        this.mZ = z;
        return this;
    }

    public float getAngleX() {
        return mAngleX;
    }

    /**
     * setAngleX
     * @param angleX in degree
     * @return self
     */
    public MDMutablePosition setAngleX(float angleX) {
        changed |= this.mAngleX != angleX;
        this.mAngleX = angleX;
        return this;
    }

    public float getAngleY() {
        return mAngleY;
    }

    /**
     * setAngleY
     * @param angleY in degree
     * @return self
     */
    public MDMutablePosition setAngleY(float angleY) {
        changed |= this.mAngleY != angleY;
        this.mAngleY = angleY;
        return this;
    }

    public float getAngleZ() {
        return mAngleZ;
    }

    /**
     * setAngleZ
     * @param angleZ in degree
     * @return self
     */
    public MDMutablePosition setAngleZ(float angleZ) {
        changed |= this.mAngleX != angleZ;
        this.mAngleZ = angleZ;
        return this;
    }

    public static MDMutablePosition newInstance(){
        return new MDMutablePosition();
    }

    @Override
    public String toString() {
        return "MDPosition{" +
                "mX=" + mX +
                ", mY=" + mY +
                ", mZ=" + mZ +
                ", mAngleX=" + mAngleX +
                ", mAngleY=" + mAngleY +
                ", mAngleZ=" + mAngleZ +
                ", mPitch=" + mPitch +
                ", mYaw=" + mYaw +
                ", mRoll=" + mRoll +
                '}';
    }

    private void ensure(){
        // model
        if (mModelMatrix == null){
            mModelMatrix = new float[16];
            Matrix.setIdentityM(mModelMatrix, 0);
        }

        if (!changed){
            return;
        }

        Matrix.setIdentityM(mModelMatrix, 0);

        Matrix.rotateM(mModelMatrix, 0, getAngleX(), 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, getAngleY(), 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, getAngleZ(), 0.0f, 0.0f, 1.0f);

        Matrix.translateM(mModelMatrix, 0, getX(),getY(),getZ());

        Matrix.rotateM(mModelMatrix, 0, getYaw(), 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, getPitch(), 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, getRoll(), 0.0f, 0.0f, 1.0f);

        // rotation
        if (mRotationMatrix != null){
            Matrix.multiplyMM(mTmpMatrix, 0,  mRotationMatrix, 0, mModelMatrix, 0);
            System.arraycopy(mTmpMatrix, 0, mModelMatrix, 0, 16);
        }

        changed = false;
    }

    @Override
    public void setRotationMatrix(float[] rotationMatrix){
        notNull(rotationMatrix, "rotationMatrix can't be null!");
        checkGLThread("setRotationMatrix must called in gl thread!");

        if (mRotationMatrix == null){
            mRotationMatrix = new float[16];
        }

        System.arraycopy(rotationMatrix, 0, mRotationMatrix, 0, 16);
        changed = true;
    }

    @Override
    public float[] getMatrix() {
        ensure();
        return mModelMatrix;
    }
}
