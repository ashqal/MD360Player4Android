package com.asha.vrlib.objects;

import android.content.Context;
import android.opengl.GLES20;
import android.util.SparseArray;

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

    private ShortBuffer mIndicesBuffer;
    private int mNumIndices;

    private SparseArray<FloatBuffer> mTexCoordinateBuffers = new SparseArray<>(2);
    private SparseArray<FloatBuffer> mVerticesBuffers = new SparseArray<>(2);


    public MDAbsObject3D() {

    }

    public void uploadVerticesBufferIfNeed(MD360Program program, int index){
        FloatBuffer vertexBuffer = getVerticesBuffer(index);
        if (vertexBuffer == null) return;

        vertexBuffer.position(0);

        // set data to OpenGL
        int positionHandle = program.getPositionHandle();
        GLES20.glVertexAttribPointer(positionHandle, sPositionDataSize, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(positionHandle);

    }

    public void uploadTexCoordinateBufferIfNeed(MD360Program program, int index){
        FloatBuffer textureBuffer = getTexCoordinateBuffer(index);
        if (textureBuffer == null) return;

        textureBuffer.position(0);

        // set data to OpenGL
        int textureCoordinateHandle = program.getTextureCoordinateHandle();
        GLES20.glVertexAttribPointer(textureCoordinateHandle, sTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glEnableVertexAttribArray(textureCoordinateHandle);

    }

    abstract protected void executeLoad(Context context);

    public int getNumIndices() {
        return mNumIndices;
    }

    public void setNumIndices(int mNumIndices) {
        this.mNumIndices = mNumIndices;
    }

    public FloatBuffer getVerticesBuffer(int index) {
        return mVerticesBuffers.get(index);
    }

    public void setVerticesBuffer(int index, FloatBuffer verticesBuffer) {
        mVerticesBuffers.put(index,verticesBuffer);
    }

    public FloatBuffer getTexCoordinateBuffer(int index) {
        return mTexCoordinateBuffers.get(index);
    }

    public void setTexCoordinateBuffer(int index, FloatBuffer texCoordinateBuffer) {
        mTexCoordinateBuffers.put(index,texCoordinateBuffer);
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
