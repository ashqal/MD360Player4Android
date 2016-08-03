package com.asha.vrlib.strategy.projection;

import android.app.Activity;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MD360DirectorFactory;
import com.asha.vrlib.model.MDPosition;
import com.asha.vrlib.objects.MDAbsObject3D;
import com.asha.vrlib.objects.MDObject3DHelper;
import com.asha.vrlib.objects.MDStereoSphere3D;

/**
 * Created by hzqiujiadi on 16/6/26.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class StereoSphereProjection extends AbsProjectionStrategy {

    private static class FixedDirectorFactory extends MD360DirectorFactory{
        @Override
        public MD360Director createDirector(int index) {
            return MD360Director.builder().build();
        }
    }

    private MDAbsObject3D object3D;

    @Override
    public void on(Activity activity) {
        object3D = new MDStereoSphere3D();
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

    @Override
    public MDPosition getModelPosition() {
        return MDPosition.sOriginalPosition;
    }

    @Override
    protected MD360DirectorFactory hijackDirectorFactory() {
        return new FixedDirectorFactory();
    }
}
