package com.asha.vrlib.strategy.projection;

import android.app.Activity;
import android.graphics.RectF;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MD360DirectorFactory;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.MDPosition;
import com.asha.vrlib.objects.MDAbsObject3D;
import com.asha.vrlib.strategy.ModeManager;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by hzqiujiadi on 16/6/25.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class ProjectionModeManager extends ModeManager<AbsProjectionStrategy> implements IProjectionMode {

    public static int[] sModes = {MDVRLibrary.PROJECTION_MODE_SPHERE, MDVRLibrary.PROJECTION_MODE_DOME180, MDVRLibrary.PROJECTION_MODE_DOME230};

    public static class Params{
        public RectF textureSize;
        public MD360DirectorFactory directorFactory;
    }

    private List<MD360Director> mDirectors = new CopyOnWriteArrayList<>();

    private RectF mTextureSize;

    private MD360DirectorFactory mCustomDirectorFactory;

    public ProjectionModeManager(int mode, Params projectionManagerParams) {
        super(mode);
        this.mTextureSize = projectionManagerParams.textureSize;
        this.mCustomDirectorFactory = projectionManagerParams.directorFactory;
    }

    @Override
    public void switchMode(Activity activity, int mode) {
        super.switchMode(activity, mode);
    }

    @Override
    public void on(Activity activity) {
        super.on(activity);

        mDirectors.clear();

        MD360DirectorFactory factory = getStrategy().hijackDirectorFactory();
        factory = factory == null ? mCustomDirectorFactory : factory;

        for (int i = 0; i < MDVRLibrary.sMultiScreenSize; i++){
            mDirectors.add(factory.createDirector(i));
        }
    }

    @Override
    protected AbsProjectionStrategy createStrategy(int mode) {
        switch (mode){
            case MDVRLibrary.PROJECTION_MODE_DOME180:
                return new DomeProjection(this.mTextureSize,180f,false);
            case MDVRLibrary.PROJECTION_MODE_DOME230:
                return new DomeProjection(this.mTextureSize,230f,false);
            case MDVRLibrary.PROJECTION_MODE_DOME180_UPPER:
                return new DomeProjection(this.mTextureSize,180f,true);
            case MDVRLibrary.PROJECTION_MODE_DOME230_UPPER:
                return new DomeProjection(this.mTextureSize,230f,true);
            case MDVRLibrary.PROJECTION_MODE_STEREO_SPHERE:
                return new StereoSphereProjection();
            case MDVRLibrary.PROJECTION_MODE_PLANE_FIT:
            case MDVRLibrary.PROJECTION_MODE_PLANE_CROP:
            case MDVRLibrary.PROJECTION_MODE_PLANE_FULL:
                return PlaneProjection.create(mode,this.mTextureSize);
            case MDVRLibrary.PROJECTION_MODE_MULTI_FISHEYE:
                return new MultiFisheyeProjection();
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
    public MDPosition getModelPosition() {
        return getStrategy().getModelPosition();
    }

    @Override
    public MDAbsObject3D getObject3D() {
        return getStrategy().getObject3D();
    }

    public List<MD360Director> getDirectors() {
        return mDirectors;
    }
}
