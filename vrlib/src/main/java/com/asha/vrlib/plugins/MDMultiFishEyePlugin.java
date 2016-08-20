package com.asha.vrlib.plugins;

import android.content.Context;
import android.opengl.GLES20;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MD360Program;
import com.asha.vrlib.model.MDMainPluginBuilder;
import com.asha.vrlib.model.MDPosition;
import com.asha.vrlib.objects.MDAbsObject3D;
import com.asha.vrlib.strategy.projection.ProjectionModeManager;
import com.asha.vrlib.texture.MD360Texture;

import static com.asha.vrlib.common.GLUtil.glCheck;

/**
 * Created by hzqiujiadi on 16/8/20.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDMultiFishEyePlugin extends MDAbsPlugin {

    private MD360Program mProgram;

    private MD360Texture mTexture;

    private ProjectionModeManager mProjectionModeManager;

    private MDMultiFisheyeConvertLinePipe linePipe = new MDMultiFisheyeConvertLinePipe();

    public MDMultiFishEyePlugin(MDMainPluginBuilder builder) {
        mTexture = builder.getTexture();
        mProgram = new MD360Program(builder.getContentType());
        mProjectionModeManager = builder.getProjectionModeManager();
    }

    @Override
    public void init(Context context) {
        mProgram.build(context);
        mTexture.create();

        linePipe.setup(context);
    }

    @Override
    public void beforeRenderer(int mWidth, int mHeight) {
        linePipe.takeOver(mWidth,mHeight,1);
        linePipe.draw(mProgram,mTexture,mWidth,mHeight);
        linePipe.commit(mWidth,mHeight,0);
    }

    @Override
    public void renderer(int index, int width, int height, MD360Director director) {


        MDAbsObject3D object3D = mProjectionModeManager.getObject3D();
        // check obj3d
        if (object3D == null) return;

        // Update Projection
        director.updateViewport(width, height);

        // Set our per-vertex lighting program.
        mProgram.use();
        glCheck("MDPanoramaPlugin mProgram use");

        // mTexture.texture(mProgram);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, linePipe.getTextureId());


        object3D.uploadVerticesBufferIfNeed(mProgram, index);

        object3D.uploadTexCoordinateBufferIfNeed(mProgram, index);

        // Pass in the combined matrix.
        director.shot(mProgram, getModelPosition());
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

    @Override
    protected MDPosition getModelPosition() {
        return mProjectionModeManager.getModelPosition();
    }

    @Override
    protected boolean removable() {
        return false;
    }

}
