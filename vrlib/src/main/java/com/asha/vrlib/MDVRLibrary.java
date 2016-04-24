package com.asha.vrlib;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import com.asha.vrlib.common.GLUtil;
import com.asha.vrlib.objects.MDAbsObject3D;
import com.asha.vrlib.objects.MDObject3DLoader;
import com.asha.vrlib.objects.MDSphere3D48;
import com.asha.vrlib.strategy.display.DisplayModeManager;
import com.asha.vrlib.strategy.interactive.InteractiveModeManager;
import com.asha.vrlib.texture.MD360BitmapTexture;
import com.asha.vrlib.texture.MD360Texture;
import com.asha.vrlib.texture.MD360VideoTexture;

import java.util.LinkedList;
import java.util.List;

import static com.asha.vrlib.common.VRUtil.notNull;

/**
 * Created by hzqiujiadi on 16/3/12.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDVRLibrary {

    private static final String TAG = "MDVRLibrary";

    // interactive mode
    public static final int INTERACTIVE_MODE_MOTION = 1;
    public static final int INTERACTIVE_MODE_TOUCH = 2;

    // display mode
    public static final int DISPLAY_MODE_NORMAL = 3;
    public static final int DISPLAY_MODE_GLASS = 4;

    // private int mDisplayMode = DISPLAY_MODE_NORMAL;
    private InteractiveModeManager mInteractiveModeManager;
    private DisplayModeManager mDisplayModeManager;

    private List<MD360Director> mDirectorList;
    private List<GLSurfaceView> mGLSurfaceViewList;
    private List<MD360Renderer> mRendererList;
    private MD360Texture mSurface;
    private MDStatusManager mMDStatusManager;
    private int mContentType;

    private MDVRLibrary(Builder builder) {
        mContentType = builder.contentType;
        mSurface = builder.texture;

        mDirectorList = new LinkedList<>();
        mGLSurfaceViewList = new LinkedList<>();
        mRendererList = new LinkedList<>();
        mMDStatusManager = new MDStatusManager();

        // init glSurfaceViews
        initWithGLSurfaceViewIds(builder.activity,builder.glSurfaceViewIds);

        // init mode manager
        mDisplayModeManager = new DisplayModeManager(builder.displayMode,mGLSurfaceViewList);
        mInteractiveModeManager = new InteractiveModeManager(builder.interactiveMode,mDirectorList);

        mDisplayModeManager.prepare(builder.activity, builder.notSupportCallback);
        mInteractiveModeManager.prepare(builder.activity, builder.notSupportCallback);

        mMDStatusManager.reset(mDisplayModeManager.getVisibleSize());

        MDObject3DLoader.loadObj(builder.activity, new MDSphere3D48(), new MDObject3DLoader.LoadComplete() {
            @Override
            public void onComplete(MDAbsObject3D object3D) {
                if (mRendererList == null) return;
                for (MD360Renderer renderer : mRendererList){
                    renderer.updateObject3D(MDAbsObject3D.duplicate(object3D));
                }
            }
        });
    }

    private void initWithGLSurfaceViewIds(Activity activity, int[] glSurfaceViewIds){
        for (int id:glSurfaceViewIds){
            GLSurfaceView glSurfaceView = (GLSurfaceView) activity.findViewById(id);
            initOpenGL(activity,glSurfaceView,mSurface);
        }
    }

    private void initOpenGL(Context context, GLSurfaceView glSurfaceView, MD360Texture texture) {
        if (GLUtil.supportsEs2(context)) {
            // Request an OpenGL ES 2.0 compatible context.
            int index = mDirectorList.size();
            glSurfaceView.setEGLContextClientVersion(2);
            MD360Director director = MD360DirectorFactory.createDirector(index);
            MD360Renderer renderer = MD360Renderer.with(context)
                    .setTexture(texture)
                    .setDirector(director)
                    .setContentType(mContentType)
                    .build();
            renderer.setStatus(mMDStatusManager.newChild());

            // Set the renderer to our demo renderer, defined below.
            glSurfaceView.setRenderer(renderer);

            mDirectorList.add(director);
            mGLSurfaceViewList.add(glSurfaceView);
            mRendererList.add(renderer);
        } else {
            glSurfaceView.setVisibility(View.GONE);
            Toast.makeText(context, "OpenGLES2 not supported.", Toast.LENGTH_SHORT).show();
        }
    }

    public void switchInteractiveMode(Activity activity) {
        mInteractiveModeManager.switchMode(activity);
    }

    public void switchDisplayMode(Activity activity){
        mDisplayModeManager.switchMode(activity);
        mMDStatusManager.reset(mDisplayModeManager.getVisibleSize());
    }

    public void onResume(Context context){
        mInteractiveModeManager.onResume(context);

        for (GLSurfaceView glSurfaceView:mGLSurfaceViewList){
            glSurfaceView.onResume();
        }
    }

    public void onPause(Context context){
        mInteractiveModeManager.onPause(context);

        for (GLSurfaceView glSurfaceView:mGLSurfaceViewList){
            glSurfaceView.onPause();
        }
    }

    public void onDestroy(){
        if (mSurface != null) mSurface.release();
    }

    /**
     * handle touch touch to rotate the model
     *
     * @param event
     * @return true if handled.
     */
    public boolean handleTouchEvent(MotionEvent event) {
        return mInteractiveModeManager.handleTouchEvent(event);
    }

    public int getInteractiveMode() {
        return mInteractiveModeManager.getMode();
    }

    public int getDisplayMode(){
        return mDisplayModeManager.getMode();
    }

    public interface IOnSurfaceReadyCallback {
        void onSurfaceReady(Surface surface);
    }

    public interface IBitmapProvider {
        void onProvideBitmap(MD360BitmapTexture.Callback callback);
    }

    public interface INotSupportCallback{
        void onNotSupport(int mode);
    }

    public static Builder with(Activity activity){
        return new Builder(activity);
    }

    public static class Builder {
        private int displayMode = DISPLAY_MODE_NORMAL;
        private int interactiveMode = INTERACTIVE_MODE_MOTION;
        private int[] glSurfaceViewIds;
        private Activity activity;
        private int contentType = ContentType.DEFAULT;
        private MD360Texture texture;
        private INotSupportCallback notSupportCallback;

        private Builder(Activity activity) {
            this.activity = activity;
        }

        public Builder displayMode(int displayMode){
            this.displayMode = displayMode;
            return this;
        }

        public Builder interactiveMode(int interactiveMode){
            this.interactiveMode = interactiveMode;
            return this;
        }

        public Builder ifNotSupport(INotSupportCallback callback){
            this.notSupportCallback = callback;
            return this;
        }

        /**
         * Deprecated since 1.1!
         * use {@link #video} instead.
         *
         * @param callback IOnSurfaceReadyCallback
         * @return builder
         */
        @Deprecated
        public Builder callback(IOnSurfaceReadyCallback callback){
            return video(callback);
        }

        public Builder video(IOnSurfaceReadyCallback callback){
            texture = new MD360VideoTexture(callback);
            contentType = ContentType.VIDEO;
            return this;
        }

        public Builder bitmap(IBitmapProvider bitmapProvider){
            notNull(bitmapProvider, "bitmap Provider can't be null!");
            texture = new MD360BitmapTexture(bitmapProvider);
            contentType = ContentType.BITMAP;
            return this;
        }

        public MDVRLibrary build(int... glSurfaceViewIds){
            notNull(texture,"You must call video/bitmap function in before build");
            this.glSurfaceViewIds = glSurfaceViewIds;
            return new MDVRLibrary(this);
        }
    }

    interface ContentType{
        int VIDEO = 0;
        int BITMAP = 1;
        int DEFAULT = VIDEO;
    }
}
