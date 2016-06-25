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

    private boolean mVerticesChanged;
    private boolean mTexCoordinateChanged;

    public MDAbsObject3D() {

    }

    public void markChanged(){
        mVerticesChanged = true;
        mTexCoordinateChanged = true;
    }

    public void markVerticesChanged(){
        mVerticesChanged = true;
    }

    public void markTexCoordinateChanged(){
        mTexCoordinateChanged = true;
    }

    public void uploadVerticesBufferIfNeed(MD360Program program, int index){
        FloatBuffer vertexBuffer = getVerticesBuffer();
        if (vertexBuffer == null) return;

        if (mVerticesChanged){
            vertexBuffer.position(0);

            // set data to OpenGL
            int positionHandle = program.getPositionHandle();
            GLES20.glVertexAttribPointer(positionHandle, sPositionDataSize, GLES20.GL_FLOAT, false, 0, vertexBuffer);
            GLES20.glEnableVertexAttribArray(positionHandle);

            mVerticesChanged = false;
        }
    }

    public void uploadTexCoordinateBufferIfNeed(MD360Program program, int index){
        FloatBuffer textureBuffer = getTexCoordinateBuffer();
        if (textureBuffer == null) return;

        if (mTexCoordinateChanged){
            textureBuffer.position(0);

            // set data to OpenGL
            int textureCoordinateHandle = program.getTextureCoordinateHandle();
            GLES20.glVertexAttribPointer(textureCoordinateHandle, sTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, textureBuffer);
            GLES20.glEnableVertexAttribArray(textureCoordinateHandle);

            mTexCoordinateChanged = false;
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
