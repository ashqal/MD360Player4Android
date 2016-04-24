package com.asha.vrlib.objects;

import android.opengl.GLES20;

import com.asha.vrlib.MD360Program;

import java.nio.FloatBuffer;


/**
 * Created by hzqiujiadi on 16/1/22.
 * hzqiujiadi ashqalcn@gmail.com
 */
public abstract class MDAbsObject3D {

    private static final int sPositionDataSize = 3;

    private FloatBuffer mVerticesBuffer;
    private FloatBuffer mTexCoordinateBuffer;
    private int mNumIndices;

    private boolean mChanged;

    public MDAbsObject3D() {
    }

    public static MDAbsObject3D duplicate(MDAbsObject3D object3D){
        MDAbsObject3D obj = new MDAbsObject3D() {
            @Override
            protected int obtainObjResId() {
                return 0;
            }
        };
        obj.mVerticesBuffer = object3D.getVerticesBuffer().duplicate();
        obj.mTexCoordinateBuffer = object3D.getTexCoordinateBuffer().duplicate();
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
            GLES20.glVertexAttribPointer(textureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
            GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
            mChanged = false;
        }
    }

    protected abstract int obtainObjResId();

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
}
