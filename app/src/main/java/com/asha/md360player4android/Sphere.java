package com.asha.md360player4android;

/**
 * Created by hzqiujiadi on 16/1/8.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class Sphere{
    public float[] vertices;
    public float[] texCoords;
    public int[] indices;
    public int numIndices;
    public int numVertices;
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
        return numIndices;
    }
}