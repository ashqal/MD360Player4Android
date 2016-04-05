package com.asha.vrlib.surface;

/**
 * Created by hzqiujiadi on 16/1/25.
 * hzqiujiadi ashqalcn@gmail.com
 *
 * copied from surfaceTexture
 * Created by nitro888 on 15. 4. 5..
 * https://github.com/Nitro888/NitroAction360
 */
public abstract class MD360Surface {
    private static final int SURFACE_TEXTURE_EMPTY = 0;
    private static final String TAG = "MD360Surface";
    private int mWidth;
    private int mHeight;
    private ThreadLocal<Integer> mLocalGLSurfaceTexture = new ThreadLocal<>();

    public MD360Surface() {
    }

    public void resize(int width,int height){
        boolean changed = false;
        if (mWidth == width && mHeight == height) changed = true;
        mWidth = width;
        mHeight = height;

        // resize the texture
        if (changed) onResize(mWidth,mHeight);
    }

    public void createSurface() {
        int glSurfaceTexture = createTextureId();
        if (glSurfaceTexture != SURFACE_TEXTURE_EMPTY)
            mLocalGLSurfaceTexture.set(glSurfaceTexture);
    }

    protected int getCurrentTextureId(){
        Integer value = mLocalGLSurfaceTexture.get();
        return value != null ? value : SURFACE_TEXTURE_EMPTY;
    }

    final protected boolean isEmpty(int textureId){
        return textureId == SURFACE_TEXTURE_EMPTY;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public void release() {}

    abstract protected void onResize(int width, int height);

    abstract protected int createTextureId();

    abstract public void syncDrawInContext(ISyncDrawCallback callback);

    public interface ISyncDrawCallback {
        void onDrawOpenGL();
    }
}
