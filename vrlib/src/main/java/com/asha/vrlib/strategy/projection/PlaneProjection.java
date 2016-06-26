package com.asha.vrlib.strategy.projection;

import android.app.Activity;
import android.graphics.RectF;

import com.asha.vrlib.objects.MDAbsObject3D;
import com.asha.vrlib.objects.MDObject3DHelper;
import com.asha.vrlib.objects.MDPlane;

/**
 * Created by hzqiujiadi on 16/6/26.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class PlaneProjection extends AbsProjectionStrategy {

    MDAbsObject3D object3D;

    private RectF mTextureSize;

    public PlaneProjection(RectF mTextureSize) {
        this.mTextureSize = mTextureSize;
    }

    @Override
    public void on(Activity activity) {
        object3D = new MDPlane(mTextureSize);
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
