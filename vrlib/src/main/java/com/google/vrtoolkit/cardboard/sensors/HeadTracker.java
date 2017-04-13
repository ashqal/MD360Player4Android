//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.google.vrtoolkit.cardboard.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.view.Display;
import android.view.WindowManager;

import com.google.vrtoolkit.cardboard.sensors.internal.GyroscopeBiasEstimator;
import com.google.vrtoolkit.cardboard.sensors.internal.Matrix3x3d;
import com.google.vrtoolkit.cardboard.sensors.internal.OrientationEKF;
import com.google.vrtoolkit.cardboard.sensors.internal.Vector3d;

import java.util.concurrent.TimeUnit;

public class HeadTracker implements SensorEventListener {
    private static final float DEFAULT_NECK_HORIZONTAL_OFFSET = 0.08F;
    private static final float DEFAULT_NECK_VERTICAL_OFFSET = 0.075F;
    private static final float DEFAULT_NECK_MODEL_FACTOR = 1.0F;
    private static final float PREDICTION_TIME_IN_SECONDS = 0.058F;
    private final Display display;
    private final float[] ekfToHeadTracker = new float[16];
    private final float[] sensorToDisplay = new float[16];
    private float displayRotation = -1.0F;
    private final float[] neckModelTranslation = new float[16];
    private final float[] tmpHeadView = new float[16];
    private final float[] tmpHeadView2 = new float[16];
    private float neckModelFactor = 1.0F;
    private final Object neckModelFactorMutex = new Object();
    private volatile boolean tracking;
    private final OrientationEKF tracker;
    private final Object gyroBiasEstimatorMutex = new Object();
    private GyroscopeBiasEstimator gyroBiasEstimator;
    private SensorEventProvider sensorEventProvider;
    private Clock clock;
    private long latestGyroEventClockTimeNs;
    private volatile boolean firstGyroValue = true;
    private float[] initialSystemGyroBias = new float[3];
    private final Vector3d gyroBias = new Vector3d();
    private final Vector3d latestGyro = new Vector3d();
    private final Vector3d latestAcc = new Vector3d();

    public static HeadTracker createFromContext(Context context) {
        SensorManager sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        return new HeadTracker(new DeviceSensorLooper(sensorManager), new SystemClock(), display);
    }

    public HeadTracker(SensorEventProvider sensorEventProvider, Clock clock, Display display) {
        this.clock = clock;
        this.sensorEventProvider = sensorEventProvider;
        this.tracker = new OrientationEKF();
        this.display = display;
        this.setGyroBiasEstimationEnabled(true);
        Matrix.setIdentityM(this.neckModelTranslation, 0);
    }

    public void onSensorChanged(SensorEvent event) {
        Object var2;
        if(event.sensor.getType() == 1) {
            this.latestAcc.set((double)event.values[0], (double)event.values[1], (double)event.values[2]);
            this.tracker.processAcc(this.latestAcc, event.timestamp);
            var2 = this.gyroBiasEstimatorMutex;
            synchronized(this.gyroBiasEstimatorMutex) {
                if(this.gyroBiasEstimator != null) {
                    this.gyroBiasEstimator.processAccelerometer(this.latestAcc, event.timestamp);
                }
            }
        } else if(event.sensor.getType() == 4 || event.sensor.getType() == 16) {
            this.latestGyroEventClockTimeNs = this.clock.nanoTime();
            if(event.sensor.getType() == 16) {
                if(this.firstGyroValue && event.values.length == 6) {
                    this.initialSystemGyroBias[0] = event.values[3];
                    this.initialSystemGyroBias[1] = event.values[4];
                    this.initialSystemGyroBias[2] = event.values[5];
                }

                this.latestGyro.set((double)(event.values[0] - this.initialSystemGyroBias[0]), (double)(event.values[1] - this.initialSystemGyroBias[1]), (double)(event.values[2] - this.initialSystemGyroBias[2]));
            } else {
                this.latestGyro.set((double)event.values[0], (double)event.values[1], (double)event.values[2]);
            }

            this.firstGyroValue = false;
            var2 = this.gyroBiasEstimatorMutex;
            synchronized(this.gyroBiasEstimatorMutex) {
                if(this.gyroBiasEstimator != null) {
                    this.gyroBiasEstimator.processGyroscope(this.latestGyro, event.timestamp);
                    this.gyroBiasEstimator.getGyroBias(this.gyroBias);
                    Vector3d.sub(this.latestGyro, this.gyroBias, this.latestGyro);
                }
            }

            this.tracker.processGyro(this.latestGyro, event.timestamp);
        }

    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void startTracking() {
        if(!this.tracking) {
            this.tracker.reset();
            Object var1 = this.gyroBiasEstimatorMutex;
            synchronized(this.gyroBiasEstimatorMutex) {
                if(this.gyroBiasEstimator != null) {
                    this.gyroBiasEstimator.reset();
                }
            }

            this.firstGyroValue = true;
            this.sensorEventProvider.registerListener(this);
            this.sensorEventProvider.start();
            this.tracking = true;
        }
    }

    public void resetTracker() {
        this.tracker.reset();
    }

    public void stopTracking() {
        if(this.tracking) {
            this.sensorEventProvider.unregisterListener(this);
            this.sensorEventProvider.stop();
            this.tracking = false;
        }
    }

    public void setNeckModelEnabled(boolean enabled) {
        if(enabled) {
            this.setNeckModelFactor(1.0F);
        } else {
            this.setNeckModelFactor(0.0F);
        }

    }

    public float getNeckModelFactor() {
        Object var1 = this.neckModelFactorMutex;
        synchronized(this.neckModelFactorMutex) {
            return this.neckModelFactor;
        }
    }

    public void setNeckModelFactor(float factor) {
        Object var2 = this.neckModelFactorMutex;
        synchronized(this.neckModelFactorMutex) {
            if(factor >= 0.0F && factor <= 1.0F) {
                this.neckModelFactor = factor;
            } else {
                throw new IllegalArgumentException("factor should be within [0.0, 1.0]");
            }
        }
    }

    public void setGyroBiasEstimationEnabled(boolean enabled) {
        Object var2 = this.gyroBiasEstimatorMutex;
        synchronized(this.gyroBiasEstimatorMutex) {
            if(!enabled) {
                this.gyroBiasEstimator = null;
            } else if(this.gyroBiasEstimator == null) {
                this.gyroBiasEstimator = new GyroscopeBiasEstimator();
            }

        }
    }

    public boolean getGyroBiasEstimationEnabled() {
        Object var1 = this.gyroBiasEstimatorMutex;
        synchronized(this.gyroBiasEstimatorMutex) {
            return this.gyroBiasEstimator != null;
        }
    }

    public void getLastHeadView(float[] headView, int offset) {
        if(offset + 16 > headView.length) {
            throw new IllegalArgumentException("Not enough space to write the result");
        } else {
            float rotation = 0.0F;
            switch(this.display.getRotation()) {
                case 0:
                    rotation = 0.0F;
                    break;
                case 1:
                    rotation = 90.0F;
                    break;
                case 2:
                    rotation = 180.0F;
                    break;
                case 3:
                    rotation = 270.0F;
            }

            if(rotation != this.displayRotation) {
                this.displayRotation = rotation;
                Matrix.setRotateEulerM(this.sensorToDisplay, 0, 0.0F, 0.0F, -rotation);
                Matrix.setRotateEulerM(this.ekfToHeadTracker, 0, -90.0F, 0.0F, rotation);
            }

            OrientationEKF var4 = this.tracker;
            synchronized(this.tracker) {
                if(!this.tracker.isReady()) {
                    return;
                }

                double secondsSinceLastGyroEvent = (double)TimeUnit.NANOSECONDS.toSeconds(this.clock.nanoTime() - this.latestGyroEventClockTimeNs);
                double secondsToPredictForward = secondsSinceLastGyroEvent + 0.057999998331069946D;
                double[] mat = this.tracker.getPredictedGLMatrix(secondsToPredictForward);
                int i = 0;

                while(true) {
                    if(i >= headView.length) {
                        break;
                    }

                    this.tmpHeadView[i] = (float)mat[i];
                    ++i;
                }
            }

            Matrix.multiplyMM(this.tmpHeadView2, 0, this.sensorToDisplay, 0, this.tmpHeadView, 0);
            Matrix.multiplyMM(headView, offset, this.tmpHeadView2, 0, this.ekfToHeadTracker, 0);
            Matrix.setIdentityM(this.neckModelTranslation, 0);
            Matrix.translateM(this.neckModelTranslation, 0, 0.0F, -this.neckModelFactor * 0.075F, this.neckModelFactor * 0.08F);
            Matrix.multiplyMM(this.tmpHeadView, 0, this.neckModelTranslation, 0, headView, offset);
            Matrix.translateM(headView, offset, this.tmpHeadView, 0, 0.0F, this.neckModelFactor * 0.075F, 0.0F);
        }
    }

    Matrix3x3d getCurrentPoseForTest() {
        return new Matrix3x3d(this.tracker.getRotationMatrix());
    }

    void setGyroBiasEstimator(GyroscopeBiasEstimator estimator) {
        Object var2 = this.gyroBiasEstimatorMutex;
        synchronized(this.gyroBiasEstimatorMutex) {
            this.gyroBiasEstimator = estimator;
        }
    }
}
