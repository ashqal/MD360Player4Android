package com.asha.vrlib.objects;

import android.content.Context;

import com.asha.vrlib.common.MDDirection;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by hzqiujiadi on 16/6/26.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDStereoSphere3D extends MDAbsObject3D {

    private MDDirection direction = MDDirection.HORIZONTAL;

    public MDStereoSphere3D(MDDirection direction) {
        this.direction = direction;
    }

    @Override
    protected void executeLoad(Context context) {
        generateSphere(this, direction);
    }

    private static void generateSphere(MDAbsObject3D object3D, MDDirection direction) {
        generateSphere(18, 75, 150, object3D, direction);
    }

    private static void generateSphere(float radius, int rings, int sectors, MDAbsObject3D object3D, MDDirection direction) {
        final float PI = (float) Math.PI;
        final float PI_2 = (float) (Math.PI / 2);

        float R = 1f/(float)rings;
        float S = 1f/(float)sectors;
        short r, s;
        float x, y, z;

        int numPoint = (rings + 1) * (sectors + 1);
        float[] vertexs = new float[numPoint * 3];
        float[] texcoords = new float[numPoint * 2];
        float[] texcoords2 = new float[numPoint * 2];
        short[] indices = new short[numPoint * 6];

        int t = 0, v = 0;
        for(r = 0; r < rings + 1; r++) {
            for(s = 0; s < sectors + 1; s++) {
                x = (float) (Math.cos(2*PI * s * S) * Math.sin( PI * r * R ));
                y = - (float) Math.sin( -PI_2 + PI * r * R );
                z = (float) (Math.sin(2*PI * s * S) * Math.sin( PI * r * R ));

                if (MDDirection.VERTICAL == direction){
                    texcoords[t] = s*S;
                    texcoords2[t] = s*S;
                    t++;

                    texcoords[t] = 1 - r*R/2;
                    texcoords2[t] = 0.5f - r*R/2;
                    t++;
                } else {
                    texcoords[t] = s*S/2;
                    texcoords2[t] = s*S/2 + 0.5f;
                    t++;

                    texcoords[t] = 1 - r*R;
                    texcoords2[t] = 1 - r*R;
                    t++;
                }

                vertexs[v++] = x * radius;
                vertexs[v++] = y * radius;
                vertexs[v++] = z * radius;
            }
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

        // initialize vertex2 byte buffer for shape coordinates
        ByteBuffer cc2 = ByteBuffer.allocateDirect(
                texcoords.length * 4);
        cc2.order(ByteOrder.nativeOrder());
        FloatBuffer texBuffer2 = cc2.asFloatBuffer();
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
