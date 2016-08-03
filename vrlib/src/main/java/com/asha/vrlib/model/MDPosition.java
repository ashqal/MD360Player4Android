package com.asha.vrlib.model;

/**
 * Created by hzqiujiadi on 16/8/3.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDPosition {

    public static final MDPosition sOriginalPosition = MDPosition.newInstance();

    private float mX;
    private float mY;
    private float mZ;
    private float mAngleX;
    private float mAngleY;
    private float mAngleZ;

    private MDPosition() {
        mX = 0;
        mY = 0;
        mZ = 0;
        mAngleX = 0;
        mAngleY = 0;
        mAngleZ = 0;
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
}
