package com.asha.vrlib.model;

/**
 * Created by hzqiujiadi on 2017/4/20.
 * hzqiujiadi ashqalcn@gmail.com
 */

public class MDHitPoint {

    private static final MDHitPoint sPointNotHit = new NotHit();

    private static final float sNotHit = Float.MAX_VALUE;

    private float distance;

    private float u;

    private float v;

    public MDHitPoint() {
        asNotHit();
    }

    public void asNotHit(){
        this.distance = sNotHit;
    }

    public boolean isNotHit(){
        return this.distance == sNotHit;
    }

    public boolean nearThen(MDHitPoint other){
        return this.distance <= other.distance;
    }

    public float getU() {
        return u;
    }

    public float getV() {
        return v;
    }

    public void set(float t, float u, float v) {
        this.distance = t;
        this.u = u;
        this.v = v;
    }

    public static MDHitPoint min(MDHitPoint a, MDHitPoint b){
        return a.distance < b.distance ? a : b;
    }

    public static MDHitPoint notHit(){
        return sPointNotHit;
    }

    // not hit impl
    private static class NotHit extends MDHitPoint {

        @Override
        public void set(float t, float u, float v) {
            throw new RuntimeException("NotHit can't be set.");
        }
    }
}
