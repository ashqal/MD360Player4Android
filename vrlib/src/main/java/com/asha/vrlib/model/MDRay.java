package com.asha.vrlib.model;

/**
 * Created by hzqiujiadi on 16/8/5.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDRay {
    private MDVector3D mOrig;
    private MDVector3D mDir;

    public MDRay(MDVector3D mOrig, MDVector3D mDir) {
        this.mOrig = mOrig;
        this.mDir = mDir;
    }

    public MDVector3D getOrig() {
        return mOrig;
    }

    public void setOrig(MDVector3D mOrig) {
        this.mOrig = mOrig;
    }

    public MDVector3D getDir() {
        return mDir;
    }

    public void setDir(MDVector3D mDir) {
        this.mDir = mDir;
    }

    @Override
    public String toString() {
        return "MDRay{" +
                ", mDir=" + mDir +
                ", mOrig=" + mOrig +
                '}';
    }
}
