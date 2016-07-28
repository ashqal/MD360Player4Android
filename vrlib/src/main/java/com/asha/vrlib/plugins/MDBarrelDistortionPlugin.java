package com.asha.vrlib.plugins;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MD360Program;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.common.GLUtil;
import com.asha.vrlib.objects.MDAbsObject3D;
import com.asha.vrlib.objects.MDObject3DHelper;
import com.asha.vrlib.objects.MDPlane;
import com.asha.vrlib.strategy.projection.PlaneProjection;

import java.nio.Buffer;

import static com.asha.vrlib.common.GLUtil.glCheck;

/**
 * Created by hzqiujiadi on 16/7/27.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDBarrelDistortionPlugin extends MDAbsPlugin {

    private MD360Program mProgram;

    private MDAbsObject3D object3D;

    private MD360Director director;

    private int mFrameBufferId;

    private int mTextureId;

    private PlaneProjection.PlaneScaleCalculator calculator;

    public MDBarrelDistortionPlugin(final Context context) {
        mProgram = new MD360Program(MDVRLibrary.ContentType.BITMAP);
        director = new OrthogonalDirector(new MD360Director.Builder());

        final RectF rectF = new RectF();
        calculator = new PlaneProjection.PlaneScaleCalculator(MDVRLibrary.PROJECTION_MODE_PLANE_FULL,rectF);
        object3D = new MDBarrelDistortionPlane(calculator);
        MDObject3DHelper.loadObj(context,object3D);

    }

    @Override
    public void init(final Context context) {
        mProgram.build(context);

        // frame buffer
        int[] frameBuffer = new int[1];
        GLES20.glGenFramebuffers(1, frameBuffer, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0]);
        mFrameBufferId = frameBuffer[0];
        glCheck("MDBarrelDistortionPlugin frame buffer");

        // renderer buffer
        final int[] renderbufferIds = { 0 };
        GLES20.glGenRenderbuffers(1, renderbufferIds, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderbufferIds[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, 1000, 1000);
        glCheck("MDBarrelDistortionPlugin renderer buffer");


        final int[] textureIds = { 0 };
        GLES20.glGenTextures(1, textureIds, 0);
        mTextureId = textureIds[0];
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);

        // GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, BitmapFactory.decodeResource(context.getResources(), R.drawable.bitmap2), 0);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 1000, 1000, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, (Buffer)null);


        // attach
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mTextureId, 0);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, renderbufferIds[0]);
        glCheck("MDBarrelDistortionPlugin attach");

        // check
        final int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            final String s = "Framebuffer is not complete: ";
            final String value = String.valueOf(Integer.toHexString(status));
            throw new RuntimeException((value.length() != 0) ? s.concat(value) : new String(s));
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        glCheck("MDBarrelDistortionPlugin attach");
    }

    @Override
    public void renderer(int width, int height, int index) {

    }

    @Override
    public void destroy() {

    }

    public void before(int itemWidth, int mHeight, int i) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, this.mFrameBufferId);
    }

    public void after(int width, int height, int index) {

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        // GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        // Set our per-vertex lighting program.
        mProgram.use();
        glCheck("mProgram use");

        // mTextureId

        object3D.uploadVerticesBufferIfNeed(mProgram, index);

        object3D.uploadTexCoordinateBufferIfNeed(mProgram, index);

        // Pass in the combined matrix.
        director.updateViewport(width,height);
        director.shot(mProgram);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);

        object3D.draw();
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
            calculator.setViewportRatio(getRatio());
            calculator.calculate();
            final float left = - calculator.getViewportWidth();
            final float right = calculator.getViewportWidth();
            final float bottom = - calculator.getViewportHeight();
            final float top = calculator.getViewportHeight();
            final float far = 500;
            Matrix.orthoM(getProjectionMatrix(), 0, left, right, bottom, top, getNear(), far);
        }
    }

    private static class MDBarrelDistortionPlane extends MDPlane {

        private static final String TAG = "MDBarrelDistortionPlane";

        public MDBarrelDistortionPlane(PlaneProjection.PlaneScaleCalculator calculator) {
            super(calculator);
        }

        @Override
        protected float[] generateVertex() {
            float[] vertex = super.generateVertex();
            PointF pointF = new PointF();

            for (int i = 0; i < getNumPoint(); i++){
                int xIndex = i * 3;
                int yIndex = i * 3 + 1;
                float xValue = vertex[xIndex];
                float yValue = vertex[yIndex];

                pointF.set(xValue,yValue);
                GLUtil.barrelDistortion(pointF);

                vertex[xIndex] = pointF.x * 0.8f;
                vertex[yIndex] = pointF.y * 0.8f;

                Log.e(TAG,String.format("%f %f => %f %f",xValue,yValue,pointF.x,pointF.y));
            }
            return vertex;
        }

        @Override
        protected int getNumColumn() {
            return 10;
        }

        @Override
        protected int getNumRow() {
            return 10;
        }
    }

}
