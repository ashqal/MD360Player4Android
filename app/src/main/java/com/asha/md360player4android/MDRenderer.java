package com.asha.md360player4android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by hzqiujiadi on 16/1/7.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDRenderer {
    private static final String TAG = "MDRenderer";
    private int[] uniforms = new int[UNIFORM.NUM_UNIFORMS.ordinal()];
    private int vertexTexCoordAttributeIndex;
    private MDGLProgram program;
    private Sphere sphere;
    IntBuffer vertexArrayId;
    IntBuffer vertexBufferId;
    IntBuffer vertexTexCoordId;
    IntBuffer vertexIndicesBufferId;

    public enum UNIFORM {
        UNIFORM_MODELVIEWPROJECTION_MATRIX,
        UNIFORM_Y,
        UNIFORM_UV,
        UNIFORM_COLOR_CONVERSION_MATRIX,
        NUM_UNIFORMS
    }

    static float kColorConversion709[] = {
            1.164f, 1.164f, 1.164f,
            0.0f, -0.213f, 2.112f,
            1.793f, -0.533f, 0.0f};

    public enum NS_ENUM {
        GLKVertexAttribPosition,
                GLKVertexAttribNormal,
                GLKVertexAttribColor,
                GLKVertexAttribTexCoord0,
                GLKVertexAttribTexCoord1
    }

    public void draw(Context context){
        program.use();

        GLES30.glBindVertexArray(vertexArrayId.get(0));
        GLES20.glUniformMatrix4fv(uniforms[UNIFORM.UNIFORM_MODELVIEWPROJECTION_MATRIX.ordinal()], 1, false, modelViewProjectionMatrix,0);

        // obtain video buffer
        // ..

        final BitmapFactory.Options options = new BitmapFactory.Options();

        // Read in the resource
        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.demo, options);


        int width = 0;
        int height = 0;
        Buffer buffer = ByteBuffer.allocate(1);

        // texture
        IntBuffer textures = IntBuffer.allocate(2);
        GLES20.glGenTextures(2, textures);

        // Y-plane
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textures.get(0));
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        // lumaTexture target, name
        // GLES20.glBindTexture(CVOpenGLESTextureGetTarget(_lumaTexture), CVOpenGLESTextureGetName(_lumaTexture));
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        //GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES30.GL_RED, width, height, 0, GLES30.GL_RED, GLES20.GL_UNSIGNED_BYTE, buffer);

        bitmap.recycle();
        /*

        // UV-plane.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textures.get(1));
        // chromaTexture target, name
        // glBindTexture(CVOpenGLESTextureGetTarget(_chromaTexture), CVOpenGLESTextureGetName(_chromaTexture));

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        //GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES30.GL_RG, width, height, 0, GLES30.GL_RG, GLES20.GL_UNSIGNED_BYTE, buffer);

        */


        // done
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glDrawElements ( GLES20.GL_TRIANGLES, sphere.numIndices,
                GLES20.GL_UNSIGNED_SHORT, 0 );

    }

    private static final float MAX_OVERTURE = 95.0f;
    private static final float MIN_OVERTURE = 25.0f;
    private static final float DEFAULT_OVERTURE = 85.0f;

    private float GLKMathDegreesToRadians(float degrees) {
        return (float) (degrees * (Math.PI / 180));
    }

    private float GLKMathRadiansToDegrees(float angle) {
        return (float) (angle * 180 / Math.PI );
    }

    private void GLKMatrix4MakePerspective(float fovyRadians, float aspect, float nearZ, float farZ, float[] out) {
        float cotan = (float) (1.0f / Math.tan(fovyRadians / 2.0f));
        out[0] = cotan / aspect;
        out[1] = 0.0f;
        out[2] = 0.0f;
        out[3] = 0.0f;

        out[4] = 0.0f;
        out[5] = cotan;
        out[6] = 0.0f;
        out[7] = 0.0f;

        out[8] = 0.0f;
        out[9] = 0.0f;
        out[10] = (farZ + nearZ) / (nearZ - farZ);
        out[11] = -1.0f;

        out[12] = 0.0f;
        out[13] = 0.0f;
        out[14] = (2.0f * farZ * nearZ) / (nearZ - farZ);
        out[15] = 0.0f;
    }

    private void GLKMatrix4Identity(float[] out){
        out[0] = 1.0f;
        out[1] = 0.0f;
        out[2] = 0.0f;
        out[3] = 0.0f;

        out[4] = 0.0f;
        out[5] = 1.0f;
        out[6] = 0.0f;
        out[7] = 0.0f;

        out[8] = 0.0f;
        out[9] = 0.0f;
        out[10] = 1.0f;
        out[11] = 0.0f;

        out[12] = 0.0f;
        out[13] = 0.0f;
        out[14] = 0.0f;
        out[15] = 1.0f;
    }

    private float[] GLKMatrix4Scale(float[] m, float sx, float sy, float sz){
        float[] a = {m[0] * sx,m[1] * sx, m[2] * sx, m[3] * sx,
                m[4] * sy, m[5] * sy, m[6] * sy, m[7] * sy,
                m[8] * sz, m[9] * sz, m[10] * sz, m[11] * sz,
                m[12], m[13], m[14], m[15]};
        return a;
    }

    private void GLKMatrix4Multiply(float[] matrixLeft, float[] matrixRight, float[] out){
        Matrix.multiplyMM(out,0,matrixLeft,0,matrixRight,0);
    }

    private void GLKMatrix4Rotate(float[] matrix, float radians, float x, float y, float z){
        Matrix.rotateM(matrix,0,GLKMathRadiansToDegrees(radians),x,y,z);
    }

    private float[] projectionMatrix = new float[16];
    private float[] modelViewMatrix = new float[16];
    private float[] modelViewProjectionMatrix = new float[16];

    public void update(){
        float aspect = 1.5f;
        GLKMatrix4MakePerspective(GLKMathDegreesToRadians(DEFAULT_OVERTURE),aspect, 0.1f, 400.0f,projectionMatrix);
        GLKMatrix4Rotate(projectionMatrix, (float) Math.PI, 1.0f, 0.0f, 0.0f);

        GLKMatrix4Identity(modelViewMatrix);
        modelViewMatrix = GLKMatrix4Scale(modelViewMatrix, 300.0f, 300.0f, 300.0f);

        GLKMatrix4Multiply(projectionMatrix,modelViewMatrix,modelViewProjectionMatrix);
    }

    public void setupGL(Context context){
        program = buildProgram(context);
        sphere = new Sphere(200,1.0f);
        int numVertices = sphere.numVertices;
        int numIndices = sphere.numIndices;
        Buffer vertices = FloatBuffer.wrap(sphere.vertices);
        Buffer textCoord = FloatBuffer.wrap(sphere.texCoords);
        Buffer indices = IntBuffer.wrap(sphere.indices);

        // GLES3.0
        vertexArrayId = IntBuffer.allocate(1);
        GLES30.glGenVertexArrays(1,vertexArrayId);
        GLES30.glBindVertexArray(vertexArrayId.get(0));

        // Vertex
        vertexBufferId = IntBuffer.allocate(1);
        GLES20.glGenBuffers(1,vertexBufferId);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,vertexBufferId.get(0));
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, numVertices, vertices, GLES20.GL_STATIC_DRAW);
        GLES20.glEnableVertexAttribArray(NS_ENUM.GLKVertexAttribPosition.ordinal());
        GLES20.glVertexAttribPointer(NS_ENUM.GLKVertexAttribPosition.ordinal(),3,GLES20.GL_FLOAT,false,3,0);

        // Texture Coordinates
        vertexTexCoordId = IntBuffer.allocate(1);
        GLES20.glGenBuffers(1,vertexTexCoordId);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,vertexTexCoordId.get(0));
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,numVertices*2,textCoord,GLES20.GL_DYNAMIC_DRAW);
        GLES20.glEnableVertexAttribArray(vertexTexCoordAttributeIndex);
        GLES20.glVertexAttribPointer(vertexTexCoordAttributeIndex,2,GLES20.GL_FLOAT,false,2,0);

        // Indices
        vertexIndicesBufferId = IntBuffer.allocate(1);
        GLES20.glGenBuffers(1,vertexIndicesBufferId);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER,vertexIndicesBufferId.get(0));
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER,numIndices,indices,GLES20.GL_STATIC_DRAW);

        // CVOpenGLESTextureCacheCreate
        // TODO: 16/1/7  CVOpenGLESTextureCacheCreate

        FloatBuffer preferredConversion = FloatBuffer.wrap(kColorConversion709);
        program.use();
        GLES20.glUniform1i(uniforms[UNIFORM.UNIFORM_Y.ordinal()],0);
        GLES20.glUniform1i(uniforms[UNIFORM.UNIFORM_UV.ordinal()],1);
        GLES20.glUniformMatrix3fv(uniforms[UNIFORM.UNIFORM_COLOR_CONVERSION_MATRIX.ordinal()],1,false,preferredConversion);

    }

    private MDGLProgram buildProgram(Context context){
        MDGLProgram program = new MDGLProgram();
        program.init(context);
        program.addAttribute("position");
        program.addAttribute("texCoord");

        if (!program.link()){
            Log.e(TAG,"program link failed!");
        }

        vertexTexCoordAttributeIndex = program.indexOfAttribute("texCoord");

        uniforms[UNIFORM.UNIFORM_MODELVIEWPROJECTION_MATRIX.ordinal()] = program.indexOfUniform("modelViewProjectionMatrix");
        uniforms[UNIFORM.UNIFORM_Y.ordinal()] = program.indexOfUniform("SamplerY");
        uniforms[UNIFORM.UNIFORM_UV.ordinal()] = program.indexOfUniform("SamplerUV");
        uniforms[UNIFORM.UNIFORM_COLOR_CONVERSION_MATRIX.ordinal()] = program.indexOfUniform("colorConversionMatrix");

        return program;

    }

    private static class Sphere{
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
                    indices[i*6]  = i * ( numSlices + 1 ) + j;
                    indices[i*6+1] = ( i + 1 ) * ( numSlices + 1 ) + j;
                    indices[i*6+2] = ( i + 1 ) * ( numSlices + 1 ) + ( j + 1 );

                    indices[i*6+3] = i * ( numSlices + 1 ) + j;
                    indices[i*6+4] = ( i + 1 ) * ( numSlices + 1 ) + ( j + 1 );
                    indices[i*6+5] = i * ( numSlices + 1 ) + ( j + 1 );
                }
            }
            return numIndices;
        }
    }

}
