package com.google.vrtoolkit.cardboard.sensors.internal;

public class GyroBiasEstimator {
    private static final float GYRO_SMOOTHING_FACTOR = 0.01f;
    private static final float ACC_SMOOTHING_FACTOR = 0.1f;
    private static final Vector3d UP_VECTOR;
    private static final float MIN_ACCEL_DOT_WITH_UP;
    private static final float MIN_ACCEL_LENGTH = 1.0E-4f;
    private static final float MAX_GYRO_DIFF = 0.01f;
    private static final long CALIBRATION_DURATION_NS = 5000000000L;
    private static final long MAX_DELAY_BETWEEN_EVENTS_NS = 100000000L;
    private final Vector3d mLastGyro;
    private final Vector3d mCurrGyro;
    private final Vector3d mGyroDiff;
    private final Vector3d mCurrAcc;
    private final Vector3d mAccSmoothed;
    private final Vector3d mAccNormalizedTmp;
    private float mGyroMagnitudeDiffSmoothed;
    private final Estimate mBiasEstimate;
    private long mCalibrationStartTimeNs;
    private long mLastGyroTimeNs;
    private long mLastAccTimeNs;
    
    public GyroBiasEstimator() {
        super();
        this.mLastGyro = new Vector3d();
        this.mCurrGyro = new Vector3d();
        this.mGyroDiff = new Vector3d();
        this.mCurrAcc = new Vector3d();
        this.mAccSmoothed = new Vector3d();
        this.mAccNormalizedTmp = new Vector3d();
        this.mBiasEstimate = new Estimate();
        this.mCalibrationStartTimeNs = -1L;
        this.mLastGyroTimeNs = -1L;
        this.mLastAccTimeNs = -1L;
    }
    
    public void processGyroscope(final Vector3d gyro, final long sensorTimeStamp) {
        if (this.mBiasEstimate.mState == Estimate.State.CALIBRATED) {
            return;
        }
        this.mCurrGyro.set(gyro);
        Vector3d.sub(this.mCurrGyro, this.mLastGyro, this.mGyroDiff);
        final float mCurrDiff = (float)this.mGyroDiff.length();
        this.mGyroMagnitudeDiffSmoothed = MAX_GYRO_DIFF * mCurrDiff + (1-MAX_GYRO_DIFF) * this.mGyroMagnitudeDiffSmoothed;
        this.mLastGyro.set(this.mCurrGyro);
        final boolean eventIsDelayed = sensorTimeStamp > this.mLastGyroTimeNs + MAX_DELAY_BETWEEN_EVENTS_NS;
        this.mLastGyroTimeNs = sensorTimeStamp;
        if (eventIsDelayed) {
            this.resetCalibration();
            return;
        }
        if (this.mBiasEstimate.mState == Estimate.State.CALIBRATING && sensorTimeStamp > this.mCalibrationStartTimeNs + CALIBRATION_DURATION_NS) {
            this.mBiasEstimate.mState = Estimate.State.CALIBRATED;
            return;
        }
        if (!this.canCalibrateGyro()) {
            this.resetCalibration();
            return;
        }
        this.startCalibration(sensorTimeStamp);
    }
    
    private void resetCalibration() {
        this.mBiasEstimate.mState = Estimate.State.UNCALIBRATED;
        this.mBiasEstimate.mBias.set(0.0, 0.0, 0.0);
        this.mCalibrationStartTimeNs = -1L;
    }
    
    private void startCalibration(final long gyroTimeStamp) {
        if (this.mBiasEstimate.mState != Estimate.State.CALIBRATING) {
            this.mBiasEstimate.mBias.set(this.mCurrGyro);
            this.mBiasEstimate.mState = Estimate.State.CALIBRATING;
            this.mCalibrationStartTimeNs = gyroTimeStamp;
        }
        else {
            smooth(this.mBiasEstimate.mBias, this.mCurrGyro, GYRO_SMOOTHING_FACTOR);
        }
    }
    
    public void processAccelerometer(final Vector3d acc, final long sensorTimeStamp) {
        if (this.mBiasEstimate.mState == Estimate.State.CALIBRATED) {
            return;
        }
        this.mCurrAcc.set(acc);
        final boolean eventIsDelayed = sensorTimeStamp > this.mLastAccTimeNs + MAX_DELAY_BETWEEN_EVENTS_NS;
        this.mLastAccTimeNs = sensorTimeStamp;
        if (eventIsDelayed) {
            this.resetCalibration();
            return;
        }
        smooth(this.mAccSmoothed, this.mCurrAcc, ACC_SMOOTHING_FACTOR);
    }
    
    public void getEstimate(final Estimate output) {
        output.set(this.mBiasEstimate);
    }
    
    private boolean canCalibrateGyro() {
        if (this.mAccSmoothed.length() < MIN_ACCEL_LENGTH) {
            return false;
        }
        this.mAccNormalizedTmp.set(this.mAccSmoothed);
        this.mAccNormalizedTmp.normalize();
        return Vector3d.dot(this.mAccNormalizedTmp, GyroBiasEstimator.UP_VECTOR) >= GyroBiasEstimator.MIN_ACCEL_DOT_WITH_UP && this.mGyroMagnitudeDiffSmoothed <= 0.01f;
    }
    
    private static void smooth(final Vector3d smoothed, final Vector3d newValue, final float smoothingFactor) {
        smoothed.x = smoothingFactor * newValue.x + (1.0f - smoothingFactor) * smoothed.x;
        smoothed.y = smoothingFactor * newValue.y + (1.0f - smoothingFactor) * smoothed.y;
        smoothed.z = smoothingFactor * newValue.z + (1.0f - smoothingFactor) * smoothed.z;
    }
    
    static {
        UP_VECTOR = new Vector3d(0.0, 0.0, 1.0);
        MIN_ACCEL_DOT_WITH_UP = (float)Math.cos(Math.toRadians(10.0));
    }
    
    public static class Estimate
    {
        public State mState;
        public final Vector3d mBias;
        
        public Estimate() {
            super();
            this.mState = State.UNCALIBRATED;
            this.mBias = new Vector3d();
        }
        
        public void set(final Estimate from) {
            this.mState = from.mState;
            this.mBias.set(from.mBias);
        }
        
        public enum State
        {
            UNCALIBRATED, 
            CALIBRATING, 
            CALIBRATED;
        }
    }
}
