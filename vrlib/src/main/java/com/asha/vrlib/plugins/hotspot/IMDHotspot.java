package com.asha.vrlib.plugins.hotspot;

import com.asha.vrlib.model.MDHitPoint;
import com.asha.vrlib.model.MDRay;

/**
 * Created by hzqiujiadi on 16/8/5.
 * hzqiujiadi ashqalcn@gmail.com
 */
public interface IMDHotspot {
    MDHitPoint hit(MDRay ray);
    void onEyeHitIn(long timestamp);
    void onEyeHitOut();
    void onTouchHit(MDRay ray);
    String getTitle();
    String getTag();
    void rotateToCamera();
}
