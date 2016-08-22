package com.asha.vrlib.strategy.projection;

import com.asha.vrlib.model.MDMainPluginBuilder;
import com.asha.vrlib.plugins.MDAbsPlugin;
import com.asha.vrlib.plugins.MDMultiFishEyePlugin;

/**
 * Created by hzqiujiadi on 16/7/29.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MultiFishEyeProjection extends SphereProjection {

    private float radius;
    private boolean isHorizontal;

    public MultiFishEyeProjection(float radius, boolean isHorizontal) {
        this.radius = radius;
        this.isHorizontal = isHorizontal;
    }

    @Override
    public MDAbsPlugin buildMainPlugin(MDMainPluginBuilder builder) {
        return new MDMultiFishEyePlugin(builder, radius, isHorizontal);
    }
}
