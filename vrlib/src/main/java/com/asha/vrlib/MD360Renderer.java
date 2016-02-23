package com.asha.vrlib;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import com.asha.vrlib.objects.MDAbsObject3D;
import com.asha.vrlib.objects.MDSphere3D;

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

	private MDAbsObject3D mObject3D;
	private MD360Program mProgram;
	private MD360Surface mSurface;

	// final
	private final Context mContext;
	private final MD360Director mDirector;


	private MD360Renderer(Builder params){
		mContext = params.context;
		mSurface = params.surface;
		mDirector = new MD360Director();
		mObject3D = new MDSphere3D();
		mProgram = new MD360Program();
	}

	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config){
		// set the background clear color to black.
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		
		// use culling to remove back faces.
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		
		// enable depth testing
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		// init
		initProgram();
		initTexture();
		initObject3D();
	}

	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height){
		// Set the OpenGL viewport to the same size as the surface.
		GLES20.glViewport(0, 0, width, height);

		// Update surface
		mSurface.resize(width,height);

		// Update Projection
		mDirector.updateProjection(width,height);
	}

	@Override
	public void onDrawFrame(GL10 glUnused){
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		// Set our per-vertex lighting program.
		mProgram.use();

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        
        // Bind the texture to this unit.
        // GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureDataHandle);
		mSurface.onDrawFrame();
        
        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mProgram.getTextureUniformHandle(), 0);

		// Pass in the combined matrix.
		mDirector.shot(mProgram);

		// Draw
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mObject3D.getNumIndices());
	}

	private void initProgram(){
		mProgram.build(mContext);
	}

	private void initTexture(){
		mSurface.createSurface();
	}

	private void initObject3D(){
		// load
		mObject3D.loadObj(mContext);

		// upload
		mObject3D.uploadDataToProgram(mProgram);
	}

	/**
	 * handle touch touch to rotate the model
	 *
	 * @param event
	 * @return true if handled.
	 */
	public boolean handleTouchEvent(MotionEvent event) {
		return mDirector.handleTouchEvent(event);
	}

	public static Builder with(Context context) {
		Builder builder = new Builder();
		builder.context = context;
		return builder;
	}

	public static class Builder{
		private Context context;
		private MD360Surface surface;

		private Builder() {
		}

		public MD360Renderer build(){
			return new MD360Renderer(this);
		}

		/**
		 * set surface{@link MD360Surface} to this render
		 * @param surface {@link MD360Surface} surface may used by multiple render{@link MD360Renderer}
		 * @return builder
		 */
		public Builder setSurface(MD360Surface surface){
			this.surface = surface;
			return this;
		}

		/**
		 * add IOnSurfaceReadyListener listener
		 * the render will invoke the callback if the Surface is ready
		 * @param listener onSurfaceReady(Surface surface)
		 */
		public Builder defaultSurface(MD360Surface.IOnSurfaceReadyListener listener){
			this.surface = new MD360Surface(listener);
			return this;
		}
	}
}
