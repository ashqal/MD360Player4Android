package com.asha.vrlib.plugins;

import android.content.Context;

import com.asha.vrlib.MDDirectorCamUpdate;
import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MD360Program;
import com.asha.vrlib.MDDirectorFilter;
import com.asha.vrlib.model.MDMainPluginBuilder;
import com.asha.vrlib.model.MDPosition;
import com.asha.vrlib.objects.MDAbsObject3D;
import com.asha.vrlib.strategy.projection.ProjectionModeManager;
import com.asha.vrlib.texture.MD360Texture;

import java.util.List;

import static com.asha.vrlib.common.GLUtil.glCheck;

/**
 * Created by hzqiujiadi on 16/7/22.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDPanoramaPlugin extends MDAbsPlugin {

    private MD360Program mProgram;

    private MD360Texture mTexture;

    private ProjectionModeManager mProjectionModeManager;

    private MDDirectorCamUpdate mDirectorCameraUpdate;

    private MDDirectorFilter mDirectorFilter;

    public MDPanoramaPlugin(MDMainPluginBuilder builder) {
        mTexture = builder.getTexture();
        mProgram = new MD360Program(builder.getContentType());
        mProjectionModeManager = builder.getProjectionModeManager();
        mDirectorCameraUpdate = builder.getCameraUpdate();
        mDirectorFilter = builder.getFilter();
    }

    @Override
    public void initInGL(Context context) {
        mProgram.build(context);
        mTexture.create();
    }

    @Override
    public void beforeRenderer(int totalWidth, int totalHeight) {
        List<MD360Director> directors = mProjectionModeManager.getDirectors();
        if (directors != null){
            // apply the update
            for (MD360Director director : directors){
                if (mDirectorCameraUpdate.isChanged()){
                    director.applyUpdate(mDirectorCameraUpdate);
                }

                director.applyFilter(mDirectorFilter);
            }

            mDirectorCameraUpdate.consumeChanged();
        }
    }

    @Override
    public void renderer(int index, int width, int height, MD360Director director) {

        MDAbsObject3D object3D = mProjectionModeManager.getObject3D();
        // check obj3d
        if (object3D == null) return;

        // Update Projection
        director.setViewport(width, height);

        // Set our per-vertex lighting program.
        mProgram.use();
        glCheck("MDPanoramaPlugin mProgram use");

        mTexture.texture(mProgram);

        object3D.uploadVerticesBufferIfNeed(mProgram, index);

        object3D.uploadTexCoordinateBufferIfNeed(mProgram, index);

        // Pass in the combined matrix.
        // model view projection matrix.
        director.beforeShot();
        director.shot(mProgram, getModelPosition());
        object3D.draw();

    }

    @Override
    public void destroyInGL() {
        mTexture = null;
    }

    @Override
    protected MDPosition getModelPosition() {
        return mProjectionModeManager.getModelPosition();
    }

    @Override
    protected boolean removable() {
        return false;
    }

}
