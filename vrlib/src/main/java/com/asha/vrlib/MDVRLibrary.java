package com.asha.vrlib;

import android.app.Activity;
import android.content.Context;
import android.graphics.RectF;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import com.asha.vrlib.common.GLUtil;
import com.asha.vrlib.strategy.display.DisplayModeManager;
import com.asha.vrlib.strategy.interactive.InteractiveModeManager;
import com.asha.vrlib.strategy.projection.ProjectionModeManager;
import com.asha.vrlib.texture.MD360BitmapTexture;
import com.asha.vrlib.texture.MD360Texture;
import com.asha.vrlib.texture.MD360VideoTexture;
import com.google.android.apps.muzei.render.GLTextureView;

import java.util.List;

import static com.asha.vrlib.common.VRUtil.notNull;

/**
 * Created by hzqiujiadi on 16/3/12.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDVRLibrary {

    private static final String TAG = "MDVRLibrary";
    public static final int sMultiScreenSize = 2;

    // interactive mode
    public static final int INTERACTIVE_MODE_MOTION = 1;
    public static final int INTERACTIVE_MODE_TOUCH = 2;
    public static final int INTERACTIVE_MODE_MOTION_WITH_TOUCH = 3;

    // display mode
    public static final int DISPLAY_MODE_NORMAL = 101;
    public static final int DISPLAY_MODE_GLASS = 102;

    // projection mode
    public static final int PROJECTION_MODE_SPHERE = 201;
    public static final int PROJECTION_MODE_DOME180 = 202;
    public static final int PROJECTION_MODE_DOME230 = 203;
    public static final int PROJECTION_MODE_DOME180_UPPER = 204;
    public static final int PROJECTION_MODE_DOME230_UPPER = 205;
    public static final int PROJECTION_MODE_STEREO_SPHERE = 206;
    public static final int PROJECTION_MODE_PLANE_FIT = 207;
    public static final int PROJECTION_MODE_PLANE_CROP = 208;
    public static final int PROJECTION_MODE_PLANE_FULL = 209;

    // private int mDisplayMode = DISPLAY_MODE_NORMAL;
    private RectF mTextureSize = new RectF(0,0,1024,1024);
    private InteractiveModeManager mInteractiveModeManager;
    private DisplayModeManager mDisplayModeManager;
    private ProjectionModeManager mProjectionModeManager;

    private MDGLScreenWrapper mScreenWrapper;
    private MD360Texture mTexture;
    private MDTouchHelper mTouchHelper;

    // video or image
    private int mContentType;

    private MDVRLibrary(Builder builder) {
        mContentType = builder.contentType;
        mTexture = builder.texture;

        // init mode manager
        initModeManager(builder);

        // init glSurfaceViews
        initOpenGL(builder.activity, builder.screenWrapper, mTexture);

        mTouchHelper = new MDTouchHelper(builder.activity);
        mTouchHelper.setPinchEnabled(builder.pinchEnabled);
        // listener
        mTouchHelper.setGestureListener(builder.gestureListener);
        mTouchHelper.setAdvanceGestureListener(new IAdvanceGestureListener() {
            @Override
            public void onDrag(float distanceX, float distanceY) {
                mInteractiveModeManager.handleDrag((int) distanceX,(int) distanceY);
            }

            @Override
            public void onPinch(float scale) {
                List<MD360Director> directors = mProjectionModeManager.getDirectors();
                for (MD360Director director : directors){
                    director.updateProjectionNearScale(scale);
                }
            }

        });
    }

    private void initModeManager(Builder builder) {

        // init ProjectionModeManager
        ProjectionModeManager.Params projectionManagerParams = new ProjectionModeManager.Params();
        projectionManagerParams.textureSize = mTextureSize;
        projectionManagerParams.directorFactory = builder.directorFactory;
        mProjectionModeManager = new ProjectionModeManager(builder.projectionMode, projectionManagerParams);
        mProjectionModeManager.prepare(builder.activity, builder.notSupportCallback);

        // init DisplayModeManager
        mDisplayModeManager = new DisplayModeManager(builder.displayMode);
        mDisplayModeManager.prepare(builder.activity, builder.notSupportCallback);

        // init InteractiveModeManager
        InteractiveModeManager.Params interactiveManagerParams = new InteractiveModeManager.Params();
        interactiveManagerParams.projectionModeManager = mProjectionModeManager;
        interactiveManagerParams.mMotionDelay = builder.motionDelay;
        interactiveManagerParams.mSensorListener = builder.sensorListener;
        mInteractiveModeManager = new InteractiveModeManager(builder.interactiveMode,interactiveManagerParams);
        mInteractiveModeManager.prepare(builder.activity, builder.notSupportCallback);
    }

    private void initOpenGL(Context context, MDGLScreenWrapper screenWrapper, MD360Texture texture) {
        if (GLUtil.supportsEs2(context)) {
            screenWrapper.init(context);
            // Request an OpenGL ES 2.0 compatible context.

            MD360Renderer renderer = MD360Renderer.with(context)
                    .setTexture(texture)
                    .setDisplayModeManager(mDisplayModeManager)
                    .setProjectionModeManager(mProjectionModeManager)
                    .setContentType(mContentType)
                    .build();

            // Set the renderer to our demo renderer, defined below.
            screenWrapper.setRenderer(renderer);
            this.mScreenWrapper = screenWrapper;
        } else {
            this.mScreenWrapper.getView().setVisibility(View.GONE);
            Toast.makeText(context, "OpenGLES2 not supported.", Toast.LENGTH_SHORT).show();
        }
    }

    public void switchInteractiveMode(Activity activity) {
        mInteractiveModeManager.switchMode(activity);
    }

    /**
     * Switch Interactive Mode
     *
     * @param activity activity
     * @param mode mode
     *
     * {@link #INTERACTIVE_MODE_MOTION}
     * {@link #INTERACTIVE_MODE_TOUCH}
     * {@link #INTERACTIVE_MODE_MOTION_WITH_TOUCH}
     */
    public void switchInteractiveMode(Activity activity, int mode){
        mInteractiveModeManager.switchMode(activity,mode);
    }

    public void switchDisplayMode(Activity activity){
        mDisplayModeManager.switchMode(activity);
    }

    /**
     * Switch Display Mode
     *
     * @param activity activity
     * @param mode mode
     *
     * {@link #DISPLAY_MODE_GLASS}
     * {@link #DISPLAY_MODE_NORMAL}
     */
    public void switchDisplayMode(Activity activity, int mode){
        mDisplayModeManager.switchMode(activity,mode);
    }


    public void switchProjectionMode(Activity activity, int mode) {
        mProjectionModeManager.switchMode(activity, mode);
    }

    public void resetTouch(){
        List<MD360Director> directors = mProjectionModeManager.getDirectors();
        for (MD360Director director : directors){
            director.reset();
        }
    }

    public void resetPinch(){
        mTouchHelper.reset();
    }

    public void onTextureResize(float width, float height){
        mTextureSize.set(0,0,width,height);
    }

    public void onResume(Context context){
        mInteractiveModeManager.onResume(context);
        if (mScreenWrapper != null){
            mScreenWrapper.onResume();
        }
    }

    public void onPause(Context context){
        mInteractiveModeManager.onPause(context);

        if (mScreenWrapper != null){
            mScreenWrapper.onPause();
        }

        if (mTexture != null){
            mTexture.destroy();
        }

    }

    public void onDestroy(){
        if (mTexture != null){
            mTexture.destroy();
            mTexture.release();
            mTexture = null;
        }
    }

    /**
     * handle touch touch to rotate the model
     *
     * @param event
     * @return true if handled.
     */
    public boolean handleTouchEvent(MotionEvent event) {
        return mTouchHelper.handleTouchEvent(event);
    }

    public int getInteractiveMode() {
        return mInteractiveModeManager.getMode();
    }

    public int getDisplayMode(){
        return mDisplayModeManager.getMode();
    }

    public int getProjectionMode(){
        return mProjectionModeManager.getMode();
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

    public interface IGestureListener {
        void onClick(MotionEvent e);
    }

    interface IAdvanceGestureListener {
        void onDrag(float distanceX, float distanceY);
        void onPinch(float scale);
    }

    public static Builder with(Activity activity){
        return new Builder(activity);
    }

    /**
     *
     */
    public static class Builder {
        private int displayMode = DISPLAY_MODE_NORMAL;
        private int interactiveMode = INTERACTIVE_MODE_MOTION;
        private int projectionMode = PROJECTION_MODE_SPHERE;
        private Activity activity;
        private int contentType = ContentType.DEFAULT;
        private MD360Texture texture;
        private INotSupportCallback notSupportCallback;
        private IGestureListener gestureListener;
        private boolean pinchEnabled; // default false.
        public MD360DirectorFactory directorFactory;
        public int motionDelay = SensorManager.SENSOR_DELAY_GAME;
        public SensorEventListener sensorListener;
        public MDGLScreenWrapper screenWrapper;

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

        public Builder projectionMode(int projectionMode){
            this.projectionMode = projectionMode;
            return this;
        }

        public Builder ifNotSupport(INotSupportCallback callback){
            this.notSupportCallback = callback;
            return this;
        }

        /**
         * Deprecated since 1.1.0!
         * Will remove in 2.0.0!
         * Please use {@link #video} instead.
         *
         * @param callback IOnSurfaceReadyCallback
         * @return builder
         */
        @Deprecated
        public Builder callback(IOnSurfaceReadyCallback callback){
            return asVideo(callback);
        }

        /**
         * Deprecated since 1.5.0!
         * Will remove in 2.0.0!
         * Please use {@link #asVideo} instead.
         *
         * @param callback callback if the surface is created.
         * @return builder
         */
        @Deprecated
        public Builder video(IOnSurfaceReadyCallback callback){
            asVideo(callback);
            return this;
        }

        /**
         * Deprecated since 1.5.0!
         * Will remove in 2.0.0!
         * Please use {@link #asBitmap} instead.
         *
         * @param bitmapProvider provide the bitmap.
         * @return builder
         */
        @Deprecated
        public Builder bitmap(IBitmapProvider bitmapProvider){
            asBitmap(bitmapProvider);
            return this;
        }

        public Builder asVideo(IOnSurfaceReadyCallback callback){
            texture = new MD360VideoTexture(callback);
            contentType = ContentType.VIDEO;
            return this;
        }

        public Builder asBitmap(IBitmapProvider bitmapProvider){
            notNull(bitmapProvider, "bitmap Provider can't be null!");
            texture = new MD360BitmapTexture(bitmapProvider);
            contentType = ContentType.BITMAP;
            return this;
        }

        /**
         * gesture listener, e.g.
         * onClick
         *
         * @param listener listener
         * @return builder
         */
        public Builder gesture(IGestureListener listener) {
            gestureListener = listener;
            return this;
        }

        /**
         * enable or disable the pinch gesture
         *
         * @param enabled default is false
         * @return builder
         */
        public Builder pinchEnabled(boolean enabled) {
            this.pinchEnabled = enabled;
            return this;
        }


        /**
         * sensor delay in motion mode.
         *
         * {@link android.hardware.SensorManager#SENSOR_DELAY_FASTEST}
         * {@link android.hardware.SensorManager#SENSOR_DELAY_GAME}
         * {@link android.hardware.SensorManager#SENSOR_DELAY_NORMAL}
         * {@link android.hardware.SensorManager#SENSOR_DELAY_UI}
         *
         * @param motionDelay default is {@link android.hardware.SensorManager#SENSOR_DELAY_GAME}
         * @return builder
         */
        public Builder motionDelay(int motionDelay){
            this.motionDelay = motionDelay;
            return this;
        }

        public Builder sensorCallback(SensorEventListener callback){
            this.sensorListener = callback;
            return this;
        }

        public Builder directorFactory(MD360DirectorFactory directorFactory){
            this.directorFactory = directorFactory;
            return this;
        }

        /**
         * build it!
         *
         * @param glViewId will find the GLSurfaceView by glViewId in the giving {@link #activity}
         *                 or find the GLTextureView by glViewId
         * @return vr lib
         */
        public MDVRLibrary build(int glViewId){
            View view = activity.findViewById(glViewId);
            if (view instanceof GLSurfaceView){
                return build((GLSurfaceView) view);
            } else if(view instanceof GLTextureView){
                return build((GLTextureView) view);
            } else {
                throw new RuntimeException("Please ensure the glViewId is instance of GLSurfaceView or GLTextureView");
            }
        }

        public MDVRLibrary build(GLSurfaceView glSurfaceView){
            return build(MDGLScreenWrapper.wrap(glSurfaceView));
        }

        public MDVRLibrary build(GLTextureView glTextureView){
            return build(MDGLScreenWrapper.wrap(glTextureView));
        }

        private MDVRLibrary build(MDGLScreenWrapper screenWrapper){
            notNull(texture,"You must call video/bitmap function in before build");
            if (this.directorFactory == null) directorFactory = new MD360DirectorFactory.DefaultImpl();
            this.screenWrapper = screenWrapper;
            return new MDVRLibrary(this);
        }
    }

    interface ContentType{
        int VIDEO = 0;
        int BITMAP = 1;
        int DEFAULT = VIDEO;
    }
}
