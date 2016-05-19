package com.asha.vrlib.objects;

import android.content.Context;
import android.opengl.GLES20;

import com.asha.vrlib.MD360Program;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;


/**
 * Created by hzqiujiadi on 16/1/22.
 * hzqiujiadi ashqalcn@gmail.com
 */
public abstract class MDAbsObject3D {

    private static final int sPositionDataSize = 3;
    private static final int sTextureCoordinateDataSize = 2;

    private FloatBuffer mVerticesBuffer;
    private FloatBuffer mTexCoordinateBuffer;
    private ShortBuffer mIndicesBuffer;
    private int mNumIndices;

    private boolean mChanged;

    public MDAbsObject3D() {
    }

    public static MDAbsObject3D duplicate(MDAbsObject3D object3D){
        MDAbsObject3D obj = new MDAbsObject3D(){

            @Override
            protected void executeLoad(Context context) {
                // nop
            }
        };
        obj.mVerticesBuffer = object3D.getVerticesBuffer().duplicate();
        obj.mTexCoordinateBuffer = object3D.getTexCoordinateBuffer().duplicate();
        if (object3D.getIndicesBuffer() != null)
            obj.mIndicesBuffer = object3D.getIndicesBuffer().duplicate();
        obj.mNumIndices = object3D.getNumIndices();
        return obj;
    }

    public void markChanged(){
        mChanged = true;
    }

    public void uploadDataToProgramIfNeed(MD360Program program){
        if (mChanged){
            // set data to OpenGL
            FloatBuffer vertexBuffer = getVerticesBuffer();
            FloatBuffer textureBuffer = getTexCoordinateBuffer();

            vertexBuffer.position(0);
            textureBuffer.position(0);

            int positionHandle = program.getPositionHandle();
            GLES20.glVertexAttribPointer(positionHandle, sPositionDataSize, GLES20.GL_FLOAT, false, 0, vertexBuffer);
            GLES20.glEnableVertexAttribArray(positionHandle);

            int textureCoordinateHandle = program.getTextureCoordinateHandle();
            GLES20.glVertexAttribPointer(textureCoordinateHandle, sTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, textureBuffer);
            GLES20.glEnableVertexAttribArray(textureCoordinateHandle);

            mChanged = false;
        }
    }

    abstract protected void executeLoad(Context context);

    public int getNumIndices() {
        return mNumIndices;
    }

    public void setNumIndices(int mNumIndices) {
        this.mNumIndices = mNumIndices;
    }

    public FloatBuffer getVerticesBuffer() {
        return mVerticesBuffer;
    }

    public void setVerticesBuffer(FloatBuffer verticesBuffer) {
        this.mVerticesBuffer = verticesBuffer;
    }

    public FloatBuffer getTexCoordinateBuffer() {
        return mTexCoordinateBuffer;
    }

    public void setTexCoordinateBuffer(FloatBuffer texCoordinateBuffer) {
        this.mTexCoordinateBuffer = texCoordinateBuffer;
    }

    public ShortBuffer getIndicesBuffer() {
        return mIndicesBuffer;
    }

    public void setIndicesBuffer(ShortBuffer mIndicesBuffer) {
        this.mIndicesBuffer = mIndicesBuffer;
    }

    public void draw() {
        // Draw
        if (getIndicesBuffer() != null){
            getIndicesBuffer().position(0);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, getNumIndices(), GLES20.GL_UNSIGNED_SHORT, getIndicesBuffer());
        } else {
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, getNumIndices());
        }
    }
}
