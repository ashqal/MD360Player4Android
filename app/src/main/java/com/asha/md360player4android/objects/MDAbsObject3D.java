package com.asha.md360player4android.objects;

import android.content.Context;

import com.asha.md360player4android.common.LoadObjectHelper;

import java.nio.FloatBuffer;

/**
 * Created by hzqiujiadi on 16/1/22.
 * hzqiujiadi ashqalcn@gmail.com
 */
public abstract class MDAbsObject3D {
    private FloatBuffer mVerticesBuffer;
    private FloatBuffer mTexCoordinateBuffer;
    private int mNumIndices;

    public MDAbsObject3D() {
    }

    public void loadObj(Context context){
        LoadObjectHelper.load(context, obtainObjResId(), this);
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
