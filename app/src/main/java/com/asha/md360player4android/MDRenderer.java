package com.asha.md360player4android;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.util.Log;

import com.asha.md360player4android.common.TextureHelper;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static com.asha.md360player4android.common.GLKMatrixUtil.GLKMathDegreesToRadians;
import static com.asha.md360player4android.common.GLKMatrixUtil.GLKMatrix4Identity;
import static com.asha.md360player4android.common.GLKMatrixUtil.GLKMatrix4MakePerspective;
import static com.asha.md360player4android.common.GLKMatrixUtil.GLKMatrix4Multiply;
import static com.asha.md360player4android.common.GLKMatrixUtil.GLKMatrix4Rotate;
import static com.asha.md360player4android.common.GLKMatrixUtil.GLKMatrix4Scale;

/**
 * Created by hzqiujiadi on 16/1/7.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDRenderer {
    private static final String TAG = "MDRenderer";
    private int[] uniforms = new int[UNIFORM.NUM_UNIFORMS.ordinal()];
    private int vertexTexCoordAttributeIndex;
    private int vertexAttributeIndex;
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

    int texture;
    public void draw(Context context){
        //update();

        program.use();

        GLES30.glBindVertexArray(vertexArrayId.get(0));
        GLES20.glUniformMatrix4fv(uniforms[UNIFORM.UNIFORM_MODELVIEWPROJECTION_MATRIX.ordinal()], 1, false, modelViewProjectionMatrix,0);

        // obtain video buffer
        // ..

        // Read in the resource
        //final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.demo, options);


        int width = 0;
        int height = 0;
        Buffer buffer = ByteBuffer.allocate(1);

        // texture
        if ( texture == 0 ){
            texture = TextureHelper.loadTexture(context,R.drawable.demo);
        }

        //IntBuffer textures = IntBuffer.allocate(2);
        //GLES20.glGenTextures(2, textures);

        // Y-plane
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);

        //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textures.get(0));
        //GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        // lumaTexture target, name
        // GLES20.glBindTexture(CVOpenGLESTextureGetTarget(_lumaTexture), CVOpenGLESTextureGetName(_lumaTexture));
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        //GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES30.GL_RED, width, height, 0, GLES30.GL_RED, GLES20.GL_UNSIGNED_BYTE, buffer);

        //bitmap.recycle();
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
        Buffer vertices = sphere.getVerticesBuffer();
        Buffer textCoord = sphere.getTexCoordsBuffer();
        Buffer indices = sphere.getIndicesBuffer();

        // GLES3.0
        vertexArrayId = IntBuffer.allocate(1);
        GLES30.glGenVertexArrays(1,vertexArrayId);
        GLES30.glBindVertexArray(vertexArrayId.get(0));

        // Vertex
        vertexBufferId = IntBuffer.allocate(1);
        GLES20.glGenBuffers(1,vertexBufferId);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,vertexBufferId.get(0));
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, numVertices*3*4, vertices, GLES20.GL_STATIC_DRAW);
        GLES20.glVertexAttribPointer(vertexAttributeIndex,3,GLES20.GL_FLOAT,false,0,0);
        GLES20.glEnableVertexAttribArray(vertexAttributeIndex);

        // Texture Coordinates
        vertexTexCoordId = IntBuffer.allocate(1);
        GLES20.glGenBuffers(1,vertexTexCoordId);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,vertexTexCoordId.get(0));
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,numVertices*2*4,textCoord,GLES20.GL_DYNAMIC_DRAW);
        GLES20.glVertexAttribPointer(vertexTexCoordAttributeIndex,2,GLES20.GL_FLOAT,false,0,0);
        GLES20.glEnableVertexAttribArray(vertexTexCoordAttributeIndex);

        // Indices
        vertexIndicesBufferId = IntBuffer.allocate(1);
        GLES20.glGenBuffers(1,vertexIndicesBufferId);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER,vertexIndicesBufferId.get(0));
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER,numIndices*4,indices,GLES20.GL_STATIC_DRAW);

        // CVOpenGLESTextureCacheCreate
        // TODO: 16/1/7  CVOpenGLESTextureCacheCreate

        float[] preferredConversion = kColorConversion709;
        program.use();
        GLES20.glUniform1i(uniforms[UNIFORM.UNIFORM_Y.ordinal()],0);
        GLES20.glUniform1i(uniforms[UNIFORM.UNIFORM_UV.ordinal()],1);
        GLES20.glUniformMatrix3fv(uniforms[UNIFORM.UNIFORM_COLOR_CONVERSION_MATRIX.ordinal()],1,false,preferredConversion,0);

    }

    private MDGLProgram buildProgram(Context context){
        MDGLProgram program = new MDGLProgram();
        program.init(context);
        program.addAttribute("position");
        program.addAttribute("texCoord");

        if (!program.link()){
            Log.e(TAG,"program link failed!");
        }

        vertexAttributeIndex = program.indexOfAttribute("position");
        vertexTexCoordAttributeIndex = program.indexOfAttribute("texCoord");

        uniforms[UNIFORM.UNIFORM_MODELVIEWPROJECTION_MATRIX.ordinal()] = program.indexOfUniform("modelViewProjectionMatrix");
        uniforms[UNIFORM.UNIFORM_Y.ordinal()] = program.indexOfUniform("SamplerY");
        uniforms[UNIFORM.UNIFORM_UV.ordinal()] = program.indexOfUniform("SamplerUV");
        uniforms[UNIFORM.UNIFORM_COLOR_CONVERSION_MATRIX.ordinal()] = program.indexOfUniform("colorConversionMatrix");

        return program;

    }
}
