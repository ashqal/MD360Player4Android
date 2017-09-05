package com.asha.vrlib;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.asha.vrlib.common.VRUtil;
import com.asha.vrlib.model.MDPosition;
import com.asha.vrlib.model.MDQuaternion;
import com.asha.vrlib.model.position.MDMutablePosition;

/**
 * Created by hzqiujiadi on 16/1/22.
 * hzqiujiadi ashqalcn@gmail.com
 *
 * response for model * view * projection
 */
public class MD360Director {

    private static final String TAG = "MD360Director";
    private static final float sNear = 0.7f;

    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];

    private float[] mMVMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private float[] mWorldRotationMatrix = new float[16];
    private float[] mWorldRotationInvertMatrix = new float[16];
    private float[] mCurrentRotationPost = new float[16];
    private float[] mSensorMatrix = new float[16];
    private float[] mTempMatrix = new float[16];
    private float[] mCameraMatrix = new float[16];

    private final MDDirectorCamera mCamera;
    private final MDDirectorCamUpdate mCameraUpdate = new MDDirectorCamUpdate();
    private final MDMutablePosition mCameraRotation = MDMutablePosition.newInstance();
    private final MDQuaternion mViewQuaternion = new MDQuaternion();
    private MDDirectorFilter mDirectorFilter;

    private float mDeltaX;
    private float mDeltaY;

    private boolean mWorldRotationMatrixInvalidate = true;

    protected MD360Director(Builder builder) {
        this.mCamera = builder.mCamera;
        initModel();
    }

    public float getDeltaY() {
        return mDeltaY;
    }

    public void setDeltaY(float mDeltaY) {
        this.mDeltaY = mDeltaY;
        mWorldRotationMatrixInvalidate = true;
    }

    public float getDeltaX() {
        return mDeltaX;
    }

    public void setDeltaX(float mDeltaX) {
        this.mDeltaX = mDeltaX;
        mWorldRotationMatrixInvalidate = true;
    }

    private void initModel(){
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setIdentityM(mSensorMatrix, 0);
        mViewQuaternion.fromMatrix(mViewMatrix);
    }

    public void beforeShot(){
        updateProjectionIfNeed();
        updateViewMatrixIfNeed();
    }

    public void shot(MD360Program program, MDPosition modelPosition) {
        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, modelPosition.getMatrix(), 0);


        // This multiplies the model view matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0);

        // Pass in the model view matrix
        GLES20.glUniformMatrix4fv(program.getMVMatrixHandle(), 1, false, mMVMatrix, 0);

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(program.getMVPMatrixHandle(), 1, false, mMVPMatrix, 0);
    }

    private void updateViewMatrixIfNeed(){
        boolean camera = mCamera.isPositionValidate() || mCameraUpdate.isPositionValidate();
        boolean world = mWorldRotationMatrixInvalidate || mCamera.isRotationValidate() || mCameraUpdate.isRotationValidate();

        if (camera){
            updateCameraMatrix();
            mCamera.consumePositionValidate();
            mCameraUpdate.consumePositionValidate();
        }

        if (world){
            mCameraRotation.setPitch(mCamera.getPitch() + mCameraUpdate.getPitch());
            mCameraRotation.setRoll(mCamera.getRoll() + mCameraUpdate.getRoll());
            mCameraRotation.setYaw(mCamera.getYaw() + mCameraUpdate.getYaw());

            // mCamera changed will be consumed after updateWorldRotationMatrix
            updateWorldRotationMatrix();
            mWorldRotationMatrixInvalidate = false;
            mCamera.consumeRotationValidate();
            mCameraUpdate.consumeRotationValidate();
        }

        if (camera || world){
            Matrix.multiplyMM(mViewMatrix, 0, mCameraMatrix, 0, mWorldRotationMatrix, 0);
            filterViewMatrix();
        }
    }

    private void filterViewMatrix() {
        if (mDirectorFilter == null) {
            return;
        }

        mViewQuaternion.fromMatrix(mViewMatrix);
        float pitch = mViewQuaternion.getPitch();
        float yaw = mViewQuaternion.getYaw();
        float roll = mViewQuaternion.getRoll();

        float filterPitch = mDirectorFilter.onFilterPitch(pitch);
        float filterYaw = mDirectorFilter.onFilterYaw(yaw);
        float filterRoll = mDirectorFilter.onFilterRoll(roll);

        if (pitch != filterPitch || yaw != filterYaw || roll != filterRoll){
            mViewQuaternion.setEulerAngles(filterPitch, filterYaw, filterRoll);
            mViewQuaternion.toMatrix(mViewMatrix);
        }
    }

    public void setViewport(int width, int height){
        // Projection Matrix
        mCamera.updateViewport(width, height);
    }

    public void setNearScale(float scale){
        mCamera.setNearScale(scale);
    }

    private void updateProjectionIfNeed(){
        if (mCamera.isProjectionValidate() || mCameraUpdate.isProjectionValidate()){
            updateProjection();
            mCamera.consumeProjectionValidate();
            mCameraUpdate.consumeProjectionValidate();
        }
    }

    protected void updateProjection(){
        final float left = -mCamera.getRatio()/2;
        final float right = mCamera.getRatio()/2;
        final float bottom = -0.5f;
        final float top = 0.5f;
        final float far = 500;
        Matrix.frustumM(getProjectionMatrix(), 0, left, right, bottom, top, getNear(), far);
    }

    protected float getNear(){
        return (mCamera.getNearScale() + mCameraUpdate.getNearScale()) * sNear;
    }

    protected float getRatio(){
        return mCamera.getRatio();
    }

    public float[] getProjectionMatrix(){
        return mProjectionMatrix;
    }

    public int getViewportWidth() {
        return mCamera.getViewportWidth();
    }

    public int getViewportHeight() {
        return mCamera.getViewportHeight();
    }

    public float[] getViewMatrix() {
        return mViewMatrix;
    }

    public MDQuaternion getViewQuaternion() {
        return mViewQuaternion;
    }

    private void updateCameraMatrix() {
        final float eyeX = mCamera.getEyeX() + mCameraUpdate.getEyeX();
        final float eyeY = mCamera.getEyeY() + mCameraUpdate.getEyeY();
        final float eyeZ = mCamera.getEyeZ() + mCameraUpdate.getEyeZ();
        final float lookX = mCamera.getLookX() + mCameraUpdate.getLookX();
        final float lookY = mCamera.getLookY() + mCameraUpdate.getLookY();
        final float lookZ = -1.0f;
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;
        Matrix.setIdentityM(mCameraMatrix, 0);
        Matrix.setLookAtM(mCameraMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
    }

    private void updateWorldRotationMatrix(){
        Matrix.setIdentityM(mWorldRotationMatrix, 0);
        Matrix.rotateM(mWorldRotationMatrix, 0, -mDeltaY, 1.0f, 0.0f, 0.0f);
        Matrix.setIdentityM(mCurrentRotationPost, 0);
        Matrix.rotateM(mCurrentRotationPost, 0, -mDeltaX, 0.0f, 1.0f, 0.0f);

        Matrix.setIdentityM(mTempMatrix, 0);
        Matrix.multiplyMM(mTempMatrix, 0, mCurrentRotationPost, 0, mCameraRotation.getMatrix(), 0);

        Matrix.multiplyMM(mCurrentRotationPost, 0, mSensorMatrix, 0, mTempMatrix, 0);
        Matrix.multiplyMM(mTempMatrix, 0, mWorldRotationMatrix, 0, mCurrentRotationPost, 0);
        System.arraycopy(mTempMatrix, 0, mWorldRotationMatrix, 0, 16);

        boolean success = VRUtil.invertM(mWorldRotationInvertMatrix, mWorldRotationMatrix);
        if (!success){
            Matrix.setIdentityM(mWorldRotationInvertMatrix, 0);
        }

    }

    // call in gl thread
    public void updateSensorMatrix(float[] sensorMatrix) {
        if (sensorMatrix == null
                || sensorMatrix.length != 16
                || Float.isNaN(sensorMatrix[0])
                || Float.isNaN(sensorMatrix[1])) {
            return;
        }

        System.arraycopy(sensorMatrix, 0, mSensorMatrix, 0, 16);
        mWorldRotationMatrixInvalidate = true;
    }

    // call in gl thread
    public void reset(){
        mDeltaX = mDeltaY = 0;
        Matrix.setIdentityM(mSensorMatrix, 0);
        mWorldRotationMatrixInvalidate = true;
    }

    public static Builder builder(){
        return new Builder();
    }

    public float[] getWorldRotationInvert() {
        return mWorldRotationInvertMatrix;
    }

    public void applyUpdate(MDDirectorCamUpdate cameraUpdate) {
        mCameraUpdate.copy(cameraUpdate);
    }

    public void applyFilter(MDDirectorFilter directorFilter) {
        this.mDirectorFilter = directorFilter;
    }

    public static class Builder {

        private MDDirectorCamera mCamera = new MDDirectorCamera();

        private MDDirectorCamera camera(){
            return mCamera;
        }

        public Builder setLookX(float mLookX) {
            camera().setLookX(mLookX);
            return this;
        }

        public Builder setLookY(float mLookY) {
            camera().setLookY(mLookY);
            return this;
        }

        public Builder setEyeX(float mEyeX) {
            camera().setEyeX(mEyeX);
            return this;
        }

        public Builder setEyeY(float mEyeY) {
            camera().setEyeY(mEyeY);
            return this;
        }

        public Builder setEyeZ(float mEyeZ) {
            camera().setEyeZ(mEyeZ);
            return this;
        }

        public Builder setNearScale(float scale) {
            camera().setNearScale(scale);
            return this;
        }

        public Builder setRoll(float roll){
            camera().setRoll(roll);
            return this;
        }

        public Builder setPitch(float pitch){
            camera().setPitch(pitch);
            return this;
        }

        public Builder setYaw(float yaw){
            camera().setYaw(yaw);
            return this;
        }

        public MD360Director build(){
            return new MD360Director(this);
        }
    }
}
