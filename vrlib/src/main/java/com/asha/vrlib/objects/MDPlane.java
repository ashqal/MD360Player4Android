package com.asha.vrlib.objects;

import android.content.Context;
import android.graphics.RectF;

import com.asha.vrlib.MD360Program;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.strategy.projection.PlaneProjection;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by hzqiujiadi on 16/6/26.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDPlane extends MDAbsObject3D {

    private static final String TAG = "MDPlane";

    private float mPrevRatio;

    private RectF mSize;

    private PlaneProjection.PlaneScaleCalculator mCalculator;

    private MDPlane(PlaneProjection.PlaneScaleCalculator calculator, RectF size) {
        this.mCalculator = calculator;
        this.mSize = size;
    }

    public MDPlane(PlaneProjection.PlaneScaleCalculator calculator) {
        this(calculator,new RectF(0,0,1.0f,1.0f));
    }

    public MDPlane(RectF size) {
        this(new PlaneProjection.PlaneScaleCalculator(MDVRLibrary.PROJECTION_MODE_PLANE_FULL,new RectF(0,0,100,100)),size);
    }

    @Override
    protected void executeLoad(Context context) {
        generateMesh(this);
    }

    @Override
    public void uploadVerticesBufferIfNeed(MD360Program program, int index) {
        if (super.getVerticesBuffer(index) == null){
            return;
        }

        // update the texture only if the index == 0
        if (index == 0){
            float ratio = mCalculator.getTextureRatio();
            if (ratio != mPrevRatio) {

                float[] vertexs = generateVertex();

                // initialize vertex byte buffer for shape coordinates
                ByteBuffer bb = ByteBuffer.allocateDirect(
                        // (# of coordinate values * 4 bytes per float)
                        vertexs.length * 4);
                bb.order(ByteOrder.nativeOrder());
                FloatBuffer buffer = bb.asFloatBuffer();
                buffer.put(vertexs);
                buffer.position(0);

                setVerticesBuffer(0,buffer);
                setVerticesBuffer(1,buffer);

                mPrevRatio = ratio;
            }
        }

        super.uploadVerticesBufferIfNeed(program, index);
    }

    private float[] generateVertex(){
        int z = 0;
        mCalculator.calculate();
        mPrevRatio = mCalculator.getTextureRatio();
        float width = mCalculator.getTextureWidth() * mSize.width();
        float height = mCalculator.getTextureHeight() * mSize.height();

        float[] vertexs = new float[getNumPoint() * 3];
        int rows = getNumRow();
        int columns = getNumColumn();
        float R = 1f/(float) rows;
        float S = 1f/(float) columns;
        short r, s;

        int v = 0;
        for(r = 0; r < rows + 1; r++) {
            for(s = 0; s < columns + 1; s++) {
                vertexs[v++] = (s * S - 0.5f) * width;
                vertexs[v++] = (r * R - 0.5f) * height;
                vertexs[v++] = z;
            }
        }

        return vertexs;
    }

    private float[] generateTexcoords(){
        float[] texcoords = new float[getNumPoint() * 2];

        int rows = getNumRow();
        int columns = getNumColumn();
        float R = 1f/(float) rows;
        float S = 1f/(float) columns;
        short r, s;

        int t = 0;
        for(r = 0; r < rows + 1; r++) {
            for(s = 0; s < columns + 1; s++) {
                texcoords[t++] = s*S;
                texcoords[t++] = r*R;
            }
        }

        return texcoords;
    }

    private void generateMesh(MDAbsObject3D object3D){
        int rows = getNumRow();
        int columns = getNumColumn();
        short r, s;

        float[] vertexs = generateVertex();
        float[] texcoords = generateTexcoords();
        short[] indices = new short[getNumPoint() * 6];


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
        ByteBuffer cc = ByteBuffer.allocateDirect(
                texcoords.length * 4);
        cc.order(ByteOrder.nativeOrder());
        FloatBuffer texBuffer = cc.asFloatBuffer();
        texBuffer.put(texcoords);
        texBuffer.position(0);

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
        object3D.setTexCoordinateBuffer(1,texBuffer);
        object3D.setVerticesBuffer(0,vertexBuffer);
        object3D.setVerticesBuffer(1,vertexBuffer);
        object3D.setNumIndices(indices.length);
    }

    private int getNumPoint(){
        return (getNumRow() + 1) * (getNumColumn() + 1);
    }

    private int getNumRow(){
        return 1;
    }

    private int getNumColumn(){
        return 1;
    }
}
