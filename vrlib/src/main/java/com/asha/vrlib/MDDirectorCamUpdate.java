package com.asha.vrlib;

/**
 * Created by hzqiujiadi on 2017/5/10.
 * hzqiujiadi ashqalcn@gmail.com
 */

public class MDDirectorCamUpdate {

    private MDDirectorCamera delegate = new MDDirectorCamera();

    public MDDirectorCamUpdate() {
        clear();
    }

    public MDDirectorCamUpdate setLookX(float mLookX) {
        delegate.setLookX(mLookX);
        return this;
    }

    public MDDirectorCamUpdate setLookY(float mLookY) {
        delegate.setLookY(mLookY);
        return this;
    }

    public MDDirectorCamUpdate setEyeX(float mEyeX) {
        delegate.setEyeX(mEyeX);
        return this;
    }

    public MDDirectorCamUpdate setEyeY(float mEyeY) {
        delegate.setEyeY(mEyeY);
        return this;
    }

    public MDDirectorCamUpdate setEyeZ(float mEyeZ) {
        delegate.setEyeZ(mEyeZ);
        return this;
    }

    public MDDirectorCamUpdate setNearScale(float scale) {
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

    public MDDirectorCamUpdate setRoll(float roll) {
        delegate.setRoll(roll);
        return this;
    }

    public MDDirectorCamUpdate setPitch(float pitch) {
        delegate.setPitch(pitch);
        return this;
    }

    public MDDirectorCamUpdate setYaw(float yaw) {
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

    public void copy(MDDirectorCamUpdate cameraUpdate) {
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
