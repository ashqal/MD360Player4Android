package com.asha.vrlib.strategy.projection;

import android.app.Activity;

import com.asha.vrlib.model.MDPosition;
import com.asha.vrlib.objects.MDAbsObject3D;
import com.asha.vrlib.objects.MDObject3DHelper;
import com.asha.vrlib.objects.MDSphere3D;

/**
 * Created by hzqiujiadi on 16/6/25.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class SphereProjection extends AbsProjectionStrategy {

    private MDAbsObject3D object3D;

    public SphereProjection() {

    }

    @Override
    public MDAbsObject3D getObject3D() {
        return object3D;
    }

    @Override
    public MDPosition getModelPosition() {
        return MDPosition.sOriginalPosition;
    }

    @Override
    public void on(Activity activity) {
        object3D = new MDSphere3D();
        MDObject3DHelper.loadObj(activity, object3D);
    }

    @Override
    public void off(Activity activity) {
    }

    @Override
    public boolean isSupport(Activity activity) {
        return true;
    }
}
