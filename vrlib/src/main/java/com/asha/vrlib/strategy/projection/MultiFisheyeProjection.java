package com.asha.vrlib.strategy.projection;

import android.app.Activity;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MD360DirectorFactory;
import com.asha.vrlib.model.MDPosition;
import com.asha.vrlib.objects.MDAbsObject3D;
import com.asha.vrlib.objects.MDMultiFisheye3D;
import com.asha.vrlib.objects.MDObject3DHelper;

/**
 * Created by hzqiujiadi on 16/7/29.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MultiFisheyeProjection extends AbsProjectionStrategy {

    private MDAbsObject3D object3D;

    private MDPosition position;

    @Override
    public void on(Activity activity) {
        object3D = new MDMultiFisheye3D();
        MDObject3DHelper.loadObj(activity, object3D);
    }

    @Override
    public void off(Activity activity) {

    }

    @Override
    protected MD360DirectorFactory hijackDirectorFactory() {
        return new MD360DirectorFactory() {
            @Override
            public MD360Director createDirector(int index) {
                return MD360Director.builder().build();
            }
        };
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
        if (position == null){
            position = MDPosition.newInstance().setAngleZ(-90);
        }
        return position;
    }
}
