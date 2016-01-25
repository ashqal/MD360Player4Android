package com.asha.vrlib.objects;

import android.content.Context;
import android.opengl.GLES20;

import com.asha.vrlib.MD360Program;

import java.nio.FloatBuffer;

import static com.asha.vrlib.common.GLUtil.loadObject3D;


/**
 * Created by hzqiujiadi on 16/1/22.
 * hzqiujiadi ashqalcn@gmail.com
 */
public abstract class MDAbsObject3D {

    private static final int sPositionDataSize = 3;

    private FloatBuffer mVerticesBuffer;
    private FloatBuffer mTexCoordinateBuffer;
    private int mNumIndices;

    public MDAbsObject3D() {
    }

    public void uploadDataToProgram(MD360Program program){
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
    }

    public void loadObj(Context context){
        loadObject3D(context, obtainObjResId(), this);
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
