package com.asha.vrlib.plugins;

import android.content.Context;
import android.opengl.GLES20;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MD360DirectorFactory;
import com.asha.vrlib.MD360Program;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.common.MDDirection;
import com.asha.vrlib.model.MDMainPluginBuilder;
import com.asha.vrlib.model.MDPosition;
import com.asha.vrlib.objects.MDAbsObject3D;
import com.asha.vrlib.objects.MDObject3DHelper;
import com.asha.vrlib.strategy.projection.ProjectionModeManager;
import com.asha.vrlib.texture.MD360Texture;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static com.asha.vrlib.common.GLUtil.glCheck;

/**
 * Created by hzqiujiadi on 16/8/20.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDMultiFishEyePlugin extends MDAbsPlugin {

    private MD360Program mProgram;

    private MD360Program mBitmapProgram;

    private MD360Texture mTexture;

    private ProjectionModeManager mProjectionModeManager;

    private MDMesh mConverterObject3D;

    private MD360Director mFixedDirector;

    private MDDrawingCache mDrawingCache;

    public MDMultiFishEyePlugin(MDMainPluginBuilder builder, float radius, MDDirection direction) {
        mTexture = builder.getTexture();
        mProgram = new MD360Program(builder.getContentType());
        mBitmapProgram = new MD360Program(MDVRLibrary.ContentType.FBO);

        mProjectionModeManager = builder.getProjectionModeManager();

        mFixedDirector = new MD360DirectorFactory.OrthogonalImpl().createDirector(0);
        mConverterObject3D = new MDMesh(radius, direction);
        mDrawingCache = new MDDrawingCache();
    }

    @Override
    public void initInGL(Context context) {
        mProgram.build(context);
        mBitmapProgram.build(context);
        mTexture.create();

        MDObject3DHelper.loadObj(context, mConverterObject3D);
    }

    @Override
    public void beforeRenderer(int totalWidth, int totalHeight) {
        mFixedDirector.setViewport(totalWidth, totalHeight);
        mDrawingCache.bind(totalWidth, totalHeight);
        drawConverter(totalWidth,totalHeight);
        mDrawingCache.unbind();
    }

    @Override
    public void renderer(int index, int width, int height, MD360Director director) {

        MDAbsObject3D object3D = mProjectionModeManager.getObject3D();
        // check obj3d
        if (object3D == null) return;

        // Update Projection
        director.setViewport(width, height);

        // Set our per-vertex lighting program.
        mBitmapProgram.use();
        glCheck("MDPanoramaPlugin mProgram use");

        // mTexture.texture(mProgram);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mDrawingCache.getTextureOutput());

        object3D.uploadVerticesBufferIfNeed(mBitmapProgram, index);
        object3D.uploadTexCoordinateBufferIfNeed(mBitmapProgram, index);

        // Pass in the combined matrix.
        director.beforeShot();
        director.shot(mBitmapProgram, getModelPosition());
        object3D.draw();
    }

    @Override
    public void destroyInGL() {
        mTexture = null;
    }

    @Override
    protected MDPosition getModelPosition() {
        return mProjectionModeManager.getModelPosition();
    }

    @Override
    protected boolean removable() {
        return false;
    }

    private void drawConverter(int width, int height){
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        glCheck("MDMultiFisheyeConvertLinePipe glClear");

        int itemWidth = width / 2;
        for (int index = 0; index < 2; index++){
            GLES20.glViewport(itemWidth * index, 0, itemWidth, height);
            GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
            GLES20.glScissor(itemWidth * index, 0, itemWidth, height);

            mProgram.use();
            mTexture.texture(mProgram);

            mFixedDirector.setViewport(itemWidth, height);
            mConverterObject3D.uploadVerticesBufferIfNeed(mProgram, index);
            mConverterObject3D.uploadTexCoordinateBufferIfNeed(mProgram, index);

            // Pass in the combined matrix.
            mFixedDirector.beforeShot();
            mFixedDirector.shot(mProgram, MDPosition.getOriginalPosition());

            mConverterObject3D.draw();

            GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
        }

    }

    private class MDMesh extends MDAbsObject3D {

        private static final String TAG = "MDMesh";

        private final MDDirection direction;

        private final float radius;

        public MDMesh(float radius, MDDirection direction) {
            this.radius = radius;
            this.direction = direction;
        }

        @Override
        protected void executeLoad(Context context) {
            generateMesh(this);
        }

        private void generateMesh(MDAbsObject3D object3D){
            final float PI = (float) Math.PI;
            int rows = 16;
            int columns = 16;
            int numPoint = (rows + 1) * (columns + 1);
            short r, s;
            float z = -8;
            float R = 1f/(float) rows;
            float S = 1f/(float) columns;

            float[] vertexs = new float[numPoint * 3];
            float[] texcoords = new float[numPoint * 2];
            float[] texcoords2 = new float[numPoint * 2];
            short[] indices = new short[numPoint * 6];

            int t = 0;
            int v = 0;
            for(r = 0; r < rows + 1; r++) {
                for(s = 0; s < columns + 1; s++) {

                    vertexs[v++] = (s * S * 2 - 1);
                    vertexs[v++] = (r * R * 2 - 1);
                    vertexs[v++] = z;

                    float FOV = 3.141592654f; // FOV of the fisheye, eg: 180 degrees
                    float width = 1;
                    float height = 1;

                    float theta = PI * (s * S - 0.5f); // -pi to pi
                    float phi = PI * (r * R - 0.5f);  // -pi/2 to pi/2

                    float psphx = (float) (Math.cos(phi) * Math.sin(theta));
                    float psphy = (float) (Math.cos(phi) * Math.cos(theta));
                    float psphz = (float) Math.sin(phi);

                    theta = (float) Math.atan2(psphz, psphx);
                    phi = (float) Math.atan2(Math.sqrt(psphx*psphx + psphz*psphz), psphy);
                    float rr = radius * phi / FOV;

                    float a = (float) (0.5f * width + rr * Math.cos(theta));
                    float b = (float) (0.5f * height + rr * Math.sin(theta));

                    if (direction == MDDirection.HORIZONTAL){
                        texcoords[t*2] = a * 0.5f;
                        texcoords[t*2 + 1] = b;
                        texcoords2[t*2] = a * 0.5f + 0.5f;
                        texcoords2[t*2 + 1] = b;
                    } else {
                        texcoords[t*2] = a;
                        texcoords[t*2 + 1] = b * 0.5f;
                        texcoords2[t*2] = a;
                        texcoords2[t*2 + 1] = b * 0.5f + 0.5f;
                    }


                    t++;

                }
            }



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
            ByteBuffer ee2 = ByteBuffer.allocateDirect(
                    texcoords2.length * 4);
            ee2.order(ByteOrder.nativeOrder());
            FloatBuffer texBuffer2 = ee2.asFloatBuffer();
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
            object3D.setTexCoordinateBuffer(0,texBuffer);
            object3D.setTexCoordinateBuffer(1,texBuffer2);
            object3D.setVerticesBuffer(0,vertexBuffer);
            object3D.setVerticesBuffer(1,vertexBuffer);
            object3D.setNumIndices(indices.length);
        }
    }
}
