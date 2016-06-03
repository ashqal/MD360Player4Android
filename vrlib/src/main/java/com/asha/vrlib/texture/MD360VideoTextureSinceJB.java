package com.asha.vrlib.texture;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.os.Build;

import com.asha.vrlib.MDVRLibrary;

/**
 * Created by hzqiujiadi on 16/6/3.
 * hzqiujiadi ashqalcn@gmail.com
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MD360VideoTextureSinceJB extends MD360VideoTexture {
    public MD360VideoTextureSinceJB(MDVRLibrary.IOnSurfaceReadyCallback onSurfaceReadyListener) {
        super(onSurfaceReadyListener);
    }

    @Override
    protected void onSurfaceTextureCreated(SurfaceTexture mSurfaceTexture) {
        mSurfaceTexture.detachFromGLContext();
    }

    @Override
    protected void drawActually(ISyncDrawCallback callback, SurfaceTexture surfaceTexture) {
        int glSurfaceTexture = getCurrentTextureId();
        if (isEmpty(glSurfaceTexture)) return;

        surfaceTexture.attachToGLContext(glSurfaceTexture);
        surfaceTexture.updateTexImage();
        callback.onDrawOpenGL();
        surfaceTexture.detachFromGLContext();
    }
}
