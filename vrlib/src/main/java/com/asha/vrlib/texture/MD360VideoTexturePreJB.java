package com.asha.vrlib.texture;

import android.graphics.SurfaceTexture;

import com.asha.vrlib.MDVRLibrary;

/**
 * Created by hzqiujiadi on 16/6/3.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MD360VideoTexturePreJB extends MD360VideoTexture {

    private ThreadLocal<Boolean> mLocalTextureWithSurface = new ThreadLocal<>();

    public MD360VideoTexturePreJB(MDVRLibrary.IOnSurfaceReadyCallback onSurfaceReadyListener) {
        super(onSurfaceReadyListener);
    }

    @Override
    protected void onSurfaceTextureCreated(SurfaceTexture mSurfaceTexture) {
        mLocalTextureWithSurface.set(true);
    }

    @Override
    protected void drawActually(ISyncDrawCallback callback, SurfaceTexture surfaceTexture) {
        int glSurfaceTexture = getCurrentTextureId();
        if (isEmpty(glSurfaceTexture)) return;
        Boolean textureWithSurface = mLocalTextureWithSurface.get() != null;
        if (textureWithSurface){
            surfaceTexture.updateTexImage();
            callback.onDrawOpenGL();
        }

    }
}
