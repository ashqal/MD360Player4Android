package com.asha.vrlib.texture;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Handler;
import android.os.Looper;

import com.asha.vrlib.MDVRLibrary;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.asha.vrlib.common.VRUtil.notNull;

/**
 * Created by hzqiujiadi on 16/4/5.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MD360BitmapTexture extends MD360Texture {

    private static final String TAG = "MD360BitmapTexture";
    private MDVRLibrary.IBitmapProvider mBitmapProvider;
    private Map<String,AsyncCallback> mCallbackList = new HashMap<>();
    private Handler mMainHandler;

    public MD360BitmapTexture(MDVRLibrary.IBitmapProvider bitmapProvider) {
        this.mBitmapProvider = bitmapProvider;
        this.mMainHandler = new Handler(Looper.myLooper());
    }

    @Override
    protected void onResize(int width, int height) {

    }

    @Override
    protected int createTextureId() {
        final int[] textureHandle = new int[1];
        GLES20.glGenTextures(1, textureHandle, 0);

        final int textureId = textureHandle[0];

        final AsyncCallback callback = new AsyncCallback();

        // save to thread local
        mCallbackList.put(Thread.currentThread().toString(),callback);

        // call the provider
        // to load the bitmap.
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                mBitmapProvider.onProvideBitmap(callback);
            }
        });

        return textureId;
    }

    @Override
    public void release() {
        super.release();
        Collection<AsyncCallback> callbacks = mCallbackList.values();
        for (AsyncCallback callback:callbacks){
            callback.releaseBitmap();
        }
        mCallbackList.clear();

        mMainHandler = null;
    }

    @Override
    synchronized public void syncDrawInContext(ISyncDrawCallback callback) {
        AsyncCallback asyncCallback = mCallbackList.get(Thread.currentThread().toString());
        if (asyncCallback != null && asyncCallback.hasBitmap()){
            Bitmap bitmap = asyncCallback.getBitmap();
            int textureId = getCurrentTextureId();
            textureInThread(textureId,bitmap);
            asyncCallback.releaseBitmap();
        }
        callback.onDrawOpenGL();
    }

    private void textureInThread(int textureId, Bitmap bitmap) {
        notNull(bitmap,"bitmap can't be null!");

        if (isEmpty(textureId)) return;

        // Bind to the texture in OpenGL
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
    }

    private static class AsyncCallback implements Callback{
        private Bitmap bitmap;

        @Override
        public void texture(Bitmap bitmap) {
            this.bitmap = bitmap.copy(bitmap.getConfig(),true);
        }

        public Bitmap getBitmap(){
            return bitmap;
        }

        public boolean hasBitmap(){
            return bitmap != null;
        }

        synchronized public void releaseBitmap(){
            if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
            bitmap = null;
        }
    }

    public interface Callback {
        void texture(Bitmap bitmap);
    }
}
