package com.asha.vrlib.strategy.projection;

import android.app.Activity;
import android.graphics.RectF;
import android.opengl.Matrix;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MD360DirectorFactory;
import com.asha.vrlib.objects.MDAbsObject3D;
import com.asha.vrlib.objects.MDObject3DHelper;
import com.asha.vrlib.objects.MDPlane;

/**
 * Created by hzqiujiadi on 16/6/26.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class PlaneProjection extends AbsProjectionStrategy {

    private class OrthogonalDirectorFactory extends MD360DirectorFactory{
        @Override
        public MD360Director createDirector(int index) {
            return new OrthogonalDirector(new MD360Director.Builder());
        }
    }

    private class OrthogonalDirector extends MD360Director{

        private OrthogonalDirector(Builder builder) {
            super(builder);
        }

        @Override
        public void setDeltaX(float mDeltaX) {
            // nop
        }

        @Override
        public void setDeltaY(float mDeltaY) {
            // nop
        }

        @Override
        public void updateSensorMatrix(float[] sensorMatrix) {
            // nop
        }

        @Override
        protected void updateProjection(){
            float textureRatio = mTextureSize.width() / mTextureSize.height();
            float viewportRatio = getRatio();
            final float left = -0.5f;
            final float right = 0.5f;
            final float bottom = -0.5f / viewportRatio;
            final float top = 0.5f / viewportRatio;
            final float far = 500;
            Matrix.orthoM(getProjectionMatrix(), 0, left, right, bottom, top, getNear(), far);
        }
    }

    MDPlane object3D;

    private RectF mTextureSize;

    public PlaneProjection(RectF mTextureSize) {
        this.mTextureSize = mTextureSize;
    }

    @Override
    public void on(Activity activity) {
        object3D = new MDPlane(mTextureSize);
        MDObject3DHelper.loadObj(activity, object3D);
    }

    @Override
    public void off(Activity activity) {

    }

    @Override
    public boolean isSupport(Activity activity) {
        return true;
    }

    @Override
    public MDAbsObject3D getObject3D() {
        return object3D;
    }

    @Override
    protected MD360DirectorFactory hijackDirectorFactory() {
        return new OrthogonalDirectorFactory();
    }
}
