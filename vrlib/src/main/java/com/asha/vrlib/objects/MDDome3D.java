package com.asha.vrlib.objects;


import android.content.Context;
import android.graphics.RectF;

import com.asha.vrlib.MD360Program;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by hzqiujiadi on 16/1/22.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDDome3D extends MDAbsObject3D {

    float mDegree;

    boolean mIsUpper;

    RectF mTextureSize;

    float mPrevRatio = 1;

    float[] texCoordinates;

    public MDDome3D(RectF textureSize, float degree, boolean isUpper) {
        this.mTextureSize = textureSize;
        this.mDegree = degree;
        this.mIsUpper = isUpper;
    }

    @Override
    public void uploadTexCoordinateBufferIfNeed(MD360Program program, int index) {
        if (super.getTexCoordinateBuffer(index) == null){
            return;
        }

        if (index == 0){
            float ratio = mTextureSize.width() / mTextureSize.height();
            if (ratio != mPrevRatio){
                int size = texCoordinates.length;
                float[] tmp = new float[size];
                for (int i = 0; i < size; i += 2){
                    tmp[i] = (texCoordinates[i]- 0.5f)/ratio + 0.5f;
                    tmp[i+1] = texCoordinates[i+1];
                }

                ByteBuffer cc = ByteBuffer.allocateDirect(
                        tmp.length * 4);
                cc.order(ByteOrder.nativeOrder());
                FloatBuffer buffer = cc.asFloatBuffer();
                buffer.put(tmp);
                buffer.position(0);
                setTexCoordinateBuffer(0,buffer);
                setTexCoordinateBuffer(1,buffer);
                mPrevRatio = ratio;
            }
        }

        super.uploadTexCoordinateBufferIfNeed(program, index);
    }

    @Override
    protected void executeLoad(Context context) {
        generateDome(mDegree, mIsUpper, this);
    }

    private static void generateDome(float degree, boolean isUpper, MDDome3D object3D) {
        generateDome(18, 150, degree, isUpper, object3D);
    }

    public static void generateDome(float radius, int sectors, float degreeY, boolean isUpper, MDDome3D object3D) {
        final float PI = (float) Math.PI;
        final float PI_2 = (float) (Math.PI / 2);

        float percent = degreeY / 360;
        int rings = sectors >> 1;

        float R = 1f/rings;
        float S = 1f/sectors;
        short r, s;
        float x, y, z;

        int lenRings = (int) (rings * percent) + 1;
        int lenSectors = sectors + 1;
        int numPoint = lenRings * lenSectors;

        float[] vertexs = new float[numPoint * 3];
        float[] texcoords = new float[numPoint * 2];
        short[] indices = new short[numPoint * 6];

        int upper = isUpper ? 1 : -1;

        int t = 0, v = 0;
        for(r = 0; r < lenRings; r++) {
            for(s = 0; s < lenSectors; s++) {
                x = (float) (Math.cos( 2 * PI * s * S ) * Math.sin( PI * r * R )) * upper;
                y = (float) Math.sin( -PI_2 + PI * r * R ) * -upper;
                z = (float) (Math.sin( 2 * PI * s * S ) * Math.sin( PI * r * R ));

                float a = (float) (Math.cos( 2 * PI * s * S) * r * R / percent)/2.0f + 0.5f;
                float b = (float) (Math.sin( 2 * PI * s * S) * r * R / percent)/2.0f + 0.5f;

                texcoords[t++] = b;
                texcoords[t++] = a;

                vertexs[v++] = x * radius;
                vertexs[v++] = y * radius;
                vertexs[v++] = z * radius;
            }
        }

        int counter = 0;
        for(r = 0; r < lenRings - 1; r++){
            for(s = 0; s < lenSectors - 1; s++) {
                indices[counter++] = (short) (r * lenSectors + s);       //(a)
                indices[counter++] = (short) ((r+1) * lenSectors + (s));    //(b)
                indices[counter++] = (short) ((r) * lenSectors + (s+1));  // (c)
                indices[counter++] = (short) ((r) * lenSectors + (s+1));  // (c)
                indices[counter++] = (short) ((r+1) * lenSectors + (s));    //(b)
                indices[counter++] = (short) ((r+1) * lenSectors + (s+1));  // (d)
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

        object3D.texCoordinates = texcoords;
    }
}
