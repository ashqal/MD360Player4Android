package com.asha.vrlib;

/**
 * Created by hzqiujiadi on 2017/5/10.
 * hzqiujiadi ashqalcn@gmail.com
 */

public class MD360CameraUpdate {

    private boolean changed = false;

    private MD360Camera delegate = new MD360Camera();

    public MD360CameraUpdate() {
        clear();
    }

    public boolean isChanged() {
        return changed;
    }

    public void consumeChanged(){
        changed = false;
    }

    public MD360CameraUpdate setLookX(float mLookX) {
        delegate.setLookX(mLookX);
        changed = true;
        return this;
    }

    public MD360CameraUpdate setLookY(float mLookY) {
        delegate.setLookY(mLookY);
        changed = true;
        return this;
    }

    public MD360CameraUpdate setEyeX(float mEyeX) {
        delegate.setEyeX(mEyeX);
        changed = true;
        return this;
    }

    public MD360CameraUpdate setEyeY(float mEyeY) {
        delegate.setEyeY(mEyeY);
        changed = true;
        return this;
    }

    public MD360CameraUpdate setEyeZ(float mEyeZ) {
        delegate.setEyeZ(mEyeZ);
        changed = true;
        return this;
    }

    public MD360CameraUpdate setNearScale(float scale) {
        delegate.setNearScale(scale);
        changed = true;
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

    public void clear(){
        setLookX(0);
        setLookY(0);
        setEyeX(0);
        setEyeY(0);
        setEyeZ(0);
        setNearScale(0);
    }

    public void copy(MD360CameraUpdate cameraUpdate) {
        setLookX(cameraUpdate.getLookX());
        setLookY(cameraUpdate.getLookY());
        setEyeX(cameraUpdate.getEyeX());
        setEyeY(cameraUpdate.getEyeY());
        setEyeZ(cameraUpdate.getEyeZ());
        setNearScale(cameraUpdate.getNearScale());
    }
}
