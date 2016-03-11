package com.asha.vrlib;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.MotionEvent;

/**
 * Created by hzqiujiadi on 16/1/22.
 * hzqiujiadi ashqalcn@gmail.com
 *
 * response for model * view * projection
 */
public class MD360Director implements SensorEventListener {

    private static final String TAG = "MD360Director";
    private static final float sDensity =  Resources.getSystem().getDisplayMetrics().density;
    private static final float sDamping = 0.2f;

    private float[] mModelMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];

    private float[] mMVMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private float mEyeZ = 0f;
    private float mAngle = 0;
    private float mRatio = 1.5f;
    private float mNear = 0.7f;

    private float[] mCurrentRotation = new float[16];
    private float[] mAccumulatedRotation = new float[16];
    private float[] mTemporaryMatrix = new float[16];

    private float[] mSensorMatrix = new float[16];

    public MD360Director() {
        initCamera();
        initModel();
    }

    private float mPreviousX;
    private float mPreviousY;

    private float mDeltaX;
    private float mDeltaY;


    /**
     * handle touch touch to rotate the model
     *
     * @param event
     * @return true if handled.
     */
    public boolean handleTouchEvent(MotionEvent event) {
        if (event != null) {
            float x = event.getX();
            float y = event.getY();

            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                float deltaX = (x - mPreviousX) / sDensity * sDamping ;
                float deltaY = (y - mPreviousY) / sDensity * sDamping ;
                mDeltaX += deltaX;
                mDeltaY += deltaY;
            }
            mPreviousX = x;
            mPreviousY = y;
            return true;

        } else {
            return false;
        }
    }

    private void initCamera() {
        // View Matrix
        updateCameraDistance(mEyeZ);
    }

    private void initModel(){
        Matrix.setIdentityM(mAccumulatedRotation, 0);
        Matrix.setIdentityM(mSensorMatrix, 0);
        // Model Matrix
        updateModelRotate(mAngle);
    }

    public void shot(MD360Program program) {

        Matrix.setIdentityM(mModelMatrix, 0);

        Matrix.setIdentityM(mCurrentRotation, 0);
        Matrix.rotateM(mCurrentRotation, 0, -mDeltaY, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mCurrentRotation, 0, -mDeltaX + mAngle, 0.0f, 1.0f, 0.0f);
        Matrix.multiplyMM(mCurrentRotation, 0, mSensorMatrix, 0, mCurrentRotation, 0);

        // set the accumulated rotation to the result.
        System.arraycopy(mCurrentRotation, 0, mAccumulatedRotation, 0, 16);

        // Rotate the cube taking the overall rotation into account.
        Matrix.multiplyMM(mTemporaryMatrix, 0, mModelMatrix, 0, mAccumulatedRotation, 0);
        System.arraycopy(mTemporaryMatrix, 0, mModelMatrix, 0, 16);

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        // This multiplies the model view matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0);

        // Pass in the model view matrix
        GLES20.glUniformMatrix4fv(program.getMVMatrixHandle(), 1, false, mMVMatrix, 0);

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(program.getMVPMatrixHandle(), 1, false, mMVPMatrix, 0);
    }

    public void updateProjection(int width, int height){
        // Projection Matrix
        mRatio = width * 1.0f / height;
        updateProjectionNear(mNear);
    }

    private void updateCameraDistance(float z) {
        mEyeZ = z;
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = mEyeZ;
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -1.0f;
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
    }

    protected void updateModelRotate(float a) {
        mAngle = a;
    }

    private void updateProjectionNear(float near){
        mNear = near;
        final float left = -mRatio/2;
        final float right = mRatio/2;
        final float bottom = -0.5f;
        final float top = 0.5f;
        final float far = 500;
        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, mNear, far);
    }

    private float[] tmp = new float[16];

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.accuracy != 0){
            int type = event.sensor.getType();
            switch (type){
                case Sensor.TYPE_GYROSCOPE:
                    break;
                case Sensor.TYPE_ROTATION_VECTOR:
                    float[] values = event.values;
                    SensorManager.getRotationMatrixFromVector(tmp, values);
                    SensorManager.remapCoordinateSystem(tmp, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, mSensorMatrix);

                    //values[1] = -values[1];
                    //values[2] = -values[2];

                    Matrix.rotateM(mSensorMatrix, 0, 90.0F, 1.0F, 0.0F, 0.0F);
                    break;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void registerSensor(Context context){
        SensorManager mSensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
    }

    public void unregisterSensor(Context context){
        SensorManager mSensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.unregisterListener(this);
    }
}
