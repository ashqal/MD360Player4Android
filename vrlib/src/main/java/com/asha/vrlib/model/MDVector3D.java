package com.asha.vrlib.model;

/**
 * Created by hzqiujiadi on 16/8/5.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDVector3D {
    public float x;
    public float y;
    public float z;

    public MDVector3D setX(float x) {
        this.x = x;
        return this;
    }

    public MDVector3D setY(float y) {
        this.y = y;
        return this;
    }

    public MDVector3D setZ(float z) {
        this.z = z;
        return this;
    }

    @Override
    public String toString() {
        return "MDVector3D{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
