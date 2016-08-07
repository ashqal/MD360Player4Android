package com.asha.vrlib.plugins;

import com.asha.vrlib.model.MDRay;

/**
 * Created by hzqiujiadi on 16/8/5.
 * hzqiujiadi ashqalcn@gmail.com
 */
public interface IMDHotspot {
    boolean hit(MDRay ray);
    void onEyeHit(long timestamp);
    void onTouchHit();
    String getTitle();
}
