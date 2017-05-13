package com.asha.vrlib.plugins;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLES20;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MD360DirectorFactory;
import com.asha.vrlib.MD360Program;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.common.VRUtil;
import com.asha.vrlib.model.BarrelDistortionConfig;
import com.asha.vrlib.model.MDPosition;
import com.asha.vrlib.objects.MDAbsObject3D;
import com.asha.vrlib.objects.MDObject3DHelper;
import com.asha.vrlib.strategy.display.DisplayModeManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static com.asha.vrlib.common.GLUtil.glCheck;

/**
 * Created by hzqiujiadi on 16/7/27.
 * hzqiujiadi ashqalcn@gmail.com
 *
 * Barrel Distortion
 *
 * For more info,
 * http://stackoverflow.com/questions/12620025/barrel-distortion-correction-algorithm-to-correct-fisheye-lens-failing-to-impl
 */
public class MDBarrelDistortionLinePipe extends MDAbsLinePipe {

    private MD360Program mProgram;

    private MDBarrelDistortionMesh object3D;

    private MD360Director mDirector;

    private boolean mEnabled;

    private MDDrawingCache mDrawingCache;

    private BarrelDistortionConfig mConfiguration;

    private DisplayModeManager mDisplayModeManager;

    public MDBarrelDistortionLinePipe(DisplayModeManager displayModeManager) {
        mDisplayModeManager = displayModeManager;
        mConfiguration = displayModeManager.getBarrelDistortionConfig();
        mProgram = new MD360Program(MDVRLibrary.ContentType.FBO);
        mDirector = new MD360DirectorFactory.OrthogonalImpl().createDirector(0);
        object3D = new MDBarrelDistortionMesh();
        mDrawingCache = new MDDrawingCache();
    }

    @Override
    public void init(final Context context) {
        mProgram.build(context);
        MDObject3DHelper.loadObj(context,object3D);
    }

    @Override
    public void takeOver(int totalWidth, int totalHeight, int size) {
        mEnabled = mDisplayModeManager.isAntiDistortionEnabled();
        if (!mEnabled){
            return;
        }

        mDrawingCache.bind(totalWidth,totalHeight);

        mDirector.setViewport(totalWidth, totalHeight);
        object3D.setMode(size);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        glCheck("MDBarrelDistortionLinePipe glClear");
    }

    @Override
    public void commit(int totalWidth, int totalHeight, int size){
        if (!mEnabled){
            return;
        }
        mDrawingCache.unbind();

        int width = totalWidth / size;
        for (int i = 0; i < size; i++){
            GLES20.glViewport(width * i, 0, width, totalHeight);
            GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
            GLES20.glScissor(width * i, 0, width, totalHeight);
            draw(i);
            GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
        }
    }

    private void draw(int index){
        // Set our per-vertex lighting program.
        mProgram.use();
        glCheck("MDBarrelDistortionLinePipe mProgram use");

        object3D.uploadVerticesBufferIfNeed(mProgram, index);
        object3D.uploadTexCoordinateBufferIfNeed(mProgram, index);

        // Pass in the combined matrix.
        mDirector.beforeShot();
        mDirector.shot(mProgram, MDPosition.getOriginalPosition());

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mDrawingCache.getTextureOutput());

        object3D.draw();
    }

    private class MDBarrelDistortionMesh extends MDAbsObject3D {

        private static final String TAG = "MDBarrelDistortionMesh";
        private int mode;
        private FloatBuffer singleTexCoordinateBuffer;

        public MDBarrelDistortionMesh() {
        }

        @Override
        public FloatBuffer getTexCoordinateBuffer(int index) {
            if (mode == 1){
                return singleTexCoordinateBuffer;
            } else if (mode == 2){
                return super.getTexCoordinateBuffer(index);
            } else {
                return null;
            }
        }

        @Override
        protected void executeLoad(Context context) {
            generateMesh(this);
        }

        private void generateMesh(MDAbsObject3D object3D){
            int rows = 10;
            int columns = 10;
            int numPoint = (rows + 1) * (columns + 1);
            short r, s;
            float z = -8;
            float R = 1f/(float) rows;
            float S = 1f/(float) columns;

            float[] vertexs = new float[numPoint * 3];
            float[] texcoords = new float[numPoint * 2];
            float[] texcoords1 = new float[numPoint * 2];
            float[] texcoords2 = new float[numPoint * 2];
            short[] indices = new short[numPoint * 6];


            int t = 0;
            int v = 0;
            for(r = 0; r < rows + 1; r++) {
                for(s = 0; s < columns + 1; s++) {
                    int tu = t++;
                    int tv = t++;

                    texcoords[tu] = s*S;
                    texcoords[tv] = r*R;

                    texcoords1[tu] = s*S*0.5f;
                    texcoords1[tv] = r*R;

                    texcoords2[tu] = s*S*0.5f + 0.5f;
                    texcoords2[tv] = r*R;

                    vertexs[v++] = (s * S * 2 - 1);
                    vertexs[v++] = (r * R * 2 - 1);
                    vertexs[v++] = z;
                }
            }

            applyBarrelDistortion(numPoint, vertexs);

            int counter = 0;
            int sectorsPlusOne = columns + 1;
            for(r = 0; r < rows; r++){
                for(s = 0; s < columns; s++) {
                    short k0 = (short) ((r) * sectorsPlusOne + (s+1));  // (c)
                    short k1 = (short) ((r+1) * sectorsPlusOne + (s));    //(b)
                    short k2 = (short) (r * sectorsPlusOne + s);       //(a);
                    short k3 = (short) ((r) * sectorsPlusOne + (s+1));  // (c)
                    short k4 = (short) ((r+1) * sectorsPlusOne + (s+1));  // (d)
                    short k5 = (short) ((r+1) * sectorsPlusOne + (s));    //(b)

                    indices[counter++] = k0;
                    indices[counter++] = k1;
                    indices[counter++] = k2;
                    indices[counter++] = k3;
                    indices[counter++] = k4;
                    indices[counter++] = k5;
                }
            }

            // initialize vertex byte buffer for shape coordinates
            ByteBuffer bb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 4 bytes per float)
                    vertexs.length * 4);
            bb.order(ByteOrder.nativeOrder());
            FloatBuffer vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(vertexs);
            vertexBuffer.position(0);

            // initialize vertex byte buffer for shape coordinates
            ByteBuffer ee = ByteBuffer.allocateDirect(
                    texcoords.length * 4);
            ee.order(ByteOrder.nativeOrder());
            FloatBuffer texBuffer = ee.asFloatBuffer();
            texBuffer.put(texcoords);
            texBuffer.position(0);

            // initialize vertex byte buffer for shape coordinates
            ByteBuffer cc = ByteBuffer.allocateDirect(
                    texcoords1.length * 4);
            cc.order(ByteOrder.nativeOrder());
            FloatBuffer texBuffer1 = cc.asFloatBuffer();
            texBuffer1.put(texcoords1);
            texBuffer1.position(0);

            // initialize vertex byte buffer for shape coordinates
            ByteBuffer dd = ByteBuffer.allocateDirect(
                    texcoords2.length * 4);
            dd.order(ByteOrder.nativeOrder());
            FloatBuffer texBuffer2 = dd.asFloatBuffer();
            texBuffer2.put(texcoords2);
            texBuffer2.position(0);

            // initialize byte buffer for the draw list
            ByteBuffer dlb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 2 bytes per short)
                    indices.length * 2);
            dlb.order(ByteOrder.nativeOrder());
            ShortBuffer indexBuffer = dlb.asShortBuffer();
            indexBuffer.put(indices);
            indexBuffer.position(0);

            object3D.setIndicesBuffer(indexBuffer);
            object3D.setTexCoordinateBuffer(0,texBuffer1);
            object3D.setTexCoordinateBuffer(1,texBuffer2);
            object3D.setVerticesBuffer(0,vertexBuffer);
            object3D.setVerticesBuffer(1,vertexBuffer);
            object3D.setNumIndices(indices.length);

            singleTexCoordinateBuffer = texBuffer;
        }

        private void applyBarrelDistortion(int numPoint, float[] vertexs) {
            PointF pointF = new PointF();

            for (int i = 0; i < numPoint; i++){
                int xIndex = i * 3;
                int yIndex = i * 3 + 1;
                float xValue = vertexs[xIndex];
                float yValue = vertexs[yIndex];

                pointF.set(xValue,yValue);
                VRUtil.barrelDistortion(mConfiguration.getParamA(),
                        mConfiguration.getParamB(),
                        mConfiguration.getParamC(),
                        pointF);

                vertexs[xIndex] = pointF.x * mConfiguration.getScale();
                vertexs[yIndex] = pointF.y * mConfiguration.getScale();

                // Log.e(TAG,String.format("%f %f => %f %f",xValue,yValue,pointF.x,pointF.y));
            }
        }

        public void setMode(int mode) {
            this.mode = mode;
        }
    }

}
