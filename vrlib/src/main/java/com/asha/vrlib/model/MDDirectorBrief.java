package com.asha.vrlib.model;

/**
 * Created by hzqiujiadi on 2017/5/13.
 * hzqiujiadi ashqalcn@gmail.com
 */

public class MDDirectorBrief {

    private float pitch;
    private float yaw;
    private float roll;

    public void make(MDQuaternion quaternion){
        this.pitch = quaternion.getPitch();
        this.yaw = quaternion.getYaw();
        this.roll = quaternion.getRoll();
    }

    /**
     * getPitch
     * @return value in degree
     * */
    public float getPitch() {
        return pitch;
    }

    /**
     * getYaw
     * @return value in degree
     * */
    public float getYaw() {
        return yaw;
    }

    /**
     * getRoll
     * @return value in degree
     * */
    public float getRoll() {
        return roll;
    }

    @Override
    public String toString() {
        return "{" +
                "pitch=" + pitch +
                ", yaw=" + yaw +
                ", roll=" + roll +
                '}';
    }
}
