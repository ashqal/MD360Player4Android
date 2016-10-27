package com.asha.vrlib.strategy.interactive;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.common.MDMainHandler;
import com.asha.vrlib.common.VRUtil;
import com.google.vrtoolkit.cardboard.sensors.internal.OrientationEKF;
import com.google.vrtoolkit.cardboard.sensors.internal.Vector3d;

import java.util.concurrent.TimeUnit;

/**
 * Created by hzqiujiadi on 16/3/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class CardboardMotionStrategy extends AbsInteractiveStrategy implements SensorEventListener {

    private static final String TAG = "CardboardMotionStrategy";

    private int mDeviceRotation;

    private float[] mResultMatrix = new float[16];

    private float[] mTmpMatrix = new float[16];

    private float[] mRotateMatrix = new float[16];

    private float[] mEkfToHeadTracker = new float[16];

    private boolean mRegistered = false;

    private Boolean mIsSupport = null;

    private final OrientationEKF mTracker = new OrientationEKF();

    private Vector3d mLatestAcc = new Vector3d();

    private long mLatestGyroEventClockTimeNs;

    private final Vector3d mGyroBias = new Vector3d();

    private final Vector3d mLatestGyro = new Vector3d();

    public CardboardMotionStrategy(InteractiveModeManager.Params params) {
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
    public void onOrientationChanged(Activity activity) {
        mDeviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
    }

    @Override
    public void on(Activity activity) {
        mDeviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        for (MD360Director director : getDirectorList()){
            director.reset();
        }
    }

    @Override
    public void off(Activity activity) {
        unregisterSensor(activity);
    }

    @Override
    public boolean isSupport(Activity activity) {
        if (mIsSupport == null){
            SensorManager mSensorManager = (SensorManager) activity
                    .getSystemService(Context.SENSOR_SERVICE);
            Sensor sensor1 = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Sensor sensor2 = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            mIsSupport = (sensor1 != null || sensor2 != null);
        }
        return mIsSupport;
    }

    protected void registerSensor(Context context){
        if (mRegistered) return;

        SensorManager mSensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor1 = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor sensor2 = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        if (sensor1 == null && sensor2 == null){
            Log.e(TAG,"TYPE_ACCELEROMETER TYPE_GYROSCOPE sensor not support!");
            return;
        }

        mSensorManager.registerListener(this, sensor1, getParams().mMotionDelay, MDMainHandler.sharedHandler());
        mSensorManager.registerListener(this, sensor2, getParams().mMotionDelay, MDMainHandler.sharedHandler());

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
        if (event.accuracy != 0){
            if (getParams().mSensorListener != null){
                getParams().mSensorListener.onSensorChanged(event);
            }

            int type = event.sensor.getType();

            if (type == Sensor.TYPE_ACCELEROMETER){
                synchronized (mTracker){
                    this.mLatestAcc.set(event.values[0], event.values[1], event.values[2]);
                    this.mTracker.processAcc(this.mLatestAcc, event.timestamp);
                }

            } else if(type == Sensor.TYPE_GYROSCOPE){
                synchronized (mTracker){
                    this.mLatestGyroEventClockTimeNs = System.nanoTime();
                    this.mLatestGyro.set(event.values[0], event.values[1], event.values[2]);
                    Vector3d.sub(this.mLatestGyro, this.mGyroBias, this.mLatestGyro);
                    this.mTracker.processGyro(this.mLatestGyro, event.timestamp);
                }
            }

            getParams().glHandler.post(updateSensorRunnable);
        }
    }

    private Runnable updateSensorRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mRegistered) return;

            // mTracker will be used in multi thread.
            synchronized (mTracker){
                final double secondsSinceLastGyroEvent = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - mLatestGyroEventClockTimeNs);
                final double secondsToPredictForward = secondsSinceLastGyroEvent + 1.0/60;
                final double[] mat = mTracker.getPredictedGLMatrix(secondsToPredictForward);
                for (int i = 0; i < mat.length; i++){
                    mTmpMatrix[i] = (float) mat[i];
                }
            }

            float rotation = 0;
            switch (mDeviceRotation){
                case Surface.ROTATION_0:
                    rotation = 0;
                    break;
                case Surface.ROTATION_90:
                    rotation = 90.0f;
                    break;
                case Surface.ROTATION_180:
                    rotation = 180.0f;
                    break;
                case Surface.ROTATION_270:
                    rotation = 270.0f;
                    break;
            }

            Matrix.setRotateEulerM(mRotateMatrix, 0, 0.0f, 0.0f, -rotation);
            Matrix.setRotateEulerM(mEkfToHeadTracker, 0, -90.0f, 0.0f, rotation);

            Matrix.multiplyMM(mResultMatrix, 0, mRotateMatrix, 0, mTmpMatrix , 0);
            Matrix.multiplyMM(mTmpMatrix, 0, mResultMatrix, 0, mEkfToHeadTracker, 0);

            for (MD360Director director : getDirectorList()){
                director.updateSensorMatrix(mTmpMatrix);
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
