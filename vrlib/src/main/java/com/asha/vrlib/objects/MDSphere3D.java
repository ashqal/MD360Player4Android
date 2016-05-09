package com.asha.vrlib.objects;

import android.content.Context;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by hzqiujiadi on 16/1/8.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDSphere3D extends MDAbsObject3D {

    @Override
    protected void executeLoad(Context context) {
        generateSphere(this);
    }

    private static void generateSphere(MDAbsObject3D object3D) {
        generateSphere(18,75,150,object3D);
    }

    /****
     * copied from https://github.com/shulja/viredero/blob/a7d28b21d762e8479dc10cde1aa88054497ff649/viredroid/src/main/java/org/viredero/viredroid/Sphere.java
     * */
    private static void generateSphere(float radius, int rings, int sectors, MDAbsObject3D object3D) {
        final float PI = (float) Math.PI;
        final float PI_2 = (float) (Math.PI / 2);
        float R = 1f/(float)(rings-1);
        float S = 1f/(float)(sectors-1);
        short r, s;
        float x, y, z;

        float[] points = new float[rings * sectors * 3];
        float[] texcoords = new float[rings * sectors * 2];

        int t = 0, v = 0, n = 0;
        for(r = 0; r < rings; r++) {
            for(s = 0; s < sectors; s++) {
                x = (float) (Math.cos(2*PI * s * S) * Math.sin( PI * r * R ));
                y = - (float) Math.sin( -PI_2 + PI * r * R );
                z = (float) (Math.sin(2*PI * s * S) * Math.sin( PI * r * R ));

                texcoords[t++] = s*S;
                texcoords[t++] = r*R;

                points[v++] = x * radius;
                points[v++] = y * radius;
                points[v++] = z * radius;
            }
        }
        int counter = 0;
        short[] indices = new short[rings * sectors * 6];
        for(r = 0; r < rings - 1; r++){
            for(s = 0; s < sectors-1; s++) {
                indices[counter++] = (short) (r * sectors + s);       //(a)
                indices[counter++] = (short) ((r+1) * sectors + (s));    //(b)
                indices[counter++] = (short) ((r+1) * sectors + (s+1));  // (c)
                indices[counter++] = (short) ((r) * sectors + (s));  // (a)
                indices[counter++] = (short) ((r) * sectors + (s+1));     //(d)
                indices[counter++] = (short) ((r+1) * sectors + (s+1));    //(c)

            }
        }
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                points.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(points);
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
        object3D.setTexCoordinateBuffer(texBuffer);
        object3D.setVerticesBuffer(vertexBuffer);
        object3D.setNumIndices(indices.length);
    }
}