package com.asha.md360player4android;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import com.asha.vrlib.MD360Renderer;
import com.asha.vrlib.MD360Surface;
import com.asha.vrlib.common.GLUtil;

/**
 * using MD360Renderer
 *
 * Created by hzqiujiadi on 16/1/22.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MD360RendererDemoActivity extends MediaPlayerActivity {

    private GLSurfaceView mGLSurfaceView;
    private MD360Renderer mRenderer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_md_render);

        mRenderer = MD360Renderer.with(this)
                .defaultSurface(new MD360Surface.IOnSurfaceReadyListener() {
                    @Override
                    public void onSurfaceReady(Surface surface) {
                        getPlayer().setSurface(surface);
                    }
                })
                .build();

        openRemoteFile();

        // init OpenGL
        initOpenGL(R.id.surface_view);

        // play button
        findViewById(R.id.btn_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mRenderer.handleTouchEvent(event) || super.onTouchEvent(event);
    }

    private void initOpenGL(int glSurfaceViewResId) {
        mGLSurfaceView = (GLSurfaceView) findViewById(glSurfaceViewResId);

        if (GLUtil.supportsEs2(this)) {
            // Request an OpenGL ES 2.0 compatible context.
            mGLSurfaceView.setEGLContextClientVersion(2);

            // Set the renderer to our demo renderer, defined below.
            mGLSurfaceView.setRenderer(mRenderer);
        } else {
            mGLSurfaceView.setVisibility(View.GONE);
            Toast.makeText(MD360RendererDemoActivity.this, "OpenGLES2 not supported.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        // The activity must call the GL surface view's onResume() on activity onResume().
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        // The activity must call the GL surface view's onPause() on activity onPause().
        super.onPause();
        mGLSurfaceView.onPause();
    }
}