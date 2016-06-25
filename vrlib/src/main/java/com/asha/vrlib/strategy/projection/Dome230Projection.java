package com.asha.vrlib.strategy.projection;

import android.app.Activity;

import com.asha.vrlib.objects.MDAbsObject3D;
import com.asha.vrlib.objects.MDDome3D;
import com.asha.vrlib.objects.MDObject3DHelper;

/**
 * Created by hzqiujiadi on 16/6/25.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class Dome230Projection extends AbsProjectionStrategy {

    MDAbsObject3D object3D;

    @Override
    public void on(Activity activity) {
        object3D = new MDDome3D(230f);
        MDObject3DHelper.loadObj(activity, object3D);
    }

    @Override
    public void off(Activity activity) {

    }

    @Override
    public boolean isSupport(Activity activity) {
        return true;
    }

    @Override
    public MDAbsObject3D getObject3D() {
        return object3D;
    }
}
