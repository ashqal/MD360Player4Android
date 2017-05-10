package com.asha.vrlib;

/**
 * Created by hzqiujiadi on 2017/5/10.
 * hzqiujiadi ashqalcn@gmail.com
 */

public class MD360CameraUpdate {

    private MD360Camera delegate = new MD360Camera();

    public MD360CameraUpdate() {
        clear();
    }

    public MD360CameraUpdate setLookX(float mLookX) {
        delegate.setLookX(mLookX);
        return this;
    }

    public MD360CameraUpdate setLookY(float mLookY) {
        delegate.setLookY(mLookY);
        return this;
    }

    public MD360CameraUpdate setEyeX(float mEyeX) {
        delegate.setEyeX(mEyeX);
        return this;
    }

    public MD360CameraUpdate setEyeY(float mEyeY) {
        delegate.setEyeY(mEyeY);
        return this;
    }

    public MD360CameraUpdate setEyeZ(float mEyeZ) {
        delegate.setEyeZ(mEyeZ);
        return this;
    }

    public MD360CameraUpdate setNearScale(float scale) {
        delegate.setNearScale(scale);
        return this;
    }

    public float getEyeX() {
        return delegate.getEyeX();
    }

    public float getEyeY() {
        return delegate.getEyeY();
    }

    public float getEyeZ() {
        return delegate.getEyeZ();
    }

    public float getLookX() {
        return delegate.getLookX();
    }

    public float getLookY() {
        return delegate.getLookY();
    }

    public float getNearScale() {
        return delegate.getNearScale();
    }

    public MD360CameraUpdate setRoll(float roll) {
        delegate.setRoll(roll);
        return this;
    }

    public MD360CameraUpdate setPitch(float pitch) {
        delegate.setPitch(pitch);
        return this;
    }

    public MD360CameraUpdate setYaw(float yaw) {
        delegate.setYaw(yaw);
        return this;
    }

    public float getPitch() {
        return delegate.getPitch();
    }

    public float getYaw() {
        return delegate.getYaw();
    }

    public float getRoll() {
        return delegate.getRoll();
    }

    public boolean isRotationValidate() {
        return delegate.isRotationValidate();
    }

    public boolean isPositionValidate() {
        return delegate.isPositionValidate();
    }

    public boolean isProjectionValidate() {
        return delegate.isProjectionValidate();
    }

    public void consumePositionValidate() {
        delegate.consumePositionValidate();
    }

    public void consumeProjectionValidate() {
        delegate.consumeProjectionValidate();
    }

    public void consumeRotationValidate() {
        delegate.consumeRotationValidate();
    }

    public void clear(){
        setLookX(0);
        setLookY(0);
        setEyeX(0);
        setEyeY(0);
        setEyeZ(0);
        setNearScale(0);
        setPitch(0);
        setYaw(0);
        setRoll(0);
    }

    public void copy(MD360CameraUpdate cameraUpdate) {
        setLookX(cameraUpdate.getLookX());
        setLookY(cameraUpdate.getLookY());
        setEyeX(cameraUpdate.getEyeX());
        setEyeY(cameraUpdate.getEyeY());
        setEyeZ(cameraUpdate.getEyeZ());
        setNearScale(cameraUpdate.getNearScale());
        setPitch(cameraUpdate.getPitch());
        setYaw(cameraUpdate.getYaw());
        setRoll(cameraUpdate.getRoll());
    }

    public boolean isChanged() {
        return isPositionValidate() || isRotationValidate() || isProjectionValidate();
    }

    public void consumeChanged() {
        consumePositionValidate();
        consumeRotationValidate();
        consumeProjectionValidate();
    }
}
