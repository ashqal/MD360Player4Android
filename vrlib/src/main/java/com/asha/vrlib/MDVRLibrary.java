package com.asha.vrlib;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.util.Log;
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
    public static final int MODE_MOTION = 1;
    public static final int MODE_TOUCH  = 2;

    private int mMode = MODE_MOTION;
    private float[] mSensorMatrix = new float[16];
    private boolean isResumed = false;
    private List<MD360Director> mDirectorList;
    private List<GLSurfaceView> mGLSurfaceViewList;
    private MD360Surface mSurface;
    private int mDeviceRotation;


    public MDVRLibrary(IOnSurfaceReadyCallback surfaceReadyListener) {
        mDirectorList = new LinkedList<>();
        mGLSurfaceViewList = new LinkedList<>();
        mSurface = new MD360Surface(surfaceReadyListener);
    }

    public void initWithGLSurfaceViewIds(Activity activity, int... glSurfaceViewIds){
        mDeviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        for (int id:glSurfaceViewIds){
            GLSurfaceView glSurfaceView = (GLSurfaceView) activity.findViewById(id);
            initOpenGL(activity,glSurfaceView,mSurface);
        }
    }

    private void initOpenGL(Context context, GLSurfaceView glSurfaceView, MD360Surface surface) {
        if (GLUtil.supportsEs2(context)) {
            // Request an OpenGL ES 2.0 compatible context.
            glSurfaceView.setEGLContextClientVersion(2);
            MD360Director director = new MD360Director();
            MD360Renderer renderer = MD360Renderer.with(context)
                    .setSurface(surface)
                    .setDirector(director)
                    .build();

            // Set the renderer to our demo renderer, defined below.
            glSurfaceView.setRenderer(renderer);

            mDirectorList.add(director);
            mGLSurfaceViewList.add(glSurfaceView);
        } else {
            glSurfaceView.setVisibility(View.GONE);
            Toast.makeText(context, "OpenGLES2 not supported.", Toast.LENGTH_SHORT).show();
        }
    }

    public void switchMode(Context context) {
        int nextMode = mMode == MODE_MOTION ? MODE_TOUCH : MODE_MOTION;
        switchMode (context, nextMode);
    }

    public void switch2MotionMode(Context context){
        switchMode(context, MODE_MOTION);
    }

    public void switch2TouchMode(Context context){
        switchMode(context, MODE_TOUCH);
    }

    private void switchMode(Context context, int mode){
        if (mode == mMode) return;
        mMode = mode;
        switch (mode){
            case MODE_MOTION:
                if (isResumed) registerSensor(context);
                for (MD360Director director : mDirectorList){
                    director.resetTouch();
                }
                break;
            case MODE_TOUCH:
                if (isResumed) unregisterSensor(context);
                for (MD360Director director : mDirectorList){
                    director.resetMotion();
                }
                break;
        }
    }

    public void onResume(Context context){
        isResumed = true;
        if (mMode == MODE_MOTION) registerSensor(context);
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
        if (mMode != MODE_MOTION) return;
        if (event.accuracy != 0){
            int type = event.sensor.getType();
            switch (type){
                case Sensor.TYPE_ROTATION_VECTOR:
                    VRUtil.sensorRotationVector2Matrix(event, mDeviceRotation, mSensorMatrix);
                    for (MD360Director director : mDirectorList){
                        director.updateSensorMatrix(mSensorMatrix);
                    }
                    break;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * handle touch touch to rotate the model
     *
     * @param event
     * @return true if handled.
     */
    public boolean handleTouchEvent(MotionEvent event) {
        if (mMode != MODE_TOUCH) return false;
        boolean handled = false;
        for (MD360Director director : mDirectorList){
            handled |= director.handleTouchEvent(event);
        }
        return handled;
    }

    public int getCurrentMode() {
        return mMode;
    }

    public interface IOnSurfaceReadyCallback {
        void onSurfaceReady(Surface surface);
    }
}
