package com.asha.vrlib.strategy.projection;

import com.asha.vrlib.common.MDDirection;
import com.asha.vrlib.model.MDMainPluginBuilder;
import com.asha.vrlib.plugins.MDAbsPlugin;
import com.asha.vrlib.plugins.MDMultiFishEyePlugin;

/**
 * Created by hzqiujiadi on 16/7/29.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MultiFishEyeProjection extends SphereProjection {

    private float radius;
    private MDDirection direction;

    public MultiFishEyeProjection(float radius, MDDirection direction) {
        this.radius = radius;
        this.direction = direction;
    }

    @Override
    public MDAbsPlugin buildMainPlugin(MDMainPluginBuilder builder) {
        return new MDMultiFishEyePlugin(builder, radius, direction);
    }
}
