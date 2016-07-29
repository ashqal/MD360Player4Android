package com.asha.vrlib;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.asha.vrlib.common.Fps;
import com.asha.vrlib.plugins.MDAbsPlugin;
import com.asha.vrlib.plugins.MDBarrelDistortionPlugin;
import com.asha.vrlib.plugins.MDPluginManager;
import com.asha.vrlib.strategy.display.DisplayModeManager;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.asha.vrlib.common.GLUtil.glCheck;

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

	private MDBarrelDistortionPlugin mBarrelDistortionPlugin;

	// final
	private final Context mContext;

	private MD360Renderer(Builder params){
		mContext = params.context;
		mDisplayModeManager = params.displayModeManager;
		mPluginManager = params.pluginManager;

		mBarrelDistortionPlugin = new MDBarrelDistortionPlugin(mContext);
		mPluginManager.add(mBarrelDistortionPlugin);
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

		boolean barrelEnabled = true;

		glCheck("MD360Renderer onDrawFrame 0");

		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		glCheck("MD360Renderer onDrawFrame 1");
		int size = mDisplayModeManager.getVisibleSize();

		int width = (int) (this.mWidth * 1.0f / size);
		int height = mHeight;

		if (barrelEnabled){
			// Barrel Distortion take over
			mBarrelDistortionPlugin.takeOver(mWidth,mHeight,size);
		}


		for (int i = 0; i < size; i++){
			GLES20.glViewport(width * i, 0, width, height);
			GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
			GLES20.glScissor(width * i, 0, width, height);

			for (MDAbsPlugin plugin : mPluginManager.getPlugins()){
				if (!(plugin instanceof MDBarrelDistortionPlugin)){
					plugin.renderer(width, height, i);
				}
			}

			GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
		}

		if (barrelEnabled){
			// Barrel Distortion render
			for (int i = 0; i < size; i++){

				GLES20.glViewport(width * i, 0, width, mHeight);
				GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
				GLES20.glScissor(width * i, 0, width, mHeight);
				mBarrelDistortionPlugin.renderer(width,height,0);
				GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
			}
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
