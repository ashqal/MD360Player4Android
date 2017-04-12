package com.asha.vrlib.plugins.hotspot;

import android.content.Context;
import android.net.Uri;
import android.opengl.GLES20;
import android.util.SparseArray;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MD360Program;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.MDHotspotBuilder;
import com.asha.vrlib.model.MDPosition;
import com.asha.vrlib.model.MDRay;
import com.asha.vrlib.objects.MDAbsObject3D;
import com.asha.vrlib.plugins.MDAbsPlugin;
import com.asha.vrlib.texture.MD360BitmapTexture;
import com.asha.vrlib.texture.MD360Texture;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.asha.vrlib.common.GLUtil.glCheck;

/**
 * Created by hzqiujiadi on 2017/4/12.
 * hzqiujiadi ashqalcn@gmail.com
 */

public abstract class MDAbsHotspot extends MDAbsPlugin implements IMDHotspot {

    // plugin
    private SparseArray<Uri> uriList;

    private MDVRLibrary.IImageLoadProvider provider;

    MDAbsObject3D object3D;

    private MD360Program program;

    private MD360Texture texture;

    // hotspot
    private String title;

    private String tag;

    private int mPendingTextureKey = 0;

    private int mCurrentTextureKey = 0;

    private MDVRLibrary.ITouchPickListener clickListener;

    private AtomicBoolean mPendingRotateToCamera = new AtomicBoolean(false);

    public MDAbsHotspot(MDHotspotBuilder builder) {
        setTag(builder.tag);
        setTitle(builder.title);
        this.provider = builder.imageLoadProvider;
        this.uriList = builder.uriList;
        this.clickListener = builder.clickListener;
        setModelPosition(builder.position == null ? MDPosition.getOriginalPosition() : builder.position);
    }

    @Override
    protected void initInGL(Context context) {

        program = new MD360Program(MDVRLibrary.ContentType.BITMAP);
        program.build(context);

        texture = new MD360BitmapTexture(new MDVRLibrary.IBitmapProvider() {
            @Override
            public void onProvideBitmap(MD360BitmapTexture.Callback callback) {
                Uri uri = uriList.get(mCurrentTextureKey);
                if (uri != null){
                    provider.onProvideBitmap(uri, callback);
                }
            }
        });
        texture.create();
    }

    @Override
    public void destroyInGL() {

    }

    @Override
    public void beforeRenderer(int totalWidth, int totalHeight) {

    }

    @Override
    public void renderer(int index, int width, int height, MD360Director director) {
        if (texture == null){
            return;
        }

        if (mPendingTextureKey != mCurrentTextureKey){
            mCurrentTextureKey = mPendingTextureKey;
            texture.notifyChanged();
        }

        texture.texture(program);

        if (texture.isReady()){
            // Update Projection
            director.updateViewport(width, height);

            // Set our per-vertex lighting program.
            program.use();
            glCheck("MDSimplePlugin mProgram use");

            object3D.uploadVerticesBufferIfNeed(program, index);

            object3D.uploadTexCoordinateBufferIfNeed(program, index);

            // Pass in the combined matrix.
            director.beforeShot();
            consumePendingRotateToCamera(director);
            director.shot(program, getModelPosition());

            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);


            object3D.draw();
            GLES20.glDisable(GLES20.GL_BLEND);
        }

    }

    @Override
    protected boolean removable() {
        return true;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public void onEyeHitIn(long timestamp) {

    }

    @Override
    public void onEyeHitOut() {

    }

    @Override
    public void onTouchHit(MDRay ray) {
        if (clickListener != null){
            clickListener.onHotspotHit(this, ray);
        }
    }
    @Override
    public void useTexture(int key) {
        mPendingTextureKey = key;
    }

    @Override
    public void rotateToCamera(){
        mPendingRotateToCamera.set(true);
    }

    private void consumePendingRotateToCamera(MD360Director director) {
        if (mPendingRotateToCamera.get()){
            MDPosition position = getModelPosition();
            float[] rotation = director.getWorldRotationInvert();
            position.setRotationMatrix(rotation);
            mPendingRotateToCamera.set(false);
        }
    }
}
