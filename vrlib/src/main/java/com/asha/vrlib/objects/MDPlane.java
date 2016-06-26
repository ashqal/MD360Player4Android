package com.asha.vrlib.objects;

import android.content.Context;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by hzqiujiadi on 16/6/26.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDPlane extends MDAbsObject3D {

    @Override
    protected void executeLoad(Context context) {
        generatePlane(this);
        //GLUtil.loadObject3D(context, R.raw.plane, this);
    }

    private static void generatePlane(MDAbsObject3D object3D) {
        int numPoint = 6;
        int width = 10;
        int height = 8;
        int z = -8;
        float[] vertexs = new float[numPoint * 3];
        float[] texcoords = new float[numPoint * 2];

        int i = 0;
        texcoords[i*2] = 1;
        texcoords[i*2 + 1] = 1;

        vertexs[i*3] = width;
        vertexs[i*3 + 1] = -height;
        vertexs[i*3 + 2] = z;
        i++;

        texcoords[i*2] = 0;
        texcoords[i*2 + 1] = 0;

        vertexs[i*3] = -width;
        vertexs[i*3 + 1] = height;
        vertexs[i*3 + 2] = z;
        i++;

        texcoords[i*2] = 0;
        texcoords[i*2 + 1] = 1;

        vertexs[i*3] = -width;
        vertexs[i*3 + 1] = -height;
        vertexs[i*3 + 2] = z;
        i++;

        texcoords[i*2] = 1;
        texcoords[i*2 + 1] = 1;

        vertexs[i*3] = width;
        vertexs[i*3 + 1] = -height;
        vertexs[i*3 + 2] = z;
        i++;

        texcoords[i*2] = 1;
        texcoords[i*2 + 1] = 0;

        vertexs[i*3] = width;
        vertexs[i*3 + 1] = height;
        vertexs[i*3 + 2] = z;
        i++;

        texcoords[i*2] = 0;
        texcoords[i*2 + 1] = 0;

        vertexs[i*3] = -width;
        vertexs[i*3 + 1] = height;
        vertexs[i*3 + 2] = z;
        i++;


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
