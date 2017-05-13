package com.asha.vrlib.model;

import com.asha.vrlib.MD360Director;

/**
 * Created by hzqiujiadi on 2017/4/20.
 * hzqiujiadi ashqalcn@gmail.com
 */

public class MDDirectorSnapshot {

    private float[] viewMatrix = new float[16];
    private float[] projectionMatrix = new float[16];
    private float viewportWidth;
    private float viewportHeight;

    public float[] getViewMatrix() {
        return viewMatrix;
    }

    public float[] getProjectionMatrix() {
        return projectionMatrix;
    }

    public float getViewportWidth() {
        return viewportWidth;
    }

    public float getViewportHeight() {
        return viewportHeight;
    }

    public void copy(MD360Director director) {
        this.viewportWidth = director.getViewportWidth();
        this.viewportHeight = director.getViewportHeight();

        System.arraycopy(director.getViewMatrix(), 0, viewMatrix, 0, 16);
        System.arraycopy(director.getProjectionMatrix(), 0, projectionMatrix, 0, 16);
    }
}
