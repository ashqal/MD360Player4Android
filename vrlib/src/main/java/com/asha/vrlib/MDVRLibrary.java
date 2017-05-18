package com.asha.vrlib;

import android.app.Activity;
import android.content.Context;
import android.graphics.RectF;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import com.asha.vrlib.common.GLUtil;
import com.asha.vrlib.common.MDGLHandler;
import com.asha.vrlib.common.MDMainHandler;
import com.asha.vrlib.compact.CompactEyePickAdapter;
import com.asha.vrlib.compact.CompactTouchPickAdapter;
import com.asha.vrlib.model.BarrelDistortionConfig;
import com.asha.vrlib.model.MDDirectorBrief;
import com.asha.vrlib.model.MDHitEvent;
import com.asha.vrlib.model.MDMainPluginBuilder;
import com.asha.vrlib.model.MDPinchConfig;
import com.asha.vrlib.model.MDRay;
import com.asha.vrlib.plugins.hotspot.IMDHotspot;
import com.asha.vrlib.plugins.MDAbsPlugin;
import com.asha.vrlib.plugins.MDPluginManager;
import com.asha.vrlib.plugins.hotspot.MDAbsView;
import com.asha.vrlib.strategy.display.DisplayModeManager;
import com.asha.vrlib.strategy.interactive.InteractiveModeManager;
import com.asha.vrlib.strategy.projection.IMDProjectionFactory;
import com.asha.vrlib.strategy.projection.ProjectionModeManager;
import com.asha.vrlib.texture.MD360BitmapTexture;
import com.asha.vrlib.texture.MD360Texture;
import com.asha.vrlib.texture.MD360VideoTexture;
import com.google.android.apps.muzei.render.GLTextureView;

import java.util.Iterator;
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
    public static final int INTERACTIVE_MODE_CARDBORAD_MOTION = 4;
    public static final int INTERACTIVE_MODE_CARDBORAD_MOTION_WITH_TOUCH = 5;

    // display mode
    public static final int DISPLAY_MODE_NORMAL = 101;
    public static final int DISPLAY_MODE_GLASS = 102;

    // projection mode
    public static final int PROJECTION_MODE_SPHERE = 201;
    public static final int PROJECTION_MODE_DOME180 = 202;
    public static final int PROJECTION_MODE_DOME230 = 203;
    public static final int PROJECTION_MODE_DOME180_UPPER = 204;
    public static final int PROJECTION_MODE_DOME230_UPPER = 205;
    /**
     * @deprecated since 2.0.4
     * use {@link #PROJECTION_MODE_STEREO_SPHERE_VERTICAL}
     */
    @Deprecated public static final int PROJECTION_MODE_STEREO_SPHERE = 206;
    public static final int PROJECTION_MODE_PLANE_FIT = 207;
    public static final int PROJECTION_MODE_PLANE_CROP = 208;
    public static final int PROJECTION_MODE_PLANE_FULL = 209;
    public static final int PROJECTION_MODE_MULTI_FISH_EYE_HORIZONTAL = 210;
    public static final int PROJECTION_MODE_MULTI_FISH_EYE_VERTICAL = 211;
    public static final int PROJECTION_MODE_STEREO_SPHERE_HORIZONTAL = 212;
    public static final int PROJECTION_MODE_STEREO_SPHERE_VERTICAL = 213;

    private RectF mTextureSize = new RectF(0, 0, 1024, 1024);
    private InteractiveModeManager mInteractiveModeManager;
    private DisplayModeManager mDisplayModeManager;
    private ProjectionModeManager mProjectionModeManager;
    private MDPluginManager mPluginManager;
    private MDPickerManager mPickerManager;
    private MDGLScreenWrapper mScreenWrapper;
    private MDTouchHelper mTouchHelper;
    private MD360Texture mTexture;
    private MDGLHandler mGLHandler;
    private MDDirectorCamUpdate mDirectorCameraUpdate;
    private MDDirectorFilter mDirectorFilter;

    private MDVRLibrary(Builder builder) {

        // init main handler
        MDMainHandler.init();

        // init gl handler
        mGLHandler = new MDGLHandler();

        // init mode manager
        initModeManager(builder);

        // init plugin manager
        initPluginManager(builder);

        // init glSurfaceViews
        initOpenGL(builder.activity, builder.screenWrapper);

        mTexture = builder.texture;
        mTouchHelper = new MDTouchHelper(builder.activity);
        mTouchHelper.addClickListener(builder.gestureListener);
        mTouchHelper.setPinchEnabled(builder.pinchEnabled);
        final UpdatePinchRunnable updatePinchRunnable = new UpdatePinchRunnable();
        mTouchHelper.setAdvanceGestureListener(new IAdvanceGestureListener() {
            @Override
            public void onDrag(float distanceX, float distanceY) {
                mInteractiveModeManager.handleDrag((int) distanceX,(int) distanceY);
            }

            @Override
            public void onPinch(final float scale) {
                updatePinchRunnable.setScale(scale);
                mGLHandler.post(updatePinchRunnable);
            }
        });
        mTouchHelper.setPinchConfig(builder.pinchConfig);

        mScreenWrapper.getView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mTouchHelper.handleTouchEvent(event);
            }
        });

        // init picker manager
        initPickerManager(builder);

        // add plugin
        initPlugin();
    }

    private class UpdatePinchRunnable implements Runnable{
        private float scale;

        public void setScale(float scale) {
            this.scale = scale;
        }

        @Override
        public void run() {
            List<MD360Director> directors = mProjectionModeManager.getDirectors();
            for (MD360Director director : directors){
                director.setNearScale(scale);
            }
        }
    }

    private void initModeManager(Builder builder) {
        // init director camera update
        mDirectorCameraUpdate = new MDDirectorCamUpdate();

        // init director
        mDirectorFilter = new MDDirectorFilter();
        mDirectorFilter.setDelegate(builder.directorFilter);

        // init ProjectionModeManager
        ProjectionModeManager.Params projectionManagerParams = new ProjectionModeManager.Params();
        projectionManagerParams.textureSize = mTextureSize;
        projectionManagerParams.directorFactory = builder.directorFactory;
        projectionManagerParams.projectionFactory = builder.projectionFactory;
        projectionManagerParams.mainPluginBuilder = new MDMainPluginBuilder()
                .setCameraUpdate(mDirectorCameraUpdate)
                .setFilter(mDirectorFilter)
                .setContentType(builder.contentType)
                .setTexture(builder.texture);

        mProjectionModeManager = new ProjectionModeManager(builder.projectionMode, mGLHandler, projectionManagerParams);
        mProjectionModeManager.prepare(builder.activity, builder.notSupportCallback);

        // init DisplayModeManager
        mDisplayModeManager = new DisplayModeManager(builder.displayMode, mGLHandler);
        mDisplayModeManager.setBarrelDistortionConfig(builder.barrelDistortionConfig);
        mDisplayModeManager.setAntiDistortionEnabled(builder.barrelDistortionConfig.isDefaultEnabled());
        mDisplayModeManager.prepare(builder.activity, builder.notSupportCallback);

        // init InteractiveModeManager
        InteractiveModeManager.Params interactiveManagerParams = new InteractiveModeManager.Params();
        interactiveManagerParams.projectionModeManager = mProjectionModeManager;
        interactiveManagerParams.mMotionDelay = builder.motionDelay;
        interactiveManagerParams.mSensorListener = builder.sensorListener;
        mInteractiveModeManager = new InteractiveModeManager(builder.interactiveMode, mGLHandler, interactiveManagerParams);
        mInteractiveModeManager.prepare(builder.activity, builder.notSupportCallback);
    }

    private void initPluginManager(Builder builder) {
        mPluginManager = new MDPluginManager();
    }

    private void initPickerManager(Builder builder) {
        mPickerManager = MDPickerManager.with()
                .setPluginManager(mPluginManager)
                .setDisplayModeManager(mDisplayModeManager)
                .setProjectionModeManager(mProjectionModeManager)
                .build();
        setEyePickEnable(builder.eyePickEnabled);
        mPickerManager.setEyePickChangedListener(builder.eyePickChangedListener);
        mPickerManager.setTouchPickListener(builder.touchPickChangedListener);

        // listener
        mTouchHelper.addClickListener(mPickerManager.getTouchPicker());
    }

    private void initOpenGL(Context context, MDGLScreenWrapper screenWrapper) {
        if (GLUtil.supportsEs2(context)) {
            screenWrapper.init(context);
            // Request an OpenGL ES 2.0 compatible context.

            MD360Renderer renderer = MD360Renderer.with(context)
                    .setGLHandler(mGLHandler)
                    .setPluginManager(mPluginManager)
                    .setProjectionModeManager(mProjectionModeManager)
                    .setDisplayModeManager(mDisplayModeManager)
                    .build();

            // Set the renderer to our demo renderer, defined below.
            screenWrapper.setRenderer(renderer);
            this.mScreenWrapper = screenWrapper;
        } else {
            this.mScreenWrapper.getView().setVisibility(View.GONE);
            Toast.makeText(context, "OpenGLES2 not supported.", Toast.LENGTH_SHORT).show();
        }
    }

    private void initPlugin() {
        addPlugin(mProjectionModeManager.getDirectorUpdatePlugin());
        addPlugin(mPickerManager.getEyePicker());
    }

    public MDDirectorCamUpdate updateCamera(){
        return mDirectorCameraUpdate;
    }

    public MDDirectorBrief getDirectorBrief(){
        return mProjectionModeManager.getDirectorBrief();
    }

    public void switchInteractiveMode(final Activity activity) {
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
    public void switchInteractiveMode(final Activity activity, final int mode){
        mInteractiveModeManager.switchMode(activity, mode);
    }

    public void switchDisplayMode(final Activity activity){
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
    public void switchDisplayMode(final Activity activity, final int mode){
        mDisplayModeManager.switchMode(activity, mode);
    }

    /**
     * Switch Projection Mode
     *
     * @param activity activity
     * @param mode mode
     *
     * {@link #PROJECTION_MODE_SPHERE}
     * {@link #PROJECTION_MODE_DOME180}
     * and so on.
     */
    public void switchProjectionMode(final Activity activity, final int mode) {
        mProjectionModeManager.switchMode(activity, mode);
    }

    public void resetTouch(){
        mGLHandler.post(new Runnable() {
            @Override
            public void run() {
                List<MD360Director> directors = mProjectionModeManager.getDirectors();
                for (MD360Director director : directors){
                    director.reset();
                }
            }
        });
    }

    public void resetPinch(){
        mTouchHelper.reset();
    }

    public void resetEyePick(){
        mPickerManager.resetEyePick();
    }

    public void setAntiDistortionEnabled(boolean enabled){
        mDisplayModeManager.setAntiDistortionEnabled(enabled);
    }

    public boolean isAntiDistortionEnabled(){
        return mDisplayModeManager.isAntiDistortionEnabled();
    }

    public boolean isEyePickEnable() {
        return mPickerManager.isEyePickEnable();
    }

    public void setEyePickEnable(boolean eyePickEnable) {
        mPickerManager.setEyePickEnable(eyePickEnable);
    }

    @Deprecated
    public void setEyePickChangedListener(IEyePickListener listener){
        mPickerManager.setEyePickChangedListener(new CompactEyePickAdapter(listener));
    }

    @Deprecated
    public void setTouchPickListener(ITouchPickListener listener){
        mPickerManager.setTouchPickListener(new CompactTouchPickAdapter(listener));
    }

    public void setEyePickChangedListener(IEyePickListener2 listener){
        mPickerManager.setEyePickChangedListener(listener);
    }

    public void setTouchPickListener(ITouchPickListener2 listener){
        mPickerManager.setTouchPickListener(listener);
    }

    public void setPinchScale(float scale){
        mTouchHelper.scaleTo(scale);
    }

    public void setDirectorFilter(IDirectorFilter filter){
        mDirectorFilter.setDelegate(filter);
    }

    public void addPlugin(MDAbsPlugin plugin){
        mPluginManager.add(plugin);
    }

    public void removePlugin(MDAbsPlugin plugin){
        mPluginManager.remove(plugin);
    }

    public void removePlugins(){
        mPluginManager.removeAll();
    }

    public IMDHotspot findHotspotByTag(String tag){
        return mPluginManager.findHotspotByTag(tag);
    }

    public MDAbsView findViewByTag(String tag){
        return mPluginManager.findViewByTag(tag);
    }

    public void onTextureResize(float width, float height){
        mTextureSize.set(0,0,width,height);
    }


    public void onOrientationChanged(Activity activity) {
        mInteractiveModeManager.onOrientationChanged(activity);
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
    }

    public void onDestroy(){
        mGLHandler.post(new Runnable() {
            @Override
            public void run() {
                fireDestroy();
            }
        });
        mGLHandler.markAsDestroy();
    }

    private void fireDestroy(){
        Iterator<MDAbsPlugin> iterator = mPluginManager.getPlugins().iterator();
        while (iterator.hasNext()){
            MDAbsPlugin plugin = iterator.next();
            plugin.destroyInGL();
        }

        MDAbsPlugin mainPlugin = mProjectionModeManager.getMainPlugin();
        if (mainPlugin != null){
            mainPlugin.destroyInGL();
        }

        if (mTexture != null){
            mTexture.destroy();
            mTexture.release();
            mTexture = null;
        }
    }

    /**
     * handle touch touch to rotate the model
     * @deprecated deprecated since 2.0
     *
     * @param event
     * @return true if handled.
     */
    public boolean handleTouchEvent(MotionEvent event) {
        Log.e(TAG,"please remove the handleTouchEvent in activity!");
        return false;
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

    public void notifyPlayerChanged(){
        if (mTexture != null){
            mTexture.notifyChanged();
        }
    }

    public interface IOnSurfaceReadyCallback {
        void onSurfaceReady(Surface surface);
    }

    public interface IBitmapProvider {
        void onProvideBitmap(MD360BitmapTexture.Callback callback);
    }

    public interface IImageLoadProvider {
        void onProvideBitmap(Uri uri, MD360BitmapTexture.Callback callback);
    }

    public interface INotSupportCallback{
        void onNotSupport(int mode);
    }

    public interface IGestureListener {
        void onClick(MotionEvent e);
    }

    public interface IDirectorFilter {
        /**
         * @param input pitch(x-axis, from -90 to 90 in degree)
         * */
        float onFilterPitch(float input);
        /**
         * @param input yaw(y-axis, from -180 to 180 in degree)
         * */
        float onFilterYaw(float input);
        /**
         * @param input roll(z-axis, from -180 to 180 in degree)
         * */
        float onFilterRoll(float input);
    }

    public static class DirectorFilterAdatper implements IDirectorFilter {

        @Override
        public float onFilterPitch(float input) {
            return input;
        }

        @Override
        public float onFilterYaw(float input) {
            return input;
        }

        @Override
        public float onFilterRoll(float input) {
            return input;
        }
    }

    interface IAdvanceGestureListener {
        void onDrag(float distanceX, float distanceY);
        void onPinch(float scale);
    }

    @Deprecated
    public interface IEyePickListener {
        void onHotspotHit(IMDHotspot hitHotspot, long hitTimestamp);
    }

    public interface IEyePickListener2 {
        void onHotspotHit(MDHitEvent hitEvent);
    }

    @Deprecated
    public interface ITouchPickListener {
        void onHotspotHit(IMDHotspot hitHotspot, MDRay ray);
    }

    public interface ITouchPickListener2 {
        void onHotspotHit(MDHitEvent hitEvent);
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
        private boolean eyePickEnabled = true; // default true.
        private BarrelDistortionConfig barrelDistortionConfig;
        private IEyePickListener2 eyePickChangedListener;
        private ITouchPickListener2 touchPickChangedListener;
        private MD360DirectorFactory directorFactory;
        private int motionDelay = SensorManager.SENSOR_DELAY_GAME;
        private SensorEventListener sensorListener;
        private MDGLScreenWrapper screenWrapper;
        private IMDProjectionFactory projectionFactory;
        private MDPinchConfig pinchConfig;
        private IDirectorFilter directorFilter;

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
         * @deprecated please use {@link #listenGesture(IGestureListener)}
         *
         * @param listener listener
         * @return builder
         */
        @Deprecated
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
         * enable or disable the eye picking.
         *
         * @param enabled default is false
         * @return builder
         */
        public Builder eyePickEnabled(boolean enabled) {
            this.eyePickEnabled = enabled;
            return this;
        }

        /**
         * gesture listener, e.g.
         * onClick
         *
         * @param listener listener
         * @return builder
         */
        public Builder listenGesture(IGestureListener listener) {
            gestureListener = listener;
            return this;
        }

        /**
         * IPickListener listener
         *
         * @param listener listener
         * @return builder
         */
        @Deprecated
        public Builder listenEyePick(final IEyePickListener listener){
            this.eyePickChangedListener = new CompactEyePickAdapter(listener);
            return this;
        }

        /**
         * IPickListener listener
         *
         * @param listener listener
         * @return builder
         */
        @Deprecated
        public Builder listenTouchPick(final ITouchPickListener listener){
            this.touchPickChangedListener = new CompactTouchPickAdapter(listener);
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

        public Builder projectionFactory(IMDProjectionFactory projectionFactory){
            this.projectionFactory = projectionFactory;
            return this;
        }

        public Builder barrelDistortionConfig(BarrelDistortionConfig config){
            this.barrelDistortionConfig = config;
            return this;
        }

        public Builder pinchConfig(MDPinchConfig config){
            this.pinchConfig = config;
            return this;
        }

        public Builder directorFilter(IDirectorFilter filter){
            this.directorFilter = filter;
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
            notNull(texture,"You must call video/bitmap function before build");
            if (this.directorFactory == null) directorFactory = new MD360DirectorFactory.DefaultImpl();
            if (this.barrelDistortionConfig == null) barrelDistortionConfig = new BarrelDistortionConfig();
            if (this.pinchConfig == null) pinchConfig = new MDPinchConfig();
            this.screenWrapper = screenWrapper;
            return new MDVRLibrary(this);
        }
    }

    public interface ContentType {
        int VIDEO = 0;
        int BITMAP = 1;
        int FBO = 2;
        int DEFAULT = VIDEO;
    }

    public void resetDirectors(float density, float damping){
        List<MD360Director> directors = mProjectionModeManager.getDirectors();
        for (MD360Director director : directors){
            float x = director.getDeltaX();
            float y = director.getDeltaY();
            director.setDeltaX(x - x / density * damping);
            director.setDeltaY(y - y / density * damping);
        }
    }

    public void resetDirectors(){
        List<MD360Director> directors = mProjectionModeManager.getDirectors();
        for (MD360Director director : directors){
            director.setDeltaX(0);
            director.setDeltaY(0);
        }
    }
}
