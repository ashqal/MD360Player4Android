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

import java.util.LinkedList;
import java.util.List;

/**
 * using multi MD360Renderer
 *
 * Created by hzqiujiadi on 16/2/22.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MultiMD360RendererDemoActivity extends MediaPlayerActivity {

    private MD360Surface mMD360Surface;
    private List<MD360Renderer> mRenderers = new LinkedList<>();
    private List<GLSurfaceView> mSurfaceViews = new LinkedList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_md_render);


        mMD360Surface = new MD360Surface(new MD360Surface.IOnSurfaceReadyListener() {
            @Override
            public void onSurfaceReady(Surface surface) {
                getPlayer().setSurface(surface);
            }
        });

        // init OpenGL
        initOpenGL(R.id.surface_view1);
        initOpenGL(R.id.surface_view2);

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
        boolean handled = false;
        for (MD360Renderer renderer : mRenderers){
            handled |= renderer.handleTouchEvent(event);
        }
        return handled || super.onTouchEvent(event);
    }

    private void initOpenGL(int glSurfaceViewResId) {
        GLSurfaceView mGLSurfaceView = (GLSurfaceView) findViewById(glSurfaceViewResId);

        if (GLUtil.supportsEs2(this)) {
            // Request an OpenGL ES 2.0 compatible context.
            mGLSurfaceView.setEGLContextClientVersion(2);

            // build render
            MD360Renderer renderer = MD360Renderer.with(this).setSurface(mMD360Surface).build();
            mRenderers.add(renderer);

            // Set the renderer to our demo renderer, defined below.
            mGLSurfaceView.setRenderer(renderer);

            mSurfaceViews.add(mGLSurfaceView);

        } else {
            mGLSurfaceView.setVisibility(View.GONE);
            Toast.makeText(MultiMD360RendererDemoActivity.this, "OpenGLES2 not supported.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        // The activity must call the GL surface view's onResume() on activity onResume().
        super.onResume();
        for (GLSurfaceView surfaceView : mSurfaceViews){
            surfaceView.onResume();
        }
    }

    @Override
    protected void onPause() {
        // The activity must call the GL surface view's onPause() on activity onPause().
        super.onPause();
        for (GLSurfaceView surfaceView : mSurfaceViews){
            surfaceView.onPause();
        }
    }
}