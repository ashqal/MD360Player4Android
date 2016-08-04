package com.asha.vrlib.model;

import android.opengl.Matrix;

/**
 * Created by hzqiujiadi on 16/8/3.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDPosition {

    public static final MDPosition sOriginalPosition = MDPosition.newInstance();

    private float[] mCurrentRotation = new float[16];
    private float mX;
    private float mY;
    private float mZ;
    private float mAngleX;
    private float mAngleY;
    private float mAngleZ;
    private float mPitch; // x-axis
    private float mYaw; // y-axis
    private float mRoll; // z-axis

    private MDPosition() {
        mX = mY = mZ = 0;
        mAngleX = mAngleY = mAngleZ = 0;
        mPitch = mYaw = mRoll = 0;
    }

    public float getPitch() {
        return mPitch;
    }

    public MDPosition setPitch(float pitch) {
        this.mPitch = pitch;
        return this;
    }

    public float getYaw() {
        return mYaw;
    }

    public MDPosition setYaw(float yaw) {
        this.mYaw = yaw;
        return this;
    }

    public float getRoll() {
        return mRoll;
    }

    public MDPosition setRoll(float roll) {
        this.mRoll = roll;
        return this;
    }

    public float getX() {
        return mX;
    }

    public MDPosition setX(float x) {
        this.mX = x;
        return this;
    }

    public float getY() {
        return mY;
    }

    public MDPosition setY(float y) {
        this.mY = y;
        return this;
    }

    public float getZ() {
        return mZ;
    }

    public MDPosition setZ(float z) {
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
    public MDPosition setAngleX(float angleX) {
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
    public MDPosition setAngleY(float angleY) {
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
    public MDPosition setAngleZ(float angleZ) {
        this.mAngleZ = angleZ;
        return this;
    }

    public static MDPosition newInstance(){
        return new MDPosition();
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
        Matrix.setIdentityM(mCurrentRotation, 0);

        Matrix.rotateM(mCurrentRotation, 0, getAngleY(), 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mCurrentRotation, 0, getAngleX(), 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mCurrentRotation, 0, getAngleZ(), 0.0f, 0.0f, 1.0f);

        Matrix.translateM(mCurrentRotation, 0, getX(),getY(),getZ());

        Matrix.rotateM(mCurrentRotation, 0, getYaw(), 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mCurrentRotation, 0, getPitch(), 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mCurrentRotation, 0, getRoll(), 0.0f, 0.0f, 1.0f);
    }

    public float[] getMatrix() {
        update();
        return mCurrentRotation;
    }
}
