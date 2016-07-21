package com.asha.vrlib;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.asha.vrlib.common.Fps;
import com.asha.vrlib.plugins.MDAbsPlugin;
import com.asha.vrlib.plugins.MDPluginManager;
import com.asha.vrlib.strategy.display.DisplayModeManager;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by hzqiujiadi on 16/1/22.
 * hzqiujiadi ashqalcn@gmail.com
 *
 * @see Builder
 * @see #with(Context)
 */
public class MD360Renderer implements GLSurfaceView.Renderer {

	private static final String TAG = "MD360Renderer";
	private DisplayModeManager mDisplayModeManager;
	private MDPluginManager mPluginManager;
	private Fps mFps = new Fps();
	private int mWidth;
	private int mHeight;

	// final
	private final Context mContext;

	private MD360Renderer(Builder params){
		mContext = params.context;
		mDisplayModeManager = params.displayModeManager;
		mPluginManager = params.pluginManager;
	}

	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config){
		// set the background clear color to black.
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		
		// use culling to remove back faces.
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		
		// enable depth testing
		// GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		for (MDAbsPlugin plugin : mPluginManager.getPlugins()){
			plugin.init(mContext);
		}
	}

	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height){
		this.mWidth = width;
		this.mHeight = height;
	}

	@Override
	public void onDrawFrame(GL10 glUnused){

		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		int size = mDisplayModeManager.getVisibleSize();
		int itemWidth = (int) (this.mWidth * 1.0f / size);

		for (int i = 0; i < size; i++){
			// Set the OpenGL viewport to the same size as the surface.
			GLES20.glViewport(itemWidth * i, 0, itemWidth, mHeight);
			GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
			GLES20.glScissor(itemWidth * i, 0, itemWidth, mHeight);

			for (MDAbsPlugin plugin : mPluginManager.getPlugins()){
				plugin.renderer(itemWidth,mHeight,i);
			}

			GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
		}

		// mFps.step();
	}

	public static Builder with(Context context) {
		Builder builder = new Builder();
		builder.context = context;
		return builder;
	}

	public static class Builder{
		private Context context;
		private DisplayModeManager displayModeManager;
		public MDPluginManager pluginManager;

		private Builder() {
		}

		public MD360Renderer build(){
			return new MD360Renderer(this);
		}

		public Builder setPluginManager(MDPluginManager pluginManager) {
			this.pluginManager = pluginManager;
			return this;
		}

		public Builder setDisplayModeManager(DisplayModeManager displayModeManager) {
			this.displayModeManager = displayModeManager;
			return this;
		}
	}
}
