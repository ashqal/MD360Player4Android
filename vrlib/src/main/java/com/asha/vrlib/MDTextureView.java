package com.asha.vrlib;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * Created by hzqiujiadi on 16/3/10.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDTextureView extends TextureView {
    MDOpenGLThread mMDOpenGLThread;
    public MDTextureView(Context context) {
        this(context, null);
    }

    public MDTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MDTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MDTextureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    public void init(MD360Renderer renderer) {
        mMDOpenGLThread = new MDOpenGLThread(renderer);
        this.setSurfaceTextureListener(mMDOpenGLThread);
    }
}
