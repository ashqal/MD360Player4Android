package com.asha.vrlib;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import com.asha.vrlib.common.GLUtil;
import com.asha.vrlib.objects.MDAbsObject3D;
import com.asha.vrlib.objects.MDDome3D;
import com.asha.vrlib.objects.MDSphere3D;
import com.asha.vrlib.strategy.display.DisplayModeManager;
import com.asha.vrlib.strategy.interactive.InteractiveModeManager;
import com.asha.vrlib.strategy.projection.ProjectionModeManager;
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
    private static final int sMultiScreenSize = 2;

    // interactive mode
    public static final int INTERACTIVE_MODE_MOTION = 1;
    public static final int INTERACTIVE_MODE_TOUCH = 2;
    public static final int INTERACTIVE_MODE_MOTION_WITH_TOUCH = 3;

    // display mode
    public static final int DISPLAY_MODE_NORMAL = 101;
    public static final int DISPLAY_MODE_GLASS = 102;

    // projection mode
    public static final int PROJECTION_MODE_SPHERE = 201;

    // private int mDisplayMode = DISPLAY_MODE_NORMAL;
    private InteractiveModeManager mInteractiveModeManager;
    private DisplayModeManager mDisplayModeManager;
    private ProjectionModeManager mProjectionModeManager;

    private List<MD360Director> mDirectorList;
    private GLSurfaceView mGLSurfaceView;
    private MD360Texture mSurface;
    private MDTouchHelper mTouchHelper;
    private MD360DirectorFactory mDirectorFactory;

    // video or image
    private int mContentType;

    private MDVRLibrary(Builder builder) {
        mContentType = builder.contentType;
        mSurface = builder.texture;
        mDirectorFactory = builder.directorFactory;

        // init director
        initDirectorList();

        // init mode manager
        initModeManager(builder);

        // init glSurfaceViews
        initOpenGL(builder.activity, builder.glSurfaceView, mSurface);

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
                for (MD360Director director : mDirectorList){
                    director.updateProjectionNearScale(scale);
                }
            }

        });
    }

    private void initDirectorList() {
        mDirectorList = new LinkedList<>();
        for (int i = 0; i < sMultiScreenSize; i++){
            MD360Director director = mDirectorFactory.createDirector(i);
            mDirectorList.add(director);
        }
    }

    private void initModeManager(Builder builder) {
        // init DisplayModeManager
        mDisplayModeManager = new DisplayModeManager(builder.displayMode);
        mDisplayModeManager.prepare(builder.activity, builder.notSupportCallback);

        // init InteractiveModeManager
        InteractiveModeManager.Params interactiveManagerParams = new InteractiveModeManager.Params();
        interactiveManagerParams.mDirectorList = mDirectorList;
        interactiveManagerParams.mMotionDelay = builder.motionDelay;
        interactiveManagerParams.mSensorListener = builder.sensorListener;
        mInteractiveModeManager = new InteractiveModeManager(builder.interactiveMode,interactiveManagerParams);
        mInteractiveModeManager.prepare(builder.activity, builder.notSupportCallback);

        // init ProjectionModeManager
        mProjectionModeManager = new ProjectionModeManager(builder.projectionMode);
        mProjectionModeManager.prepare(builder.activity, builder.notSupportCallback);
    }

    private void initOpenGL(Context context, GLSurfaceView glSurfaceView, MD360Texture texture) {
        if (GLUtil.supportsEs2(context)) {
            // Request an OpenGL ES 2.0 compatible context.
            glSurfaceView.setEGLContextClientVersion(2);
            MD360Renderer renderer = MD360Renderer.with(context)
                    .setTexture(texture)
                    .setDirectors(mDirectorList)
                    .setDisplayModeManager(mDisplayModeManager)
                    .setProjectionModeManager(mProjectionModeManager)
                    .setContentType(mContentType)
                    .build();

            // Set the renderer to our demo renderer, defined below.
            glSurfaceView.setRenderer(renderer);
            mGLSurfaceView = glSurfaceView;
        } else {
            glSurfaceView.setVisibility(View.GONE);
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
        for (MD360Director director : mDirectorList){
            director.reset();
        }
    }

    public void resetPinch(){
        mTouchHelper.reset();
    }

    public void onResume(Context context){
        mInteractiveModeManager.onResume(context);
        mProjectionModeManager.onResume(context);
        if (mGLSurfaceView != null){
            mGLSurfaceView.onResume();
        }
    }

    public void onPause(Context context){
        mInteractiveModeManager.onPause(context);
        mProjectionModeManager.onPause(context);
        if (mGLSurfaceView != null){
            mGLSurfaceView.onPause();
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
        public GLSurfaceView glSurfaceView;
        public MDAbsObject3D object3D;

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

        public Builder displayAsDome(){
            object3D = new MDDome3D();
            return this;
        }

        public Builder displayAsSphere(){
            object3D = new MDSphere3D();
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

        public MDVRLibrary build(GLSurfaceView glSurfaceView){
            notNull(texture,"You must call video/bitmap function in before build");
            if (this.directorFactory == null) directorFactory = new MD360DirectorFactory.DefaultImpl();
            if (this.object3D == null) displayAsSphere();
            this.glSurfaceView = glSurfaceView;
            return new MDVRLibrary(this);
        }

        /**
         * build it!
         *
         * @param glSurfaceViewId will find the GLSurfaceView by glSurfaceViewId in the giving {@link #activity}
         * @return vr lib
         */
        public MDVRLibrary build(int glSurfaceViewId){
            this.glSurfaceView = (GLSurfaceView) activity.findViewById(glSurfaceViewId);
            return build(this.glSurfaceView);
        }

    }

    interface ContentType{
        int VIDEO = 0;
        int BITMAP = 1;
        int DEFAULT = VIDEO;
    }
}
