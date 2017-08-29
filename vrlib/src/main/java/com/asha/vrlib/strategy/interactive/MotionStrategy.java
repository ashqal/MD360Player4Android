package com.asha.vrlib.strategy.interactive;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.WindowManager;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.common.MDMainHandler;
import com.asha.vrlib.common.VRUtil;

/**
 * Created by hzqiujiadi on 16/3/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MotionStrategy extends AbsInteractiveStrategy implements SensorEventListener {

    private static final String TAG = "MotionStrategy";
	
    private WindowManager windowManager;

    private float[] mSensorMatrix = new float[16];

    private float[] mTmpMatrix = new float[16];

    private boolean mRegistered = false;

    private Boolean mIsSupport = null;

    private final Object mMatrixLock = new Object();

    private boolean isOn;

    public MotionStrategy(InteractiveModeManager.Params params) {
        super(params);
    }

    @Override
    public void onResume(Context context) {
        registerSensor(context);
    }

    @Override
    public void onPause(Context context) {
        unregisterSensor(context);
    }

    @Override
    public boolean handleDrag(int distanceX, int distanceY) {
        return false;
    }

    @Override
    public void onOrientationChanged(Context context) {
    }

    @Override
    public void turnOnInGL(Context context) {
        isOn = true;
        windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        for (MD360Director director : getDirectorList()){
            director.reset();
        }
    }

    @Override
    public void turnOffInGL(final Context context) {
        isOn = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                unregisterSensor(context);
            }
        });
    }

    @Override
    public boolean isSupport(Context context) {
        if (mIsSupport == null){
            SensorManager mSensorManager = (SensorManager) context
                    .getSystemService(Context.SENSOR_SERVICE);
            Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            mIsSupport = (sensor != null);
        }
        return mIsSupport;
    }

    protected void registerSensor(Context context){
        if (mRegistered) return;

        SensorManager mSensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        if (sensor == null){
            Log.e(TAG,"TYPE_ROTATION_VECTOR sensor not support!");
            return;
        }

        mSensorManager.registerListener(this, sensor, getParams().mMotionDelay, MDMainHandler.sharedHandler());

        mRegistered = true;
    }

    protected void unregisterSensor(Context context){
        if (!mRegistered) return;

        SensorManager mSensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.unregisterListener(this);

        mRegistered = false;
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        if (isOn && event.accuracy != 0){
            if (getParams().mSensorListener != null){
                getParams().mSensorListener.onSensorChanged(event);
            }

            int type = event.sensor.getType();
            switch (type){
                case Sensor.TYPE_ROTATION_VECTOR:
                    // post
                    VRUtil.sensorRotationVector2Matrix(event, windowManager.getDefaultDisplay().getRotation(), mSensorMatrix);

                    // mTmpMatrix will be used in multi thread.
                    synchronized (mMatrixLock){
                        System.arraycopy(mSensorMatrix, 0, mTmpMatrix, 0, 16);
                    }
                    getParams().glHandler.post(updateSensorRunnable);
                    break;
            }
        }
    }

    private Runnable updateSensorRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mRegistered || !isOn) return;
            // mTmpMatrix will be used in multi thread.
            synchronized (mMatrixLock){
                for (MD360Director director : getDirectorList()){
                    director.updateSensorMatrix(mTmpMatrix);
                }
            }
        }
    };

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (getParams().mSensorListener != null){
            getParams().mSensorListener.onAccuracyChanged(sensor,accuracy);
        }
    }
}
