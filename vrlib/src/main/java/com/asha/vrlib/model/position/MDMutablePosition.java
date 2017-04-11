package com.asha.vrlib.model.position;

import android.opengl.Matrix;

import com.asha.vrlib.model.MDPosition;

/**
 * Created by hzqiujiadi on 2017/4/11.
 * hzqiujiadi ashqalcn@gmail.com
 */

public class MDMutablePosition extends MDPosition {

    private float[] mPositionMatrix = new float[16];

    private float mX;
    private float mY;
    private float mZ;
    private float mAngleX;
    private float mAngleY;
    private float mAngleZ;
    private float mPitch; // x-axis
    private float mYaw; // y-axis
    private float mRoll; // z-axis

    private MDMutablePosition() {
        mX = mY = mZ = 0;
        mAngleX = mAngleY = mAngleZ = 0;
        mPitch = mYaw = mRoll = 0;
    }

    public float getPitch() {
        return mPitch;
    }

    public MDMutablePosition setPitch(float pitch) {
        this.mPitch = pitch;
        return this;
    }

    public float getYaw() {
        return mYaw;
    }

    public MDMutablePosition setYaw(float yaw) {
        this.mYaw = yaw;
        return this;
    }

    public float getRoll() {
        return mRoll;
    }

    public MDMutablePosition setRoll(float roll) {
        this.mRoll = roll;
        return this;
    }

    public float getX() {
        return mX;
    }

    public MDMutablePosition setX(float x) {
        this.mX = x;
        return this;
    }

    public float getY() {
        return mY;
    }

    public MDMutablePosition setY(float y) {
        this.mY = y;
        return this;
    }

    public float getZ() {
        return mZ;
    }

    public MDMutablePosition setZ(float z) {
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

    private void update(){
        // model
        Matrix.setIdentityM(mPositionMatrix, 0);

        Matrix.rotateM(mPositionMatrix, 0, getAngleY(), 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mPositionMatrix, 0, getAngleX(), 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mPositionMatrix, 0, getAngleZ(), 0.0f, 0.0f, 1.0f);

        Matrix.translateM(mPositionMatrix, 0, getX(),getY(),getZ());

        Matrix.rotateM(mPositionMatrix, 0, getYaw(), 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mPositionMatrix, 0, getPitch(), 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mPositionMatrix, 0, getRoll(), 0.0f, 0.0f, 1.0f);
    }

    @Override
    public void setRotationMatrix(float[] rotationMatrix){
    }

    @Override
    public float[] getMatrix() {
        update();
        return mPositionMatrix;
    }
}
