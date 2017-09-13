package com.asha.vrlib.texture;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.asha.vrlib.MD360Program;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.common.MDMainHandler;

import java.lang.ref.SoftReference;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.asha.vrlib.common.GLUtil.glCheck;
import static com.asha.vrlib.common.VRUtil.notNull;

public class MD360CubemapTexture extends MD360Texture {

    private static final String TAG = "MD360CubemapTexture";

    public static final int CUBE_FRONT = 0;
    public static final int CUBE_BACK = 1;
    public static final int CUBE_LEFT = 2;
    public static final int CUBE_RIGHT = 3;
    public static final int CUBE_TOP = 4;
    public static final int CUBE_BOTTOM = 5;

    private static final int[] CUBE_TARGETS = new int[] {
            GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z,
            GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z,
            GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
            GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X,
            GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
            GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
    };

    private MDVRLibrary.ICubemapProvider mCubemapProvider;
    private boolean mIsReady;
    private AsyncCallback mTmpAsyncCallback;
    private AtomicBoolean mTextureDirty = new AtomicBoolean(false);
    private static final int[] sIsSkybox = new int[]{1};

    private int currentFaceLoading = CUBE_FRONT;

    public MD360CubemapTexture(MDVRLibrary.ICubemapProvider cubemapProvider) {
        this.mCubemapProvider = cubemapProvider;
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
            mTextureDirty.set(false);
            currentFaceLoading = CUBE_FRONT;

            loadTexture();

            mIsReady = false;
        }


        AsyncCallback asyncCallback = mTmpAsyncCallback;
        int textureId = getCurrentTextureId();

        if (!mIsReady && asyncCallback != null) {

            if (asyncCallback.hasBitmap()){
                Bitmap bitmap = asyncCallback.getBitmap();
                Log.d(TAG, "Set texture "+currentFaceLoading);

                textureInThread(textureId, program, bitmap, currentFaceLoading);
                asyncCallback.releaseBitmap();

                currentFaceLoading++;
                if(currentFaceLoading < 6)
                    requestBitmap();
            }

            if(currentFaceLoading >= 6) {
                mIsReady = true;

                if(mCubemapProvider != null) {
                    MDMainHandler.sharedHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            mCubemapProvider.onReady();
                        }
                    });
                }
            }
        }

        if (isReady() && textureId != 0){
            // Bind texture
            // Set texture 0 as active texture
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            // Bind the cube map texture to the active opengl texture
            GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureId);
            // Set shader texture variable to texture 0
            GLES20.glUniform1i(program.getTextureUniformHandle(), 0);
            // Set shader isSkybox flag to true
            GLES20.glUniform1iv(program.getIsSkyboxHandle(), 1, sIsSkybox, 0);
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

        // create a new one
        mTmpAsyncCallback = new AsyncCallback(maxSize[0]);

        requestBitmap();
    }

    private void requestBitmap() {
        MDMainHandler.sharedHandler().post(new Runnable() {
            @Override
            public void run() {
                mCubemapProvider.onProvideCubemap(mTmpAsyncCallback, currentFaceLoading);
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

    private void textureInThread(int textureId, MD360Program program, Bitmap bitmap, int face) {
        notNull(bitmap, "bitmap can't be null!");

        if (isEmpty(textureId)) return;

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        glCheck("MD360BitmapTexture glActiveTexture");

        // Bind to the texture in OpenGL
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureId);
        glCheck("MD360BitmapTexture glBindTexture");

        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(CUBE_TARGETS[face], 0, bitmap, 0);
        glCheck("MD360BitmapTexture texImage2D");

        // Set shader texture variable to texture 0
        GLES20.glUniform1i(program.getTextureUniformHandle(), 0);
        glCheck("MD360BitmapTexture textureInThread");
    }

    // @todo this can be refactored as its repeated in @MD360BitmapTexture
    private static class AsyncCallback implements Callback {
        private SoftReference<Bitmap> bitmapRef;

        private int maxSize;

        public AsyncCallback(int maxSize) {
            this.maxSize = maxSize;
        }

        @Override
        public void texture(Bitmap bitmap) {
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
            if(bitmapRef != null) {
                bitmapRef.clear();
                bitmapRef = null;
            }
        }
    }

    public interface Callback {
        void texture(Bitmap bitmap);
        int getMaxTextureSize();
    }
}
