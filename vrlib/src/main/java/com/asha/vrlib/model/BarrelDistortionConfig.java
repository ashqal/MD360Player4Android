package com.asha.vrlib.model;

/**
 * Created by hzqiujiadi on 16/7/29.
 * hzqiujiadi ashqalcn@gmail.com
 *
 * http://mipav.cit.nih.gov/pubwiki/index.php/Barrel_Distortion_Correction
 */
public class BarrelDistortionConfig {
    private double paramA;
    private double paramB;
    private double paramC;
    private float scale;
    private boolean defaultEnabled;

    public BarrelDistortionConfig() {
        paramA = -0.068; // affects only the outermost pixels of the image
        paramB = 0.320000; // most cases only require b optimization
        paramC = -0.2; // most uniform correction
        scale = 0.95f;
        defaultEnabled = false;
    }

    public BarrelDistortionConfig setParamA(double paramA) {
        this.paramA = paramA;
        return this;
    }

    public BarrelDistortionConfig setParamB(double paramB) {
        this.paramB = paramB;
        return this;
    }

    public BarrelDistortionConfig setParamC(double paramC) {
        this.paramC = paramC;
        return this;
    }

    public BarrelDistortionConfig setScale(float scale) {
        this.scale = scale;
        return this;
    }

    public BarrelDistortionConfig setDefaultEnabled(boolean defaultEnabled) {
        this.defaultEnabled = defaultEnabled;
        return this;
    }

    public double getParamA() {
        return paramA;
    }

    public double getParamB() {
        return paramB;
    }

    public double getParamC() {
        return paramC;
    }

    public float getScale() {
        return scale;
    }

    public boolean isDefaultEnabled() {
        return defaultEnabled;
    }
}
