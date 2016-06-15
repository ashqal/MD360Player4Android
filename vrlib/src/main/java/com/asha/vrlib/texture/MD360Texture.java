package com.asha.vrlib.texture;

/**
 * Created by hzqiujiadi on 16/1/25.
 * hzqiujiadi ashqalcn@gmail.com
 *
 * copied from surfaceTexture
 * Created by nitro888 on 15. 4. 5..
 * https://github.com/Nitro888/NitroAction360
 */
public abstract class MD360Texture {
    private static final int TEXTURE_EMPTY = 0;
    private static final String TAG = "MD360Texture";
    private int mWidth;
    private int mHeight;
    private ThreadLocal<Integer> mLocalGLTexture = new ThreadLocal<>();

    public MD360Texture() {
    }

    // may called from multi thread
    public synchronized void resize(int width,int height){
        boolean changed = false;
        if (mWidth == width && mHeight == height) changed = true;
        mWidth = width;
        mHeight = height;

        // resize the texture
        if (changed) onResize(mWidth,mHeight);
    }

    // may called from multi thread
    public void create() {
        int glTexture = createTextureId();

        if (glTexture != TEXTURE_EMPTY){
            mLocalGLTexture.set(glTexture);
        }
    }


    public void release() {}

    protected int getCurrentTextureId(){
        Integer value = mLocalGLTexture.get();
        return value != null ? value : TEXTURE_EMPTY;
    }

    final protected boolean isEmpty(int textureId){
        return textureId == TEXTURE_EMPTY;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    abstract protected void onResize(int width, int height);

    abstract protected int createTextureId();

    abstract public boolean updateTexture();
}
