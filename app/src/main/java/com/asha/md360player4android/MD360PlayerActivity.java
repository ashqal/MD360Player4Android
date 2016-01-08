/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.asha.md360player4android;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.AttributeSet;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Demonstrate how to use the OES_texture_cube_map extension, available on some
 * high-end OpenGL ES 1.x GPUs.
 */
public class MD360PlayerActivity extends Activity {
    private GLSurfaceView mGLSurfaceView;

    public static class MyGLSurfaceView extends GLSurfaceView{

        public MyGLSurfaceView(Context context) {
            super(context);
            init();
        }

        public MyGLSurfaceView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        private void init() {
            setEGLContextClientVersion(2);
        }
    }

    private class Renderer implements GLSurfaceView.Renderer {
        private static final String TAG = "Renderer";
        private MDRenderer renderer;



        public void onDrawFrame(GL10 gl) {
            renderer.draw(getBaseContext());
        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            //GLES20.glViewport(0, 0, width, height);
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            //GLES20.glClearColor( 0, 0xCC*1.0f/0xFF, 0x66*1.0f/0xFF, 1);
            renderer = new MDRenderer();
            renderer.update();
            renderer.setupGL(getBaseContext());
        }



    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create our surface view and set it as the content of our
        // Activity
        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setEGLContextClientVersion(3);
        mGLSurfaceView.setRenderer(new Renderer());
        setContentView(mGLSurfaceView);
    }

    @Override
    protected void onResume() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
        mGLSurfaceView.onPause();
    }
}
