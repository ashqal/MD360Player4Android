package com.asha.vrlib;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

public class TextureViewTestActivity extends Activity implements TextureView.SurfaceTextureListener {

    private TextureView mTextureView;
    private GLProducerThread mProducerThread = null;
    private GLRendererImpl mRenderer;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mTextureView = new TextureView(this);
        mTextureView.setSurfaceTextureListener(this);
        mTextureView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);//隐藏虚拟按键，即navigator bar
        setContentView(mTextureView);

        mRenderer = new GLRendererImpl(this);
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
                                          int height) {
        mRenderer.setViewport(width, height);
        mProducerThread = new GLProducerThread(surface, mRenderer);
        mProducerThread.start();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mProducerThread.exit();
        mProducerThread = null;
        return true;
    }


    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
                                            int height) {
        mRenderer.resize(width, height);

    }


    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }


    public interface GLRenderer {
        void drawFrame();
        void init();
    }

    public class GLProducerThread extends Thread {

        private AtomicBoolean mShouldRender;
        private SurfaceTexture mSurfaceTexture;
        private GLRenderer mRenderer;

        private EGL10 mEgl;
        private EGLDisplay mEglDisplay = EGL10.EGL_NO_DISPLAY;
        private EGLContext mEglContext = EGL10.EGL_NO_CONTEXT;
        private EGLSurface mEglSurface = EGL10.EGL_NO_SURFACE;


        private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
        private static final int EGL_OPEN_GL_ES2_BIT = 4;


        public GLProducerThread(SurfaceTexture surfaceTexture, GLRenderer renderer) {
            mSurfaceTexture = surfaceTexture;
            mRenderer = renderer;
            mShouldRender = new AtomicBoolean(true);
        }

        public void exit() {
            mShouldRender.set(false);
        }

        private void initGL() {
            mEgl = (EGL10) EGLContext.getEGL();

            mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            if (mEglDisplay == EGL10.EGL_NO_DISPLAY) {
                throw new RuntimeException("eglGetdisplay failed : " +
                        GLUtils.getEGLErrorString(mEgl.eglGetError()));
            }

            int[] version = new int[2];
            if (!mEgl.eglInitialize(mEglDisplay, version)) {
                throw new RuntimeException("eglInitialize failed : " +
                        GLUtils.getEGLErrorString(mEgl.eglGetError()));
            }

            int[] configAttributes = {
                    EGL10.EGL_BUFFER_SIZE, 32,
                    EGL10.EGL_ALPHA_SIZE, 8,
                    EGL10.EGL_BLUE_SIZE, 8,
                    EGL10.EGL_GREEN_SIZE, 8,
                    EGL10.EGL_RED_SIZE, 8,
                    EGL10.EGL_RENDERABLE_TYPE, EGL_OPEN_GL_ES2_BIT,
                    EGL10.EGL_SURFACE_TYPE, EGL10.EGL_WINDOW_BIT,
                    EGL10.EGL_NONE
            };

            int[] numConfigs = new int[1];
            EGLConfig[] configs = new EGLConfig[1];
            if (!mEgl.eglChooseConfig(mEglDisplay, configAttributes, configs, 1, numConfigs)) {
                throw new RuntimeException("eglChooseConfig failed : " +
                        GLUtils.getEGLErrorString(mEgl.eglGetError()));
            }

            int[] contextAttributes = {
                    EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL10.EGL_NONE
            };

            mEglContext = mEgl.eglCreateContext(mEglDisplay, configs[0], EGL10.EGL_NO_CONTEXT, contextAttributes);
            mEglSurface = mEgl.eglCreateWindowSurface(mEglDisplay, configs[0], mSurfaceTexture, null);
            if (mEglSurface == EGL10.EGL_NO_SURFACE || mEglContext == EGL10.EGL_NO_CONTEXT) {
                int error = mEgl.eglGetError();
                if (error == EGL10.EGL_BAD_NATIVE_WINDOW) {
                    throw new RuntimeException("eglCreateWindowSurface returned  EGL_BAD_NATIVE_WINDOW. ");
                }
                throw new RuntimeException("eglCreateWindowSurface failed : " +
                        GLUtils.getEGLErrorString(mEgl.eglGetError()));
            }

            if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
                throw new RuntimeException("eglMakeCurrent failed : " +
                        GLUtils.getEGLErrorString(mEgl.eglGetError()));
            }

            // mGL = mEglContext.getGL();
        }

        private void destroyGL() {
            mEgl.eglDestroyContext(mEglDisplay, mEglContext);
            mEgl.eglDestroySurface(mEglDisplay, mEglSurface);
            mEglContext = EGL10.EGL_NO_CONTEXT;
            mEglSurface = EGL10.EGL_NO_SURFACE;
        }

        public void run() {
            initGL();

            if (mRenderer != null) {
                mRenderer.init();
            }

            while (mShouldRender != null && mShouldRender.get()) {
                if (mRenderer != null)
                    mRenderer.drawFrame();
                mEgl.eglSwapBuffers(mEglDisplay, mEglSurface);

                try {
                    sleep(5);
                } catch (InterruptedException e) {

                }
            }

            destroyGL();
        }
    }

    public static class GLRendererImpl implements GLRenderer {

        private int mProgramObject;
        private int mWidth;
        private int mHeight;
        private FloatBuffer mVertices;
        private ShortBuffer mTexCoords;
        private Context mContext;
        private int mTexID;
        private static String TAG = "GLRendererImpl";

        private final float[] mVerticesData = {-0.5f, -0.5f, 0, 0.5f, -0.5f, 0, -0.5f, 0.5f, 0, 0.5f, 0.5f, 0};
        private final short[] mTexCoordsData = {0, 1, 1, 1, 0, 0, 1, 0};

        public GLRendererImpl(Context ctx) {
            mVertices = ByteBuffer.allocateDirect(mVerticesData.length * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            mVertices.put(mVerticesData).position(0);

            mTexCoords = ByteBuffer.allocateDirect(mTexCoordsData.length * 2)
                    .order(ByteOrder.nativeOrder()).asShortBuffer();
            mTexCoords.put(mTexCoordsData).position(0);

            mContext = ctx;

        }

        public void setViewport(int width, int height) {
            mWidth = width;
            mHeight = height;
        }

        private void initGL() {
            comipleAndLinkProgram();

            loadTexture();

            GLES20.glClearColor(0, 0, 0, 0);
        }

        public void resize(int width, int height) {
            mWidth = width;
            mHeight = height;

        }

        @Override
        public void drawFrame() {
            GLES20.glViewport(0, 0, mWidth, mHeight);

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            GLES20.glUseProgram(mProgramObject);

            GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, mVertices);
            GLES20.glEnableVertexAttribArray(0);
            GLES20.glVertexAttribPointer(1, 2, GLES20.GL_SHORT, false, 0, mTexCoords);
            GLES20.glEnableVertexAttribArray(1);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexID);
            int loc = GLES20.glGetUniformLocation(mProgramObject, "u_Texture");
            GLES20.glUniform1f(loc, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

            Log.i("GLRendererImpl", "drawing..." + mWidth);
        }

        @Override
        public void init() {
            initGL();
        }


        private void loadTexture() {
            Bitmap b = BitmapFactory.decodeResource(mContext.getResources(), android.R.drawable.ic_popup_sync);
            if (b != null) {
                int[] texID = new int[1];
                GLES20.glGenTextures(1, texID, 0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texID[0]);
                mTexID = texID[0];

                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                        GLES20.GL_LINEAR);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                        GLES20.GL_LINEAR);

                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                        GLES20.GL_REPEAT);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                        GLES20.GL_REPEAT);

                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, b, 0);
                b.recycle();
            }
        }

        private int loadShader(int shaderType, String shaderSource) {
            int shader;
            int[] compiled = new int[1];

            // Create the shader object
            shader = GLES20.glCreateShader(shaderType);

            if (shader == 0)
                return 0;

            // Load the shader source
            GLES20.glShaderSource(shader, shaderSource);

            // Compile the shader
            GLES20.glCompileShader(shader);

            // Check the compile status
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);

            if (compiled[0] == 0) {
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                return 0;
            }
            return shader;
        }

        private void comipleAndLinkProgram() {
            String vShaderStr = "attribute vec4 a_position;    \n"
                    + "attribute vec2 a_texCoords; \n"
                    + "varying vec2 v_texCoords; \n"
                    + "void main()                  \n"
                    + "{                            \n"
                    + "   gl_Position = a_position;  \n"
                    + "    v_texCoords = a_texCoords; \n"
                    + "}                            \n";

            String fShaderStr = "precision mediump float;					  \n"
                    + "uniform sampler2D u_Texture; \n"
                    + "varying vec2 v_texCoords; \n"
                    + "void main()                                  \n"
                    + "{                                            \n"
                    + "  gl_FragColor = texture2D(u_Texture, v_texCoords) ;\n"
                    + "}                                            \n";

            int vertexShader;
            int fragmentShader;
            int programObject;
            int[] linked = new int[1];

            // Load the vertex/fragment shaders
            vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vShaderStr);
            fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fShaderStr);

            // Create the program object
            programObject = GLES20.glCreateProgram();

            if (programObject == 0)
                return;

            GLES20.glAttachShader(programObject, vertexShader);
            GLES20.glAttachShader(programObject, fragmentShader);

            // Bind vPosition to attribute 0
            GLES20.glBindAttribLocation(programObject, 0, "a_position");
            GLES20.glBindAttribLocation(programObject, 1, "a_texCoords");

            // Link the program
            GLES20.glLinkProgram(programObject);

            // Check the link status
            GLES20.glGetProgramiv(programObject, GLES20.GL_LINK_STATUS, linked, 0);

            if (linked[0] == 0) {
                Log.e(TAG, "Error linking program:");
                Log.e(TAG, GLES20.glGetProgramInfoLog(programObject));
                GLES20.glDeleteProgram(programObject);
                return;
            }

            mProgramObject = programObject;
        }
    }

}