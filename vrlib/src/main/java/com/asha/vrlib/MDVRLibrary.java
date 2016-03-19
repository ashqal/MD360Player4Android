package com.asha.vrlib;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import com.asha.vrlib.common.GLUtil;
import com.asha.vrlib.strategy.display.DisplayModeManager;
import com.asha.vrlib.strategy.interactive.InteractiveModeManager;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by hzqiujiadi on 16/3/12.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDVRLibrary {

    private static final String TAG = "MDVRLibrary";

    // interactive mode
    public static final int INTERACTIVE_MODE_MOTION = 1;
    public static final int INTERACTIVE_MODE_TOUCH = 2;

    // display mode
    public static final int DISPLAY_MODE_NORMAL = 1;
    public static final int DISPLAY_MODE_GLASS = 2;

    // private int mDisplayMode = DISPLAY_MODE_NORMAL;
    private InteractiveModeManager mInteractiveModeManager;
    private DisplayModeManager mDisplayModeManager;

    private List<MD360Director> mDirectorList;
    private List<GLSurfaceView> mGLSurfaceViewList;
    private MD360Surface mSurface;
    private MDStatusManager mMDStatusManager;

    private MDVRLibrary(Builder builder) {
        mDirectorList = new LinkedList<>();
        mGLSurfaceViewList = new LinkedList<>();
        mSurface = new MD360Surface(builder.callback);
        mMDStatusManager = new MDStatusManager();

        // init glSurfaceViews
        initWithGLSurfaceViewIds(builder.activity,builder.glSurfaceViewIds);

        // init mode manager
        mDisplayModeManager = new DisplayModeManager(builder.displayMode,mGLSurfaceViewList);
        mInteractiveModeManager = new InteractiveModeManager(builder.interactiveMode,mDirectorList);

        mDisplayModeManager.prepare(builder.activity);
        mInteractiveModeManager.prepare(builder.activity);

        mMDStatusManager.reset(mDisplayModeManager.getVisibleSize());
    }

    private void initWithGLSurfaceViewIds(Activity activity, int[] glSurfaceViewIds){
        for (int id:glSurfaceViewIds){
            GLSurfaceView glSurfaceView = (GLSurfaceView) activity.findViewById(id);
            initOpenGL(activity,glSurfaceView,mSurface);
        }
    }

    private void initOpenGL(Context context, GLSurfaceView glSurfaceView, MD360Surface surface) {
        if (GLUtil.supportsEs2(context)) {
            // Request an OpenGL ES 2.0 compatible context.
            int index = mDirectorList.size();
            glSurfaceView.setEGLContextClientVersion(2);
            MD360Director director = MD360DirectorFactory.createDirector(index);
            MD360Renderer renderer = MD360Renderer.with(context)
                    .setSurface(surface)
                    .setDirector(director)
                    .build();
            renderer.setStatus(mMDStatusManager.newChild());

            // Set the renderer to our demo renderer, defined below.
            glSurfaceView.setRenderer(renderer);

            mDirectorList.add(director);
            mGLSurfaceViewList.add(glSurfaceView);
        } else {
            glSurfaceView.setVisibility(View.GONE);
            Toast.makeText(context, "OpenGLES2 not supported.", Toast.LENGTH_SHORT).show();
        }
    }

    public void switchInteractiveMode(Activity activity) {
        mInteractiveModeManager.switchMode(activity);
    }

    public void switchDisplayMode(Activity activity){
        mDisplayModeManager.switchMode(activity);
        mMDStatusManager.reset(mDisplayModeManager.getVisibleSize());
    }

    public void onResume(Context context){
        mInteractiveModeManager.onResume(context);

        for (GLSurfaceView glSurfaceView:mGLSurfaceViewList){
            glSurfaceView.onResume();
        }
    }

    public void onPause(Context context){
        mInteractiveModeManager.onPause(context);

        for (GLSurfaceView glSurfaceView:mGLSurfaceViewList){
            glSurfaceView.onPause();
        }
    }

    /**
     * handle touch touch to rotate the model
     *
     * @param event
     * @return true if handled.
     */
    public boolean handleTouchEvent(MotionEvent event) {
        return mInteractiveModeManager.handleTouchEvent(event);
    }

    public int getInteractiveMode() {
        return mInteractiveModeManager.getMode();
    }

    public int getDisplayMode(){
        return mDisplayModeManager.getMode();
    }

    public interface IOnSurfaceReadyCallback {
        void onSurfaceReady(Surface surface);
    }

    public static Builder with(Activity activity){
        return new Builder(activity);
    }

    public static class Builder {
        private int displayMode = DISPLAY_MODE_NORMAL;
        private int interactiveMode = INTERACTIVE_MODE_MOTION;
        private int[] glSurfaceViewIds;
        private IOnSurfaceReadyCallback callback;
        private Activity activity;

        private Builder(Activity activity) {
            this.activity = activity;
        }

        public Builder displayMode(int displayMode){
            this.displayMode = displayMode;
            return this;
        }

        public Builder interactiveMode(int interactiveMode){
            this.interactiveMode = interactiveMode;
            return this;
        }

        public Builder callback(IOnSurfaceReadyCallback callback){
            this.callback = callback;
            return this;
        }

        public MDVRLibrary build(int... glSurfaceViewIds){
            this.glSurfaceViewIds = glSurfaceViewIds;
            return new MDVRLibrary(this);
        }

    }
}
