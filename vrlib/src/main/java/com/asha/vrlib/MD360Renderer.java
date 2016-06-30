package com.asha.vrlib;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.asha.vrlib.common.Fps;
import com.asha.vrlib.objects.MDAbsObject3D;
import com.asha.vrlib.strategy.display.DisplayModeManager;
import com.asha.vrlib.strategy.projection.ProjectionModeManager;
import com.asha.vrlib.texture.MD360Texture;

import java.util.List;

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

	private MD360Program mProgram;
	private MD360Texture mTexture;
	private DisplayModeManager mDisplayModeManager;
	private ProjectionModeManager mProjectionModeManager;
	private Fps mFps = new Fps();
	private int mWidth;
	private int mHeight;

	// final
	private final Context mContext;

	private MD360Renderer(Builder params){
		mContext = params.context;
		mTexture = params.texture;
		mDisplayModeManager = params.displayModeManager;
		mProjectionModeManager = params.projectionModeManager;
		mProgram = new MD360Program(params.contentType);
	}

	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config){
		// set the background clear color to black.
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		
		// use culling to remove back faces.
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		
		// enable depth testing
		// GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		// init
		initProgram();
		initTexture();
		initObject3D();

		Log.d(TAG,"onSurfaceCreated");
	}

	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height){
		this.mWidth = width;
		this.mHeight = height;
		Log.d(TAG,"onSurfaceChanged");
	}

	@Override
	public void onDrawFrame(GL10 glUnused){
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		// mFps.step();

		MDAbsObject3D object3D = mProjectionModeManager.getObject3D();

		// check obj3d
		if (object3D == null) return;

		boolean updated = mTexture.updateTexture();
		if(updated){
			int size = mDisplayModeManager.getVisibleSize();
			int itemWidth = (int) (this.mWidth * 1.0f / size);
			List<MD360Director> directors = mProjectionModeManager.getDirectors();
			for (int i = 0; i < size; i++){

				if (i >= directors.size()) return;

				MD360Director director = directors.get(i);

				// Set the OpenGL viewport to the same size as the surface.
				GLES20.glViewport(itemWidth * i, 0, itemWidth, mHeight);

				// Update Projection
				director.updateViewport(itemWidth, mHeight);

				// Set our per-vertex lighting program.
				mProgram.use();
				glCheck("mProgram use");

				object3D.uploadVerticesBufferIfNeed(mProgram, i);
				object3D.uploadTexCoordinateBufferIfNeed(mProgram, i);

				// Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
				GLES20.glUniform1i(mProgram.getTextureUniformHandle(), 0);
				glCheck("glUniform1i");

				// Pass in the combined matrix.
				director.shot(mProgram);

				object3D.draw();
			}

		}


	}

	private void initProgram(){
		mProgram.build(mContext);
	}

	private void initTexture(){
		mTexture.create();
	}

	private void initObject3D() {
		if (mProjectionModeManager != null && mProjectionModeManager.getObject3D() != null){
			mProjectionModeManager.getObject3D().markChanged();
		}
	}

	public static Builder with(Context context) {
		Builder builder = new Builder();
		builder.context = context;
		return builder;
	}

	public static class Builder{
		private Context context;
		private MD360Texture texture;
		private int contentType = MDVRLibrary.ContentType.DEFAULT;
		private DisplayModeManager displayModeManager;
		private ProjectionModeManager projectionModeManager;

		private Builder() {
		}

		public MD360Renderer build(){
			return new MD360Renderer(this);
		}

		public Builder setContentType(int contentType) {
			this.contentType = contentType;
			return this;
		}

		/**
		 * set surface{@link MD360Texture} to this render
		 * @param texture {@link MD360Texture} surface may used by multiple render{@link MD360Renderer}
		 * @return builder
		 */
		public Builder setTexture(MD360Texture texture){
			this.texture = texture;
			return this;
		}

		public Builder setDisplayModeManager(DisplayModeManager displayModeManager) {
			this.displayModeManager = displayModeManager;
			return this;
		}

		public Builder setProjectionModeManager(ProjectionModeManager projectionModeManager) {
			this.projectionModeManager = projectionModeManager;
			return this;
		}
	}
}
