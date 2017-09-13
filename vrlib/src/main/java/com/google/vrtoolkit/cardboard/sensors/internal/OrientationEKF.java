//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.google.vrtoolkit.cardboard.sensors.internal;

public class OrientationEKF {
    private static final float NS2S = 1.0E-9F;
    private static final double MIN_ACCEL_NOISE_SIGMA = 0.75D;
    private static final double MAX_ACCEL_NOISE_SIGMA = 7.0D;
    private double[] rotationMatrix = new double[16];
    private Matrix3x3d so3SensorFromWorld = new Matrix3x3d();
    private Matrix3x3d so3LastMotion = new Matrix3x3d();
    private Matrix3x3d mP = new Matrix3x3d();
    private Matrix3x3d mQ = new Matrix3x3d();
    private Matrix3x3d mR = new Matrix3x3d();
    private Matrix3x3d mRaccel = new Matrix3x3d();
    private Matrix3x3d mS = new Matrix3x3d();
    private Matrix3x3d mH = new Matrix3x3d();
    private Matrix3x3d mK = new Matrix3x3d();
    private Vector3d mNu = new Vector3d();
    private Vector3d mz = new Vector3d();
    private Vector3d mh = new Vector3d();
    private Vector3d mu = new Vector3d();
    private Vector3d mx = new Vector3d();
    private Vector3d down = new Vector3d();
    private Vector3d north = new Vector3d();
    private long sensorTimeStampGyro;
    private final Vector3d lastGyro = new Vector3d();
    private double previousAccelNorm = 0.0D;
    private double movingAverageAccelNormChange = 0.0D;
    private float filteredGyroTimestep;
    private boolean timestepFilterInit = false;
    private int numGyroTimestepSamples;
    private boolean gyroFilterValid = true;
    private Matrix3x3d getPredictedGLMatrixTempM1 = new Matrix3x3d();
    private Matrix3x3d getPredictedGLMatrixTempM2 = new Matrix3x3d();
    private Vector3d getPredictedGLMatrixTempV1 = new Vector3d();
    private Matrix3x3d setHeadingDegreesTempM1 = new Matrix3x3d();
    private Matrix3x3d processGyroTempM1 = new Matrix3x3d();
    private Matrix3x3d processGyroTempM2 = new Matrix3x3d();
    private Matrix3x3d processAccTempM1 = new Matrix3x3d();
    private Matrix3x3d processAccTempM2 = new Matrix3x3d();
    private Matrix3x3d processAccTempM3 = new Matrix3x3d();
    private Matrix3x3d processAccTempM4 = new Matrix3x3d();
    private Matrix3x3d processAccTempM5 = new Matrix3x3d();
    private Vector3d processAccTempV1 = new Vector3d();
    private Vector3d processAccTempV2 = new Vector3d();
    private Vector3d processAccVDelta = new Vector3d();
    private Vector3d processMagTempV1 = new Vector3d();
    private Vector3d processMagTempV2 = new Vector3d();
    private Vector3d processMagTempV3 = new Vector3d();
    private Vector3d processMagTempV4 = new Vector3d();
    private Vector3d processMagTempV5 = new Vector3d();
    private Matrix3x3d processMagTempM1 = new Matrix3x3d();
    private Matrix3x3d processMagTempM2 = new Matrix3x3d();
    private Matrix3x3d processMagTempM4 = new Matrix3x3d();
    private Matrix3x3d processMagTempM5 = new Matrix3x3d();
    private Matrix3x3d processMagTempM6 = new Matrix3x3d();
    private Matrix3x3d updateCovariancesAfterMotionTempM1 = new Matrix3x3d();
    private Matrix3x3d updateCovariancesAfterMotionTempM2 = new Matrix3x3d();
    private Matrix3x3d accObservationFunctionForNumericalJacobianTempM = new Matrix3x3d();
    private Matrix3x3d magObservationFunctionForNumericalJacobianTempM = new Matrix3x3d();
    private boolean alignedToGravity;
    private boolean alignedToNorth;
    private So3Helper so3Helper = new So3Helper();

    public OrientationEKF() {
        this.reset();
    }

    public synchronized void reset() {
        this.sensorTimeStampGyro = 0L;
        this.so3SensorFromWorld.setIdentity();
        this.so3LastMotion.setIdentity();
        double initialSigmaP = 5.0D;
        this.mP.setZero();
        this.mP.setSameDiagonal(25.0D);
        double initialSigmaQ = 1.0D;
        this.mQ.setZero();
        this.mQ.setSameDiagonal(1.0D);
        double initialSigmaR = 0.25D;
        this.mR.setZero();
        this.mR.setSameDiagonal(0.0625D);
        this.mRaccel.setZero();
        this.mRaccel.setSameDiagonal(0.5625D);
        this.mS.setZero();
        this.mH.setZero();
        this.mK.setZero();
        this.mNu.setZero();
        this.mz.setZero();
        this.mh.setZero();
        this.mu.setZero();
        this.mx.setZero();
        this.down.set(0.0D, 0.0D, 9.81D);
        this.north.set(0.0D, 1.0D, 0.0D);
        this.alignedToGravity = false;
        this.alignedToNorth = false;
    }

    public boolean isReady() {
        return this.alignedToGravity;
    }

    public double getHeadingDegrees() {
        double x = this.so3SensorFromWorld.get(2, 0);
        double y = this.so3SensorFromWorld.get(2, 1);
        double mag = Math.sqrt(x * x + y * y);
        if(mag < 0.1D) {
            return 0.0D;
        } else {
            double heading = -90.0D - Math.atan2(y, x) / 3.141592653589793D * 180.0D;
            if(heading < 0.0D) {
                heading += 360.0D;
            }

            if(heading >= 360.0D) {
                heading -= 360.0D;
            }

            return heading;
        }
    }

    public synchronized void setHeadingDegrees(double heading) {
        double currentHeading = this.getHeadingDegrees();
        double deltaHeading = heading - currentHeading;
        double s = Math.sin(deltaHeading / 180.0D * 3.141592653589793D);
        double c = Math.cos(deltaHeading / 180.0D * 3.141592653589793D);
        double[][] deltaHeadingRotationVals = new double[][]{{c, -s, 0.0D}, {s, c, 0.0D}, {0.0D, 0.0D, 1.0D}};
        arrayAssign(deltaHeadingRotationVals, this.setHeadingDegreesTempM1);
        Matrix3x3d.mult(this.so3SensorFromWorld, this.setHeadingDegreesTempM1, this.so3SensorFromWorld);
    }

    public double[] getGLMatrix() {
        return this.glMatrixFromSo3(this.so3SensorFromWorld);
    }

    public double[] getPredictedGLMatrix(double secondsAfterLastGyroEvent) {
        Vector3d pmu = this.getPredictedGLMatrixTempV1;
        pmu.set(this.lastGyro);
        pmu.scale(-secondsAfterLastGyroEvent);
        Matrix3x3d so3PredictedMotion = this.getPredictedGLMatrixTempM1;
        So3Util.sO3FromMu(pmu, so3PredictedMotion);
        Matrix3x3d so3PredictedState = this.getPredictedGLMatrixTempM2;
        Matrix3x3d.mult(so3PredictedMotion, this.so3SensorFromWorld, so3PredictedState);
        return this.glMatrixFromSo3(so3PredictedState);
    }

    public Matrix3x3d getRotationMatrix() {
        return this.so3SensorFromWorld;
    }

    public static void arrayAssign(double[][] data, Matrix3x3d m) {
        assert 3 == data.length;

        assert 3 == data[0].length;

        assert 3 == data[1].length;

        assert 3 == data[2].length;

        m.set(data[0][0], data[0][1], data[0][2], data[1][0], data[1][1], data[1][2], data[2][0], data[2][1], data[2][2]);
    }

    public boolean isAlignedToGravity() {
        return this.alignedToGravity;
    }

    public boolean isAlignedToNorth() {
        return this.alignedToNorth;
    }

    public synchronized void processGyro(Vector3d gyro, long sensorTimeStamp) {
        float kTimeThreshold = 0.04F;
        float kdTdefault = 0.01F;
        if(this.sensorTimeStampGyro != 0L) {
            float dT = (float)(sensorTimeStamp - this.sensorTimeStampGyro) * 1.0E-9F;
            if(dT > 0.04F) {
                dT = this.gyroFilterValid?this.filteredGyroTimestep:0.01F;
            } else {
                this.filterGyroTimestep(dT);
            }

            this.mu.set(gyro);
            this.mu.scale((double)(-dT));
            So3Util.sO3FromMu(this.mu, this.so3LastMotion);
            this.processGyroTempM1.set(this.so3SensorFromWorld);
            Matrix3x3d.mult(this.so3LastMotion, this.so3SensorFromWorld, this.processGyroTempM1);
            this.so3SensorFromWorld.set(this.processGyroTempM1);
            this.updateCovariancesAfterMotion();
            this.processGyroTempM2.set(this.mQ);
            this.processGyroTempM2.scale((double)(dT * dT));
            this.mP.plusEquals(this.processGyroTempM2);
        }

        this.sensorTimeStampGyro = sensorTimeStamp;
        this.lastGyro.set(gyro);
    }

    private void updateAccelCovariance(double currentAccelNorm) {
        double currentAccelNormChange = Math.abs(currentAccelNorm - this.previousAccelNorm);
        this.previousAccelNorm = currentAccelNorm;
        double kSmoothingFactor = 0.5D;
        this.movingAverageAccelNormChange = 0.5D * currentAccelNormChange + 0.5D * this.movingAverageAccelNormChange;
        double kMaxAccelNormChange = 0.15D;
        double normChangeRatio = this.movingAverageAccelNormChange / 0.15D;
        double accelNoiseSigma = Math.min(7.0D, 0.75D + normChangeRatio * 6.25D);
        this.mRaccel.setSameDiagonal(accelNoiseSigma * accelNoiseSigma);
    }

    public synchronized void processAcc(Vector3d acc, long sensorTimeStamp) {
        this.mz.set(acc);
        this.updateAccelCovariance(this.mz.length());
        if(this.alignedToGravity) {
            this.accObservationFunctionForNumericalJacobian(this.so3SensorFromWorld, this.mNu);
            double eps = 1.0E-7D;

            for(int dof = 0; dof < 3; ++dof) {
                Vector3d delta = this.processAccVDelta;
                delta.setZero();
                delta.setComponent(dof, eps);
                So3Util.sO3FromMu(delta, this.processAccTempM1);
                Matrix3x3d.mult(this.processAccTempM1, this.so3SensorFromWorld, this.processAccTempM2);
                this.accObservationFunctionForNumericalJacobian(this.processAccTempM2, this.processAccTempV1);
                Vector3d withDelta = this.processAccTempV1;
                Vector3d.sub(this.mNu, withDelta, this.processAccTempV2);
                this.processAccTempV2.scale(1.0D / eps);
                this.mH.setColumn(dof, this.processAccTempV2);
            }

            this.mH.transpose(this.processAccTempM3);
            Matrix3x3d.mult(this.mP, this.processAccTempM3, this.processAccTempM4);
            Matrix3x3d.mult(this.mH, this.processAccTempM4, this.processAccTempM5);
            Matrix3x3d.add(this.processAccTempM5, this.mRaccel, this.mS);
            this.mS.invert(this.processAccTempM3);
            this.mH.transpose(this.processAccTempM4);
            Matrix3x3d.mult(this.processAccTempM4, this.processAccTempM3, this.processAccTempM5);
            Matrix3x3d.mult(this.mP, this.processAccTempM5, this.mK);
            Matrix3x3d.mult(this.mK, this.mNu, this.mx);
            Matrix3x3d.mult(this.mK, this.mH, this.processAccTempM3);
            this.processAccTempM4.setIdentity();
            this.processAccTempM4.minusEquals(this.processAccTempM3);
            Matrix3x3d.mult(this.processAccTempM4, this.mP, this.processAccTempM3);
            this.mP.set(this.processAccTempM3);
            So3Util.sO3FromMu(this.mx, this.so3LastMotion);
            Matrix3x3d.mult(this.so3LastMotion, this.so3SensorFromWorld, this.so3SensorFromWorld);
            this.updateCovariancesAfterMotion();
        } else {
            so3Helper.sO3FromTwoVec(this.down, this.mz, this.so3SensorFromWorld);
            this.alignedToGravity = true;
        }

    }

    public synchronized void processMag(float[] mag, long sensorTimeStamp) {
        if(this.alignedToGravity) {
            this.mz.set((double)mag[0], (double)mag[1], (double)mag[2]);
            this.mz.normalize();
            Vector3d downInSensorFrame = new Vector3d();
            this.so3SensorFromWorld.getColumn(2, downInSensorFrame);
            Vector3d.cross(this.mz, downInSensorFrame, this.processMagTempV1);
            Vector3d perpToDownAndMag = this.processMagTempV1;
            perpToDownAndMag.normalize();
            Vector3d.cross(downInSensorFrame, perpToDownAndMag, this.processMagTempV2);
            Vector3d magHorizontal = this.processMagTempV2;
            magHorizontal.normalize();
            this.mz.set(magHorizontal);
            if(this.alignedToNorth) {
                this.magObservationFunctionForNumericalJacobian(this.so3SensorFromWorld, this.mNu);
                double eps = 1.0E-7D;

                for(int dof = 0; dof < 3; ++dof) {
                    Vector3d delta = this.processMagTempV3;
                    delta.setZero();
                    delta.setComponent(dof, eps);
                    So3Util.sO3FromMu(delta, this.processMagTempM1);
                    Matrix3x3d.mult(this.processMagTempM1, this.so3SensorFromWorld, this.processMagTempM2);
                    this.magObservationFunctionForNumericalJacobian(this.processMagTempM2, this.processMagTempV4);
                    Vector3d withDelta = this.processMagTempV4;
                    Vector3d.sub(this.mNu, withDelta, this.processMagTempV5);
                    this.processMagTempV5.scale(1.0D / eps);
                    this.mH.setColumn(dof, this.processMagTempV5);
                }

                this.mH.transpose(this.processMagTempM4);
                Matrix3x3d.mult(this.mP, this.processMagTempM4, this.processMagTempM5);
                Matrix3x3d.mult(this.mH, this.processMagTempM5, this.processMagTempM6);
                Matrix3x3d.add(this.processMagTempM6, this.mR, this.mS);
                this.mS.invert(this.processMagTempM4);
                this.mH.transpose(this.processMagTempM5);
                Matrix3x3d.mult(this.processMagTempM5, this.processMagTempM4, this.processMagTempM6);
                Matrix3x3d.mult(this.mP, this.processMagTempM6, this.mK);
                Matrix3x3d.mult(this.mK, this.mNu, this.mx);
                Matrix3x3d.mult(this.mK, this.mH, this.processMagTempM4);
                this.processMagTempM5.setIdentity();
                this.processMagTempM5.minusEquals(this.processMagTempM4);
                Matrix3x3d.mult(this.processMagTempM5, this.mP, this.processMagTempM4);
                this.mP.set(this.processMagTempM4);
                So3Util.sO3FromMu(this.mx, this.so3LastMotion);
                Matrix3x3d.mult(this.so3LastMotion, this.so3SensorFromWorld, this.processMagTempM4);
                this.so3SensorFromWorld.set(this.processMagTempM4);
                this.updateCovariancesAfterMotion();
            } else {
                this.magObservationFunctionForNumericalJacobian(this.so3SensorFromWorld, this.mNu);
                So3Util.sO3FromMu(this.mNu, this.so3LastMotion);
                Matrix3x3d.mult(this.so3LastMotion, this.so3SensorFromWorld, this.processMagTempM4);
                this.so3SensorFromWorld.set(this.processMagTempM4);
                this.updateCovariancesAfterMotion();
                this.alignedToNorth = true;
            }

        }
    }

    private double[] glMatrixFromSo3(Matrix3x3d so3) {
        for(int r = 0; r < 3; ++r) {
            for(int c = 0; c < 3; ++c) {
                this.rotationMatrix[4 * c + r] = so3.get(r, c);
            }
        }

        this.rotationMatrix[3] = this.rotationMatrix[7] = this.rotationMatrix[11] = 0.0D;
        this.rotationMatrix[12] = this.rotationMatrix[13] = this.rotationMatrix[14] = 0.0D;
        this.rotationMatrix[15] = 1.0D;
        return this.rotationMatrix;
    }

    private void filterGyroTimestep(float timeStep) {
        float kFilterCoeff = 0.95F;
        float kMinSamples = 10.0F;
        if(!this.timestepFilterInit) {
            this.filteredGyroTimestep = timeStep;
            this.numGyroTimestepSamples = 1;
            this.timestepFilterInit = true;
        } else {
            this.filteredGyroTimestep = 0.95F * this.filteredGyroTimestep + 0.050000012F * timeStep;
            if((float)(++this.numGyroTimestepSamples) > 10.0F) {
                this.gyroFilterValid = true;
            }
        }

    }

    private void updateCovariancesAfterMotion() {
        this.so3LastMotion.transpose(this.updateCovariancesAfterMotionTempM1);
        Matrix3x3d.mult(this.mP, this.updateCovariancesAfterMotionTempM1, this.updateCovariancesAfterMotionTempM2);
        Matrix3x3d.mult(this.so3LastMotion, this.updateCovariancesAfterMotionTempM2, this.mP);
        this.so3LastMotion.setIdentity();
    }

    private void accObservationFunctionForNumericalJacobian(Matrix3x3d so3SensorFromWorldPred, Vector3d result) {
        Matrix3x3d.mult(so3SensorFromWorldPred, this.down, this.mh);
        so3Helper.sO3FromTwoVec(this.mh, this.mz, this.accObservationFunctionForNumericalJacobianTempM);
        so3Helper.muFromSO3(this.accObservationFunctionForNumericalJacobianTempM, result);
    }

    private void magObservationFunctionForNumericalJacobian(Matrix3x3d so3SensorFromWorldPred, Vector3d result) {
        Matrix3x3d.mult(so3SensorFromWorldPred, this.north, this.mh);
        so3Helper.sO3FromTwoVec(this.mh, this.mz, this.magObservationFunctionForNumericalJacobianTempM);
        so3Helper.muFromSO3(this.magObservationFunctionForNumericalJacobianTempM, result);
    }
}
