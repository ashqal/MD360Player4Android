package com.asha.md360player4android;

import android.content.Context;

import com.asha.md360player4android.common.MeshBufferHelper;
import com.asha.md360player4android.common.WaveFrontObjHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by hzqiujiadi on 16/1/8.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class Sphere{
    public float[] vertices;
    public float[] texCoords;
    public int[] indices;
    public float[] colors;
    private FloatBuffer verticesBuffer;
    private FloatBuffer texCoordsBuffer;
    private IntBuffer indicesBuffer;
    private FloatBuffer colorsBuffer;

    public int numIndices;
    public int numVertices;
    private MeshBufferHelper meshBufferHelper;

    public Sphere(Context context){
        meshBufferHelper = WaveFrontObjHelper.loadObj(context, R.raw.sphere);
        numIndices = meshBufferHelper.getCount();
        FloatBuffer[] mMeshBuffer = meshBufferHelper.getBuffer();
        verticesBuffer = mMeshBuffer[0];
        texCoordsBuffer = mMeshBuffer[1];
    }

    public Sphere(int numSlices, float radius) {
        createSphere(numSlices,radius);
    }

    private int createSphere(int numSlices, float radius){
        int i,j;
        int numParallels = numSlices >> 1;
        float angleStep = (float) ((2.0f * Math.PI) / (float) numSlices);
        numVertices = (numParallels + 1) * (numSlices + 1);
        numIndices = numParallels * numSlices * 6;
        vertices = new float[3 * numVertices];
        texCoords = new float[2 * numVertices];
        colors = new float[4 * numVertices];
        indices = new int[numIndices];

        for ( i = 0; i < numParallels + 1; i++ ){
            for ( j = 0; j < numSlices + 1; j++ ){
                int vertex = ( i * (numSlices + 1) + j ) * 3;
                vertices[vertex] = (float) (radius * Math.sin(angleStep*i) * Math.sin(angleStep*j));
                vertices[vertex + 1] = (float) (radius * Math.cos(angleStep*i));
                vertices[vertex + 2] = (float) (radius * Math.sin(angleStep*i) * Math.cos(angleStep*j));

                int texIndex = ( i * (numSlices + 1) + j ) * 2;
                texCoords[texIndex] = (float) j / (float) numSlices;
                texCoords[texIndex + 1] = 1.0f - ((float) i / (float) (numParallels));

                // RGBA
                int colorIndex = ( i * (numSlices + 1) + j ) * 4;
                colors[colorIndex] = 1.0f;
                colors[colorIndex+1] = 1.0f;
                colors[colorIndex+2] = 1.0f;
                colors[colorIndex+3] = 1.0f;

            }
        }

        for ( i = 0; i < numParallels ; i++ ) {
            for ( j = 0; j < numSlices; j++ ) {
                indices[i*numSlices + j*6]  = i * ( numSlices + 1 ) + j;
                indices[i*numSlices + j*6+1] = ( i + 1 ) * ( numSlices + 1 ) + j;
                indices[i*numSlices + j*6+2] = ( i + 1 ) * ( numSlices + 1 ) + ( j + 1 );

                indices[i*numSlices + j*6+3] = i * ( numSlices + 1 ) + j;
                indices[i*numSlices + j*6+4] = ( i + 1 ) * ( numSlices + 1 ) + ( j + 1 );
                indices[i*numSlices + j*6+5] = i * ( numSlices + 1 ) + ( j + 1 );
            }
        }

        verticesBuffer = ByteBuffer.allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        verticesBuffer.put(vertices).position(0);

        texCoordsBuffer = ByteBuffer.allocateDirect(texCoords.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        texCoordsBuffer.put(texCoords).position(0);

        indicesBuffer = ByteBuffer.allocateDirect(indices.length * 4)
                .order(ByteOrder.nativeOrder()).asIntBuffer();
        indicesBuffer.put(indices).position(0);

        colorsBuffer = ByteBuffer.allocateDirect(colors.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        colorsBuffer.put(colors).position(0);

        return numIndices;
    }

    public FloatBuffer getVerticesBuffer() {
        //verticesBuffer = FloatBuffer.wrap(vertices);
        return verticesBuffer;
    }

    public FloatBuffer getTexCoordsBuffer() {
        //texCoordsBuffer = FloatBuffer.wrap(texCoords);
        return texCoordsBuffer;
    }

    public IntBuffer getIndicesBuffer() {
        //indicesBuffer = IntBuffer.wrap(indices);
        return indicesBuffer;
    }

    public FloatBuffer getColorsBuffer() {
        //colorsBuffer = FloatBuffer.wrap(colors);
        return colorsBuffer;
    }
}