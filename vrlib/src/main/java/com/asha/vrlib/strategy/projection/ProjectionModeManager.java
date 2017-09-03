package com.asha.vrlib.strategy.projection;

import android.content.Context;
import android.graphics.RectF;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MD360DirectorFactory;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.common.MDDirection;
import com.asha.vrlib.common.MDGLHandler;
import com.asha.vrlib.model.MDDirectorBrief;
import com.asha.vrlib.model.MDMainPluginBuilder;
import com.asha.vrlib.model.MDPosition;
import com.asha.vrlib.objects.MDAbsObject3D;
import com.asha.vrlib.plugins.MDAbsPlugin;
import com.asha.vrlib.plugins.MDPluginAdapter;
import com.asha.vrlib.strategy.ModeManager;

import java.util.LinkedList;
import java.util.List;

import static com.asha.vrlib.common.VRUtil.checkGLThread;

/**
 * Created by hzqiujiadi on 16/6/25.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class ProjectionModeManager extends ModeManager<AbsProjectionStrategy> implements IProjectionMode {

    public static int[] sModes = {MDVRLibrary.PROJECTION_MODE_SPHERE, MDVRLibrary.PROJECTION_MODE_DOME180, MDVRLibrary.PROJECTION_MODE_DOME230};

    public static class Params{
        public RectF textureSize;
        public MD360DirectorFactory directorFactory;
        public MDMainPluginBuilder mainPluginBuilder;
        public IMDProjectionFactory projectionFactory;
    }

    private RectF mTextureSize;

    private MD360DirectorFactory mCustomDirectorFactory;

    private MDAbsPlugin mMainPlugin;

    private MDMainPluginBuilder mMainPluginBuilder;

    private IMDProjectionFactory mProjectionFactory;

    private final List<MD360Director> mDirectors;

    private final MDDirectorBrief mDirectorBrief;

    private final MDAbsPlugin mDirectorUpdatePlugin;

    public ProjectionModeManager(int mode, MDGLHandler handler, Params projectionManagerParams) {
        super(mode, handler);
        this.mDirectors = new LinkedList<>();
        this.mTextureSize = projectionManagerParams.textureSize;
        this.mCustomDirectorFactory = projectionManagerParams.directorFactory;
        this.mProjectionFactory = projectionManagerParams.projectionFactory;
        this.mMainPluginBuilder = projectionManagerParams.mainPluginBuilder;
        this.mMainPluginBuilder.setProjectionModeManager(this);
        this.mDirectorBrief = new MDDirectorBrief();
        this.mDirectorUpdatePlugin = new MDPluginAdapter(){
            @Override
            public void beforeRenderer(int totalWidth, int totalHeight) {
                if (mDirectors.size() > 0){
                    mDirectorBrief.make(mDirectors.get(0).getViewQuaternion());
                }
            }
        };
    }

    public MDAbsPlugin getMainPlugin() {
        if (mMainPlugin == null){
            mMainPlugin = getStrategy().buildMainPlugin(mMainPluginBuilder);
        }
        return mMainPlugin;
    }

    @Override
    public void switchMode(Context context, int mode) {
        super.switchMode(context, mode);
    }

    @Override
    public void on(Context context) {
        super.on(context);

        // destroy prev main plugin
        if( mMainPlugin != null){
            getGLHandler().post(new PluginDestroyTask(mMainPlugin));
            mMainPlugin = null;
        }

        mDirectors.clear();

        MD360DirectorFactory factory = getStrategy().hijackDirectorFactory();
        factory = factory == null ? mCustomDirectorFactory : factory;

        for (int i = 0; i < MDVRLibrary.sMultiScreenSize; i++){
            mDirectors.add(factory.createDirector(i));
        }
    }

    @Override
    protected AbsProjectionStrategy createStrategy(int mode) {
        if (mProjectionFactory != null){
            AbsProjectionStrategy strategy = mProjectionFactory.createStrategy(mode);
            if (strategy != null) return strategy;
        }
        
        switch (mode){
            case MDVRLibrary.PROJECTION_MODE_DOME180:
                return new DomeProjection(this.mTextureSize,180f,false);
            case MDVRLibrary.PROJECTION_MODE_DOME230:
                return new DomeProjection(this.mTextureSize,230f,false);
            case MDVRLibrary.PROJECTION_MODE_DOME180_UPPER:
                return new DomeProjection(this.mTextureSize,180f,true);
            case MDVRLibrary.PROJECTION_MODE_DOME230_UPPER:
                return new DomeProjection(this.mTextureSize,230f,true);
            case MDVRLibrary.PROJECTION_MODE_STEREO_SPHERE_HORIZONTAL:
                return new StereoSphereProjection(MDDirection.HORIZONTAL);
            case MDVRLibrary.PROJECTION_MODE_STEREO_SPHERE:
            case MDVRLibrary.PROJECTION_MODE_STEREO_SPHERE_VERTICAL:
                return new StereoSphereProjection(MDDirection.VERTICAL);
            case MDVRLibrary.PROJECTION_MODE_PLANE_FIT:
            case MDVRLibrary.PROJECTION_MODE_PLANE_CROP:
            case MDVRLibrary.PROJECTION_MODE_PLANE_FULL:
                return PlaneProjection.create(mode,this.mTextureSize);
            case MDVRLibrary.PROJECTION_MODE_MULTI_FISH_EYE_HORIZONTAL:
                return new MultiFishEyeProjection(1f, MDDirection.HORIZONTAL);
            case MDVRLibrary.PROJECTION_MODE_MULTI_FISH_EYE_VERTICAL:
                return new MultiFishEyeProjection(1f, MDDirection.VERTICAL);
            case MDVRLibrary.PROJECTION_MODE_CUBE:
                return new CubeProjection();
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

    public MDDirectorBrief getDirectorBrief() {
        return mDirectorBrief;
    }

    public MDAbsPlugin getDirectorUpdatePlugin() {
        return mDirectorUpdatePlugin;
    }

    private static class PluginDestroyTask implements Runnable {

        private MDAbsPlugin plugin;

        public PluginDestroyTask(MDAbsPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public void run() {
            checkGLThread("must call in gl thread");

            plugin.destroyInGL();
            plugin = null;
        }
    }
}
