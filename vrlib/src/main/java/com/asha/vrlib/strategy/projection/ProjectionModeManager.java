package com.asha.vrlib.strategy.projection;

import android.app.Activity;

import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.objects.MDAbsObject3D;
import com.asha.vrlib.strategy.ModeManager;

/**
 * Created by hzqiujiadi on 16/6/25.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class ProjectionModeManager extends ModeManager<AbsProjectionStrategy> implements IProjectionMode {

    public static int[] sModes = {MDVRLibrary.PROJECTION_MODE_SPHERE, MDVRLibrary.PROJECTION_MODE_DOME180, MDVRLibrary.PROJECTION_MODE_DOME230};

    public ProjectionModeManager(int mode) {
        super(mode);
    }

    @Override
    public void switchMode(Activity activity) {

    }

    @Override
    protected AbsProjectionStrategy createStrategy(int mode) {
        switch (mode){
            case MDVRLibrary.PROJECTION_MODE_DOME180:
                return new DomeProjection(180f,false);
            case MDVRLibrary.PROJECTION_MODE_DOME230:
                return new DomeProjection(230f,false);
            case MDVRLibrary.PROJECTION_MODE_DOME180_UPPER:
                return new DomeProjection(180f,true);
            case MDVRLibrary.PROJECTION_MODE_DOME230_UPPER:
                return new DomeProjection(230f,true);
            case MDVRLibrary.PROJECTION_MODE_SPHERE:
            default:
                return new SphereProjection();
        }
    }

    @Override
    protected int[] getModes() {
        return sModes;
    }

    @Override
    public MDAbsObject3D getObject3D() {
        return getStrategy().getObject3D();
    }
}
