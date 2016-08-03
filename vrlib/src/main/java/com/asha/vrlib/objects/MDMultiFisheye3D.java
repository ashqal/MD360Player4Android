package com.asha.vrlib.objects;

import android.content.Context;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by hzqiujiadi on 16/7/29.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDMultiFisheye3D extends MDAbsObject3D {

    private static final String TAG = "MDMultiFisheye3D";

    @Override
    protected void executeLoad(Context context) {
        generateSphere(18, 29, 30, this);
    }

    private static void generateSphere(float radius, int rings, int sectors, MDAbsObject3D object3D) {
        final float PI = (float) Math.PI;
        final float PI_2 = (float) (Math.PI / 2);

        float R = 1f/(float)rings;
        float S = 1f/(float)sectors;
        short r, s;
        float x, y, z;

        int numPoint = (rings + 1) * (sectors + 1);
        float[] vertexs = new float[numPoint * 3];
        float[] texcoords = new float[numPoint * 2];
        short[] indices = new short[numPoint * 6];

        int t = 0, v = 0;
        for(r = 0; r < rings + 1; r++) {
            for(s = 0; s < sectors + 1; s++) {
                x = (float) (Math.cos(2*PI * s * S) * Math.sin( PI * r * R ));
                y = - (float) Math.sin( -PI_2 + PI * r * R );
                z = (float) (Math.sin(2*PI * s * S) * Math.sin( PI * r * R ));

                vertexs[v++] = x * radius;
                vertexs[v++] = y * radius;
                vertexs[v++] = z * radius;


                if (t * 2 < numPoint){

                    float a = (float) (Math.sin( 2 * PI * s * S) * r * R * 2 * 0.65f) * 0.5f + 0.5f;
                    float b = (float) (Math.cos( 2 * PI * s * S) * r * R * 2 * 0.65f) * 0.5f + 0.5f;

                    texcoords[t*2] = a;
                    texcoords[t*2 + 1] = b * 0.5f;

                } else {
                    float a = (float) (Math.sin( 2 * PI * s * S) * (1 - r * R) * 2 * 0.65f) * 0.5f + 0.5f;
                    float b = (float) (Math.cos( 2 * PI * s * S) * (1 - r * R) * 2 * 0.65f) * 0.5f + 0.5f;

                    texcoords[t*2] = 1 - a;
                    texcoords[t*2 + 1] = b * 0.5f + 0.5f;
                }
                t++;
                /*
                if (t % 2 == 0){

                    texcoords[t + 1] = b * 0.5f;

                    texcoords[t + numPoint] = a;
                    texcoords[t + 1 + numPoint] = b * 0.5f + 0.5f;
                }
                */


            }
        }

        for (int k = 0; k < numPoint; k++){
            Log.e(TAG,String.format("p %d,",k));
            Log.e(TAG,String.format("v %d, x=%f y=%f z=%f",k,vertexs[k*3],vertexs[k*3+1],vertexs[k*3+2]));
            Log.e(TAG,String.format("t %d, x=%f y=%f",k,texcoords[k*2],texcoords[k*2+1]));
        }




        int counter = 0;
        int sectorsPlusOne = sectors + 1;
        for(r = 0; r < rings; r++){
            for(s = 0; s < sectors; s++) {
                indices[counter++] = (short) (r * sectorsPlusOne + s);       //(a)
                indices[counter++] = (short) ((r+1) * sectorsPlusOne + (s));    //(b)
                indices[counter++] = (short) ((r) * sectorsPlusOne + (s+1));  // (c)
                indices[counter++] = (short) ((r) * sectorsPlusOne + (s+1));  // (c)
                indices[counter++] = (short) ((r+1) * sectorsPlusOne + (s));    //(b)
                indices[counter++] = (short) ((r+1) * sectorsPlusOne + (s+1));  // (d)
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

}
