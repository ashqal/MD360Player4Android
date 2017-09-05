package com.asha.vrlib.texture;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.asha.vrlib.MD360Program;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.common.MDMainHandler;

import java.lang.ref.SoftReference;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.asha.vrlib.common.GLUtil.glCheck;
import static com.asha.vrlib.common.VRUtil.notNull;

/**
 * Created by hzqiujiadi on 16/4/5.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MD360BitmapTexture extends MD360Texture {

    private static final String TAG = "MD360BitmapTexture";
    private MDVRLibrary.IBitmapProvider mBitmapProvider;
    private boolean mIsReady;
    private AsyncCallback mTmpAsyncCallback;
    private AtomicBoolean mTextureDirty = new AtomicBoolean(false);

    public MD360BitmapTexture(MDVRLibrary.IBitmapProvider bitmapProvider) {
        this.mBitmapProvider = bitmapProvider;
    }

    @Override
    protected int createTextureId() {
        final int[] textureHandle = new int[1];
        GLES20.glGenTextures(1, textureHandle, 0);
        final int textureId = textureHandle[0];

        // call the provider
        // to load the bitmap.
        loadTexture();
        return textureId;
    }

    // gl thread
    @Override
    public boolean texture(MD360Program program) {
        if (mTextureDirty.get()){
            loadTexture();
            mTextureDirty.set(false);
        }


        AsyncCallback asyncCallback = mTmpAsyncCallback;
        int textureId = getCurrentTextureId();
        if (asyncCallback != null && asyncCallback.hasBitmap()){
            Bitmap bitmap = asyncCallback.getBitmap();
            textureInThread(textureId, program, bitmap);
            asyncCallback.releaseBitmap();
            mIsReady = true;
        }

        if (isReady() && textureId != 0){
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glUniform1i(program.getTextureUniformHandle(), 0);
        }
        return true;
    }

    @Override
    public void notifyChanged() {
        mTextureDirty.set(true);
    }

    // call from gl thread
    private void loadTexture(){
        // release the ref before
        if (mTmpAsyncCallback != null){
            mTmpAsyncCallback.releaseBitmap();
            mTmpAsyncCallback = null;
        }

        // get texture max size.
        int[] maxSize = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxSize, 0);

        final AsyncCallback finalCallback = new AsyncCallback(maxSize[0]);

        // create a new one
        mTmpAsyncCallback = finalCallback;

        MDMainHandler.sharedHandler().post(new Runnable() {
            @Override
            public void run() {
                mBitmapProvider.onProvideBitmap(finalCallback);
            }
        });
    }

    @Override
    public boolean isReady() {
        return mIsReady;
    }

    @Override
    public void destroy() {
        // release the ref before
        if (mTmpAsyncCallback != null){
            mTmpAsyncCallback.releaseBitmap();
            mTmpAsyncCallback = null;
        }
    }

    @Override
    public void release() {
    }

    private void textureInThread(int textureId, MD360Program program, Bitmap bitmap) {
        notNull(bitmap, "bitmap can't be null!");

        if (isEmpty(textureId)) return;

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        glCheck("MD360BitmapTexture glActiveTexture");

        // Bind to the texture in OpenGL
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        glCheck("MD360BitmapTexture glBindTexture");

        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        glCheck("MD360BitmapTexture texImage2D");

        GLES20.glUniform1i(program.getTextureUniformHandle(), 0);
        glCheck("MD360BitmapTexture textureInThread");
    }

    private static class AsyncCallback implements Callback {
        private SoftReference<Bitmap> bitmapRef;

        private int maxSize;

        public AsyncCallback(int maxSize) {
            this.maxSize = maxSize;
        }

        @Override
        public void texture(Bitmap bitmap) {
            releaseBitmap();
            this.bitmapRef = new SoftReference<>(bitmap);
        }

        @Override
        public int getMaxTextureSize() {
            return maxSize;
        }

        public Bitmap getBitmap(){
            return bitmapRef != null ? bitmapRef.get() : null;
        }

        public boolean hasBitmap(){
            return  bitmapRef != null && bitmapRef.get() != null;
        }

        public void releaseBitmap(){
            if (bitmapRef != null){
                bitmapRef.clear();
            }
            bitmapRef = null;
        }
    }

    public interface Callback {
        void texture(Bitmap bitmap);
        int getMaxTextureSize();
    }
}
