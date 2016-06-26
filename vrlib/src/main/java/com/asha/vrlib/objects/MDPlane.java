package com.asha.vrlib.objects;

import android.content.Context;

import com.asha.vrlib.MD360Program;
import com.asha.vrlib.strategy.projection.PlaneProjection;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by hzqiujiadi on 16/6/26.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDPlane extends MDAbsObject3D {

    private static final String TAG = "MDPlane";
    private float mPrevRatio;

    private FloatBuffer mScaledVerticesBuffer;

    private PlaneProjection.PlaneScaleCalculator mCalculator;
    private static final int sNumPoint = 6;

    public MDPlane(PlaneProjection.PlaneScaleCalculator calculator) {
        this.mCalculator = calculator;
    }

    @Override
    protected void executeLoad(Context context) {
        generatePlane(this);
    }

    @Override
    public void uploadVerticesBufferIfNeed(MD360Program program, int index) {
        if (super.getVerticesBuffer(index) == null){
            return;
        }
        float ratio = mCalculator.getTextureRatio();
        if (ratio == mPrevRatio) {
            mScaledVerticesBuffer = super.getVerticesBuffer(index);
        } else {
            float[] vertexs = generateVertex();

            // initialize vertex byte buffer for shape coordinates
            ByteBuffer bb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 4 bytes per float)
                    vertexs.length * 4);
            bb.order(ByteOrder.nativeOrder());
            mScaledVerticesBuffer = bb.asFloatBuffer();
            mScaledVerticesBuffer.put(vertexs);
            mScaledVerticesBuffer.position(0);
            markVerticesChanged();
        }

        super.uploadVerticesBufferIfNeed(program, index);
    }

    @Override
    public FloatBuffer getVerticesBuffer(int index) {
        return mScaledVerticesBuffer;
    }

    private float[] generateVertex(){
        int numPoint = sNumPoint;
        int z = -8;

        mCalculator.calculate();
        mPrevRatio = mCalculator.getTextureRatio();
        float width = mCalculator.getTextureWidth();
        float height = mCalculator.getTextureHeight();

        float[] vertexs = new float[numPoint * 3];
        int i = 0;
        vertexs[i*3] = width;
        vertexs[i*3 + 1] = -height;
        vertexs[i*3 + 2] = z;
        i++;

        vertexs[i*3] = -width;
        vertexs[i*3 + 1] = height;
        vertexs[i*3 + 2] = z;
        i++;

        vertexs[i*3] = -width;
        vertexs[i*3 + 1] = -height;
        vertexs[i*3 + 2] = z;
        i++;

        vertexs[i*3] = width;
        vertexs[i*3 + 1] = -height;
        vertexs[i*3 + 2] = z;
        i++;

        vertexs[i*3] = width;
        vertexs[i*3 + 1] = height;
        vertexs[i*3 + 2] = z;
        i++;

        vertexs[i*3] = -width;
        vertexs[i*3 + 1] = height;
        vertexs[i*3 + 2] = z;
        i++;

        return vertexs;

    }

    private static float[] generateTexcoords(){
        int numPoint = 6;
        float[] texcoords = new float[numPoint * 2];

        int i = 0;
        texcoords[i*2] = 1;
        texcoords[i*2 + 1] = 1;
        i++;

        texcoords[i*2] = 0;
        texcoords[i*2 + 1] = 0;
        i++;

        texcoords[i*2] = 0;
        texcoords[i*2 + 1] = 1;
        i++;

        texcoords[i*2] = 1;
        texcoords[i*2 + 1] = 1;
        i++;

        texcoords[i*2] = 1;
        texcoords[i*2 + 1] = 0;
        i++;

        texcoords[i*2] = 0;
        texcoords[i*2 + 1] = 0;
        i++;

        return texcoords;
    }

    private void generatePlane(MDAbsObject3D object3D) {
        int numPoint = sNumPoint;

        float[] texcoords = generateTexcoords();
        float[] vertexs = generateVertex();

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                vertexs.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertexs);
        vertexBuffer.position(0);

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer cc = ByteBuffer.allocateDirect(
                texcoords.length * 4);
        cc.order(ByteOrder.nativeOrder());
        FloatBuffer texBuffer = cc.asFloatBuffer();
        texBuffer.put(texcoords);
        texBuffer.position(0);

        object3D.setTexCoordinateBuffer(texBuffer);
        object3D.setVerticesBuffer(vertexBuffer);
        object3D.setNumIndices(numPoint);
    }
}
