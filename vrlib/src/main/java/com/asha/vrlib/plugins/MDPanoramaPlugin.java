package com.asha.vrlib.plugins;

import android.content.Context;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MD360Program;
import com.asha.vrlib.MD360Renderer;
import com.asha.vrlib.MDVRLibrary;
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

    private MDPanoramaPlugin(Builder builder) {
        mTexture = builder.texture;
        mProgram = new MD360Program(builder.contentType);
        mProjectionModeManager = builder.projectionModeManager;
    }

    @Override
    public void init(Context context) {


        mProgram.build(context);
        mTexture.create();
    }

    @Override
    public void renderer(int width, int height, int index) {
        int i = index;

        MDAbsObject3D object3D = mProjectionModeManager.getObject3D();
        // check obj3d
        if (object3D == null) return;

        List<MD360Director> directors = mProjectionModeManager.getDirectors();
        // check director size
        if (i >= directors.size()) return;

        MD360Director director = directors.get(i);

        // Update Projection
        director.updateViewport(width, height);

        // Set our per-vertex lighting program.
        mProgram.use();
        glCheck("mProgram use");

        mTexture.updateTexture();

        object3D.markVerticesChanged();
        object3D.uploadVerticesBufferIfNeed(mProgram, i);

        object3D.markTexCoordinateChanged();
        object3D.uploadTexCoordinateBufferIfNeed(mProgram, i);

        // Pass in the combined matrix.
        director.shot(mProgram);
        object3D.draw();

    }

    @Override
    public void destroy() {
        if (mTexture != null){
            mTexture.destroy();
            mTexture.release();
            mTexture = null;
        }
    }

    public static class Builder{
        private MD360Texture texture;
        private int contentType = MDVRLibrary.ContentType.DEFAULT;
        private ProjectionModeManager projectionModeManager;

        public Builder() {
        }

        public MDPanoramaPlugin build(){
            return new MDPanoramaPlugin(this);
        }

        public Builder setContentType(int contentType) {
            this.contentType = contentType;
            return this;
        }

        /**
         * set surface{@link MD360Texture} to this render
         * @param texture {@link MD360Texture} surface may used by multiple render{@link MD360Renderer}
         * @return builder
         */
        public Builder setTexture(MD360Texture texture){
            this.texture = texture;
            return this;
        }

        public Builder setProjectionModeManager(ProjectionModeManager projectionModeManager) {
            this.projectionModeManager = projectionModeManager;
            return this;
        }
    }
}
