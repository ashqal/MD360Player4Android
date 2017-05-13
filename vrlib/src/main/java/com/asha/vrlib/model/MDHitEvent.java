package com.asha.vrlib.model;

import com.asha.vrlib.plugins.hotspot.IMDHotspot;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by hzqiujiadi on 2017/4/20.
 * hzqiujiadi ashqalcn@gmail.com
 */

public class MDHitEvent {

    private IMDHotspot hotspot;

    private long timestamp;

    private MDRay ray;

    private MDHitPoint hitPoint;

    public IMDHotspot getHotspot() {
        return hotspot;
    }

    public void setHotspot(IMDHotspot hotspot) {
        this.hotspot = hotspot;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public MDRay getRay() {
        return ray;
    }

    public void setRay(MDRay ray) {
        this.ray = ray;
    }

    public MDHitPoint getHitPoint() {
        return hitPoint;
    }

    public void setHitPoint(MDHitPoint hitPoint) {
        this.hitPoint = hitPoint;
    }

    // pool

    public static MDHitEvent obtain(){
        MDHitEvent event = sPool.poll();
        if (event == null){
            event = new MDHitEvent();
        }
        return event;
    }

    public static void recycle(MDHitEvent event){
        event.hotspot = null;
        event.timestamp = 0;
        event.ray = null;
        event.hitPoint = null;
        sPool.add(event);
    }

    private static final Queue<MDHitEvent> sPool = new LinkedBlockingQueue<>();
}
