package com.asha.vrlib;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import com.asha.vrlib.common.GLUtil;
import com.asha.vrlib.common.VRUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by hzqiujiadi on 16/3/12.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDVRLibrary implements SensorEventListener {

    private static final String TAG = "MDVRLibrary";

    // interactive mode
    public static final int INTERACTIVE_MODE_MOTION = 1;
    public static final int INTERACTIVE_MODE_TOUCH = 2;

    // display mode
    public static final int DISPLAY_MODE_NORMAL = 1;
    public static final int DISPLAY_MODE_GLASS = 2;

    private int mInteractiveMode = INTERACTIVE_MODE_MOTION;
    private int mDisplayMode = DISPLAY_MODE_NORMAL;

    private float[] mSensorMatrix = new float[16];
    private boolean isResumed = false;
    private List<MD360Director> mDirectorList;
    private List<GLSurfaceView> mGLSurfaceViewList;
    private MD360Surface mSurface;
    private int mDeviceRotation;
    private StatusManager mStatusManager;

    private MDVRLibrary(Builder builder) {
        mDirectorList = new LinkedList<>();
        mGLSurfaceViewList = new LinkedList<>();
        mSurface = new MD360Surface(builder.callback);
        mStatusManager  = new StatusManager();
        mDisplayMode = builder.displayMode;
        mInteractiveMode = builder.interactiveMode;
    }

    public void initWithGLSurfaceViewIds(Activity activity, int... glSurfaceViewIds){
        mDeviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        for (int id:glSurfaceViewIds){
            GLSurfaceView glSurfaceView = (GLSurfaceView) activity.findViewById(id);
            initOpenGL(activity,glSurfaceView,mSurface);
        }

        // sync the visible
        syncDisplayMode();
    }

    private void initOpenGL(Context context, GLSurfaceView glSurfaceView, MD360Surface surface) {
        if (GLUtil.supportsEs2(context)) {
            // Request an OpenGL ES 2.0 compatible context.
            int index = mDirectorList.size();
            glSurfaceView.setEGLContextClientVersion(2);
            MD360Director director = MD360DirectorFactory.createDirector(index);
            MD360Renderer renderer = MD360Renderer.with(context)
                    .setSurface(surface)
                    .setDirector(director)
                    .build();
            renderer.setStatus(mStatusManager.newChild());

            // Set the renderer to our demo renderer, defined below.
            glSurfaceView.setRenderer(renderer);

            mDirectorList.add(director);
            mGLSurfaceViewList.add(glSurfaceView);
        } else {
            glSurfaceView.setVisibility(View.GONE);
            Toast.makeText(context, "OpenGLES2 not supported.", Toast.LENGTH_SHORT).show();
        }
    }

    public void switchInteractiveMode(Context context) {
        int nextMode = mInteractiveMode == INTERACTIVE_MODE_MOTION ? INTERACTIVE_MODE_TOUCH : INTERACTIVE_MODE_MOTION;
        switchInteractiveMode(context, nextMode);
    }

    public void switchDisplayMode(){
        int nextMode = mDisplayMode == DISPLAY_MODE_NORMAL ? DISPLAY_MODE_GLASS : DISPLAY_MODE_NORMAL;
        switchDisplayMode(nextMode);
    }

    private void switchDisplayMode(int nextMode) {
        mDisplayMode = nextMode;
        int max;
        switch (nextMode){
            case DISPLAY_MODE_GLASS:
                max = mGLSurfaceViewList.size();
                break;
            case DISPLAY_MODE_NORMAL:
            default:
                max = 1;
                break;
        }
        int i = 0;
        for (GLSurfaceView surfaceView : mGLSurfaceViewList){
            if (i < max) {
                surfaceView.setVisibility(View.VISIBLE);
            } else {
                surfaceView.setVisibility(View.GONE);
            }
            i++;
        }

        // reset status manager
        mStatusManager.setDisplayMode(mDisplayMode);
    }

    private void syncDisplayMode() {
        switchDisplayMode(mDisplayMode);
    }

    private void switchInteractiveMode(Context context, int mode){
        if (mode == mInteractiveMode) return;
        mInteractiveMode = mode;
        switch (mode){
            case INTERACTIVE_MODE_MOTION:
                if (isResumed) registerSensor(context);
                for (MD360Director director : mDirectorList){
                    director.resetTouch();
                }
                break;
            case INTERACTIVE_MODE_TOUCH:
                if (isResumed) unregisterSensor(context);
                for (MD360Director director : mDirectorList){
                    director.resetMotion();
                }
                break;
        }
    }

    public void onResume(Context context){
        isResumed = true;
        if (mInteractiveMode == INTERACTIVE_MODE_MOTION) registerSensor(context);
        else unregisterSensor(context);

        for (GLSurfaceView glSurfaceView:mGLSurfaceViewList){
            glSurfaceView.onResume();
        }
    }

    public void onPause(Context context){
        isResumed = false;
        unregisterSensor(context);
        for (GLSurfaceView glSurfaceView:mGLSurfaceViewList){
            glSurfaceView.onPause();
        }
    }

    private void registerSensor(Context context){
        SensorManager mSensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        if (sensor == null){
            Log.e(TAG,"TYPE_ROTATION_VECTOR sensor not support!");
            return;
        }

        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
    }

    private void unregisterSensor(Context context){
        SensorManager mSensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mInteractiveMode != INTERACTIVE_MODE_MOTION) return;
        if (event.accuracy != 0){
            int type = event.sensor.getType();
            switch (type){
                case Sensor.TYPE_ROTATION_VECTOR:
                    VRUtil.sensorRotationVector2Matrix(event, mDeviceRotation, mSensorMatrix);
                    for (MD360Director director : mDirectorList){
                        director.updateSensorMatrix(mSensorMatrix);
                        if (mDisplayMode == DISPLAY_MODE_NORMAL) break;
                    }
                    break;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    /**
     * handle touch touch to rotate the model
     *
     * @param event
     * @return true if handled.
     */
    public boolean handleTouchEvent(MotionEvent event) {
        if (mInteractiveMode != INTERACTIVE_MODE_TOUCH) return false;
        boolean handled = false;
        for (MD360Director director : mDirectorList){
            handled |= director.handleTouchEvent(event);
        }
        return handled;
    }

    public int getInteractiveMode() {
        return mInteractiveMode;
    }

    public int getDisplayMode(){
        return mDisplayMode;
    }

    public interface IOnSurfaceReadyCallback {
        void onSurfaceReady(Surface surface);
    }

    private static class StatusManager {
        public static final int STATUS_INIT  = 0;
        public static final int STATUS_READY = 1;
        private int mStatus = STATUS_INIT;
        private int mDisplayMode;
        private SparseBooleanArray mReadyList = new SparseBooleanArray();

        public void setDisplayMode(int mode){
            mDisplayMode = mode;
            mStatus = STATUS_INIT;

            // clear ready list
            for (int i = 0; i < mReadyList.size(); i++){
                mReadyList.put(i,false);
            }
        }

        public boolean isReady(){
            switch (mDisplayMode){
                case DISPLAY_MODE_GLASS:    return mStatus == STATUS_READY;
                case DISPLAY_MODE_NORMAL:   return true;
            }
            return false;
        }

        synchronized public void setChildReady(int index) {
            // already ready.
            if (mReadyList.get(index)) return;

            // value changed.
            mReadyList.put(index,true);
            boolean ready = true;
            for (int i = 0; i < mReadyList.size(); i++){
                ready &= mReadyList.valueAt(i);
            }
            mStatus = ready ? STATUS_READY : STATUS_INIT;
        }

        public Status newChild(){
            int index = mReadyList.size();
            mReadyList.put(index, false);
            return new StatusImpl(index, this);
        }
    }

    private static class StatusImpl extends Status{

        private StatusManager manager;

        private StatusImpl(int id, StatusManager manager) {
            super(id);
            this.manager = manager;
        }

        @Override
        public boolean isAllReady() {
            return manager.isReady();
        }

        @Override
        public void ready() {
            manager.setChildReady(mId);
        }
    }

    static abstract class Status {
        protected int mId;
        abstract public boolean isAllReady();
        abstract public void ready();
        public Status(int id) {
            mId = id;
        }
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {
        private int displayMode = DISPLAY_MODE_NORMAL;
        private int interactiveMode = INTERACTIVE_MODE_MOTION;
        private IOnSurfaceReadyCallback callback;

        private Builder() {
        }

        public Builder displayMode(int displayMode){
            this.displayMode = displayMode;
            return this;
        }

        public Builder interactiveMode(int interactiveMode){
            this.interactiveMode = interactiveMode;
            return this;
        }

        public Builder callback(IOnSurfaceReadyCallback callback){
            this.callback = callback;
            return this;
        }

        public MDVRLibrary build(){
            return new MDVRLibrary(this);
        }

    }
}
